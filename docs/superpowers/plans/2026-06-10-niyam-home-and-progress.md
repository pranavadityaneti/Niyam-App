# Niyam — Home & Sadhana Progress — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Record every completed unlock-read in Room, surface Day-N-of-14 progress / streak / today's reads on a brand-rebuilt Home, and ship the completion celebration → next-sadhana picker, per [the SP-4 spec](../specs/2026-06-10-home-and-progress-design.md).

**Architecture:** One Room table (`read_events`) behind a lazy `ProgressRepository` (single-thread executor; fire-and-forget `recordRead` from the overlay Continue tap — the unlock can never block on it). Pure `ProgressMath` for streak/day math (JVM-tested). UserPrefs gains intention/start-day/completed/pendingCelebration. Completion detection inside `recordRead`; a persisted flag routes to the celebration → picker flow on next Home composition.

**Tech Stack:** Room 2.6.1 + KSP (`2.0.21-1.0.28`, matching Kotlin 2.0.21), existing Compose/DataStore stack.

---

## Execution conventions

Same as SP-3: continuous full-control mode; Opus subagents; two-stage (or combined, for small diffs) review per task; direct commit+push per task; failures-twice → ERRORS.md + escalate. Baseline: HEAD `4db4675`, **55 tests green**. Brand law and ui-ux-pro-max protocol apply to UI tasks (9, 10).

## File structure

```
gradle/libs.versions.toml                                   M  T1 (room, ksp)
build.gradle.kts                                            M  T1
app/build.gradle.kts                                        M  T1
app/src/main/java/com/myniyam/app/progress/NiyamDatabase.kt    C  T2 (entity+dao+db)
app/src/main/java/com/myniyam/app/progress/ProgressMath.kt     C  T3 (+test)
app/src/main/java/com/myniyam/app/progress/ProgressRepository.kt C T4
app/src/main/java/com/myniyam/app/data/UserPrefs.kt            M  T5 (+test update)
app/src/main/java/com/myniyam/app/onboarding/OnboardingViewModel.kt M T6 (+test update)
app/src/main/java/com/myniyam/app/onboarding/IntentionScreen.kt     M  T6
app/src/main/java/com/myniyam/app/overlay/OverlayManager.kt    M  T7 (recordRead)
app/src/main/java/com/myniyam/app/NiyamApplication.kt          M  T7 (warmUp)
app/src/main/java/com/myniyam/app/data/StarterMantras.kt       M  T8 (priorityIds)
app/src/main/java/com/myniyam/app/progress/NextSadhana.kt      C  T8 (+test)
app/src/main/java/com/myniyam/app/ui/screens/HomeScreen.kt     M  T9 (rebuild)
app/src/main/java/com/myniyam/app/ui/screens/CelebrationScreen.kt   C  T10
app/src/main/java/com/myniyam/app/ui/screens/NextSadhanaScreen.kt   C  T10
app/src/main/java/com/myniyam/app/ui/AppNavHost.kt             M  T10
app/src/main/res/values/strings.xml                            M  T9, T10
docs/superpowers/test-reports/2026-06-10-sp4-acceptance.md     C  T11
```

Expected test growth: 55 → **68** (ProgressMath 6, UserPrefs 4→7, OnboardingViewModel 4→5, NextSadhana 3).

---

## Task 1: Room + KSP dependencies

**Files:** Modify `gradle/libs.versions.toml`, `build.gradle.kts`, `app/build.gradle.kts`.

- [ ] **1.1** `libs.versions.toml` — `[versions]` add `room = "2.6.1"` and `ksp = "2.0.21-1.0.28"`. `[libraries]` add:
```toml
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
```
`[plugins]` add: `ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }`
- [ ] **1.2** Root `build.gradle.kts` plugins block: `alias(libs.plugins.ksp) apply false`. App `plugins`: `alias(libs.plugins.ksp)`. App dependencies:
```kotlin
implementation(libs.androidx.room.runtime)
implementation(libs.androidx.room.ktx)
ksp(libs.androidx.room.compiler)
```
- [ ] **1.3** `./gradlew :app:compileDebugKotlin` → BUILD SUCCESSFUL. If KSP `2.0.21-1.0.28` doesn't resolve, use the newest `2.0.21-1.0.x` listed in the error and record it.
- [ ] **1.4** Full suite → 55/55. Commit `chore: add Room + KSP for read-event storage` + push.

## Task 2: NiyamDatabase (entity + DAO + database, one file)

**Files:** Create `app/src/main/java/com/myniyam/app/progress/NiyamDatabase.kt`.

- [ ] **2.1** Write:
```kotlin
package com.myniyam.app.progress

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity(
    tableName = "read_events",
    indices = [Index("mantraId", "epochDay"), Index("epochDay")]
)
data class ReadEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mantraId: String,
    val epochDay: Long,
    val timestampMs: Long
)

@Dao
interface ReadEventDao {
    @Insert
    suspend fun insert(event: ReadEventEntity)

    @Query("SELECT COUNT(DISTINCT epochDay) FROM read_events WHERE mantraId = :mantraId AND epochDay >= :sinceEpochDay")
    suspend fun distinctDaysFor(mantraId: String, sinceEpochDay: Long): Int

    @Query("SELECT DISTINCT epochDay FROM read_events")
    suspend fun allReadDays(): List<Long>

    @Query("SELECT COUNT(*) FROM read_events WHERE epochDay = :epochDay")
    suspend fun countOn(epochDay: Long): Int
}

@Database(entities = [ReadEventEntity::class], version = 1, exportSchema = false)
abstract class NiyamDatabase : RoomDatabase() {
    abstract fun readEventDao(): ReadEventDao

    companion object {
        @Volatile private var instance: NiyamDatabase? = null

        fun get(context: Context): NiyamDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    NiyamDatabase::class.java,
                    "niyam.db"
                ).build().also { instance = it }
            }
    }
}
```
- [ ] **2.2** `./gradlew :app:compileDebugKotlin` → BUILD SUCCESSFUL (KSP generates the impl — this is the Room schema gate). Full suite 55/55.
- [ ] **2.3** Commit `feat(progress): Room database — read_events entity + DAO` + push.

## Task 3: ProgressMath (TDD)

**Files:** Test `app/src/test/java/com/myniyam/app/progress/ProgressMathTest.kt`, Create `app/src/main/java/com/myniyam/app/progress/ProgressMath.kt`.

- [ ] **3.1** Failing test:
```kotlin
package com.myniyam.app.progress

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressMathTest {

    @Test
    fun `empty days means zero streak`() {
        assertEquals(0, ProgressMath.streak(emptySet(), today = 100))
    }

    @Test
    fun `read today only is streak 1`() {
        assertEquals(1, ProgressMath.streak(setOf(100L), today = 100))
    }

    @Test
    fun `read yesterday but not today keeps streak alive`() {
        assertEquals(1, ProgressMath.streak(setOf(99L), today = 100))
    }

    @Test
    fun `consecutive days count back from today`() {
        assertEquals(4, ProgressMath.streak(setOf(97L, 98L, 99L, 100L), today = 100))
    }

    @Test
    fun `gap breaks the streak`() {
        assertEquals(2, ProgressMath.streak(setOf(96L, 99L, 100L), today = 100))
    }

    @Test
    fun `dayN is capped at the threshold`() {
        assertEquals(3, ProgressMath.dayN(distinctDays = 3, capM = 14))
        assertEquals(14, ProgressMath.dayN(distinctDays = 20, capM = 14))
    }
}
```
- [ ] **3.2** Run focused → FAILS (unresolved ProgressMath).
- [ ] **3.3** Implementation:
```kotlin
package com.myniyam.app.progress

/** Pure date math over epochDay longs (spec §4). All edges JVM-tested. */
object ProgressMath {

    /**
     * Consecutive days ending today — or ending yesterday when today has
     * no read yet (the streak isn't broken until the day is over).
     */
    fun streak(readDays: Set<Long>, today: Long): Int {
        val anchor = when {
            today in readDays -> today
            (today - 1) in readDays -> today - 1
            else -> return 0
        }
        var n = 0
        while (anchor - n in readDays) n++
        return n
    }

    fun dayN(distinctDays: Int, capM: Int): Int = distinctDays.coerceAtMost(capM)
}
```
- [ ] **3.4** Focused 6/6; full suite → **61/61**.
- [ ] **3.5** Commit `feat(progress): ProgressMath — streak + dayN, fully tested` + push.

## Task 4: ProgressRepository

**Files:** Create `app/src/main/java/com/myniyam/app/progress/ProgressRepository.kt`.

- [ ] **4.1** Write:
```kotlin
package com.myniyam.app.progress

import android.content.Context
import android.util.Log
import com.myniyam.app.data.MantraRepository
import com.myniyam.app.data.UserPrefs
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.util.concurrent.Executors

/**
 * Read-event recording + home stats (spec §4). recordRead is
 * fire-and-forget on a single-thread executor — the unlock path can
 * never block or crash on it.
 */
object ProgressRepository {

    private const val TAG = "ProgressRepository"
    private val executor = Executors.newSingleThreadExecutor()

    fun warmUp(context: Context) {
        val app = context.applicationContext
        executor.execute {
            try {
                NiyamDatabase.get(app)
            } catch (e: Exception) {
                Log.e(TAG, "warmUp failed", e)
            }
        }
    }

    fun recordRead(context: Context, mantraId: String) {
        val app = context.applicationContext
        executor.execute {
            try {
                val today = LocalDate.now().toEpochDay()
                runBlocking {
                    NiyamDatabase.get(app).readEventDao().insert(
                        ReadEventEntity(
                            mantraId = mantraId,
                            epochDay = today,
                            timestampMs = System.currentTimeMillis()
                        )
                    )
                    maybeComplete(app, mantraId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "recordRead failed", e)
            }
        }
    }

    private suspend fun maybeComplete(context: Context, mantraId: String) {
        val snap = UserPrefs.snapshot()
        if (mantraId != snap.currentMantraId) return
        if (mantraId in snap.completedMantraIds) return
        val mantra = MantraRepository.byId(mantraId) ?: return
        val days = NiyamDatabase.get(context).readEventDao()
            .distinctDaysFor(mantraId, snap.sadhanaStartEpochDay)
        if (days >= mantra.completionThresholdDays) {
            UserPrefs.markCompleted(context, mantraId)
        }
    }

    data class HomeStats(val dayN: Int, val dayM: Int, val streak: Int, val todayReads: Int)

    suspend fun homeStats(context: Context): HomeStats {
        return try {
            val app = context.applicationContext
            val snap = UserPrefs.snapshot()
            val dao = NiyamDatabase.get(app).readEventDao()
            val today = LocalDate.now().toEpochDay()
            val mantra = MantraRepository.displayMantra(snap.currentMantraId)
            val distinct = dao.distinctDaysFor(snap.currentMantraId, snap.sadhanaStartEpochDay)
            HomeStats(
                dayN = ProgressMath.dayN(distinct, mantra.completionThresholdDays),
                dayM = mantra.completionThresholdDays,
                streak = ProgressMath.streak(dao.allReadDays().toSet(), today),
                todayReads = dao.countOn(today)
            )
        } catch (e: Exception) {
            Log.e(TAG, "homeStats failed", e)
            HomeStats(0, 14, 0, 0)
        }
    }
}
```
NOTE: `UserPrefs.markCompleted` doesn't exist until Task 5 — Tasks 4 and 5 are committed TOGETHER in Task 5's commit if needed; preferred order: implement Task 5 FIRST if the executor wants compilable intermediate states. The plan orders 4→5 for readability; the implementing subagent for T4 should stub NOTHING — instead implement T4 and T5 in the listed order but only run the full build at T5 if T4 alone cannot compile. Cleanest: T4's executor checks compile; if markCompleted missing, implement T4 with the `maybeComplete` body commented `// wired in T5` is FORBIDDEN (no placeholders) — so: **T4 and T5 are one combined task for execution purposes** (single commit `feat(progress): ProgressRepository + UserPrefs progress fields`), with T5's content below.

## Task 5: UserPrefs progress fields (executes combined with Task 4)

**Files:** Modify `app/src/main/java/com/myniyam/app/data/UserPrefs.kt`, `app/src/test/java/com/myniyam/app/data/UserPrefsTest.kt`.

- [ ] **5.1** UserPrefs additions — new keys:
```kotlin
private val KEY_SELECTED_INTENTION = stringPreferencesKey("selected_intention")
private val KEY_SADHANA_START = longPreferencesKey("sadhana_start_epoch_day")
private val KEY_COMPLETED_MANTRAS = stringSetPreferencesKey("completed_mantra_ids")
private val KEY_PENDING_CELEBRATION = booleanPreferencesKey("pending_celebration")
```
(import `androidx.datastore.preferences.core.longPreferencesKey`). Snapshot gains:
```kotlin
val selectedIntention: Intention,
val sadhanaStartEpochDay: Long,
val completedMantraIds: Set<String>,
val pendingCelebration: Boolean
```
DEFAULTS: `selectedIntention = Intention.SADHANA, sadhanaStartEpochDay = 0L, completedMantraIds = emptySet(), pendingCelebration = false`. `fromRaw` gains params `intention: String?`, `sadhanaStart: Long?`, `completed: Set<String>?`, `pendingCelebration: Boolean?` mapped with the same fallback pattern (unknown intention name → SADHANA; null start → 0L; null completed → empty; null flag → false). `ensureLoaded` reads the four new keys. New/changed writers:
```kotlin
suspend fun setCurrentMantra(context: Context, mantraId: String, startEpochDay: Long = java.time.LocalDate.now().toEpochDay()) {
    context.niyamDataStore.edit {
        it[KEY_CURRENT_MANTRA_ID] = mantraId
        it[KEY_SADHANA_START] = startEpochDay
        it[KEY_PENDING_CELEBRATION] = false
    }
    current = current.copy(currentMantraId = mantraId, sadhanaStartEpochDay = startEpochDay, pendingCelebration = false)
}

suspend fun setIntention(context: Context, intention: Intention) {
    context.niyamDataStore.edit { it[KEY_SELECTED_INTENTION] = intention.name }
    current = current.copy(selectedIntention = intention)
}

suspend fun markCompleted(context: Context, mantraId: String) {
    val newSet = current.completedMantraIds + mantraId
    context.niyamDataStore.edit {
        it[KEY_COMPLETED_MANTRAS] = newSet
        it[KEY_PENDING_CELEBRATION] = true
    }
    current = current.copy(completedMantraIds = newSet, pendingCelebration = true)
}
```
(`setCurrentMantra` re-stamping the start day + clearing the flag means onboarding, the next-picker, and "keep this mantra" all get correct journey-reset semantics for free.)
- [ ] **5.2** UserPrefsTest updates — extend the existing 4 tests and add 3:
  - `defaults match the pre-SP3 hardcoded behavior` gains: `assertEquals(Intention.SADHANA, s.selectedIntention)`, `assertEquals(0L, s.sadhanaStartEpochDay)`, `assertTrue(s.completedMantraIds.isEmpty())`, `assertFalse(s.pendingCelebration)` (import Intention, assertTrue).
  - `fromRaw maps stored values` passes the new params (`intention = "CALM"`, `sadhanaStart = 20600L`, `completed = setOf("om")`, `pendingCelebration = true`) and asserts them.
  - `fromRaw falls back on unknown language and null fields` passes `intention = "UNKNOWN_X"`, nulls for the rest — still equals DEFAULTS.
  - NEW `completion bookkeeping via snapshot copy`: setSnapshotForTest with completed+flag, assert reads.
  - NEW `setCurrentMantra semantics are representable`: snapshot copy with new mantra + start + flag false; assert.
  - NEW `intention round-trips by name`: `Intention.entries.forEach { assertEquals(it, UserPrefs.Snapshot.fromRaw(null, null, null, null, it.name, null, null, null).selectedIntention) }` — ADJUST the call shape to the final fromRaw signature; keep one assertion per enum value.
- [ ] **5.3** Build + focused tests; full suite → **64/64** (61 + 3 net new UserPrefs). Commit (combined T4+T5) `feat(progress): ProgressRepository + UserPrefs progress fields` + push.

## Task 6: Onboarding persists intention

**Files:** Modify `onboarding/OnboardingViewModel.kt`, `onboarding/IntentionScreen.kt`, `app/src/test/java/com/myniyam/app/onboarding/OnboardingViewModelTest.kt`.

- [ ] **6.1** VM gains:
```kotlin
fun persistIntention(context: Context) {
    val intention = selectedIntention ?: return
    viewModelScope.launch { UserPrefs.setIntention(context, intention) }
}
```
- [ ] **6.2** IntentionScreen: add `val ctx = LocalContext.current` (import androidx.compose.ui.platform.LocalContext) and change `onContinue = onContinue` wiring to:
```kotlin
onContinue = {
    vm.persistIntention(ctx)
    onContinue()
}
```
- [ ] **6.3** VM test +1:
```kotlin
@Test
fun `persistIntention without selection is a safe no-op`() {
    val vm = OnboardingViewModel()
    vm.persistIntention(null!!)  // NO — never write this. Instead:
}
```
FORBIDDEN draft above shown only to preempt it — the real added test (selection logic only, no Context):
```kotlin
@Test
fun `intention selection is exposed for persistence`() {
    val vm = OnboardingViewModel()
    vm.selectIntention(Intention.DHARMA)
    assertEquals(Intention.DHARMA, vm.selectedIntention)
}
```
- [ ] **6.4** Suite → **65/65**. Commit `feat(onboarding): persist intention for next-sadhana seeding` + push.

## Task 7: Overlay records reads + app warm-up (ENGINE TOUCH)

**Files:** Modify `overlay/OverlayManager.kt`, `NiyamApplication.kt`.

- [ ] **7.1** OverlayManager — the Continue handler (currently `continueBtn.setOnClickListener { hide(ctx) }`) becomes:
```kotlin
continueBtn.setOnClickListener {
    ProgressRepository.recordRead(ctx, mantra.id)
    hide(ctx)
}
```
(import `com.myniyam.app.progress.ProgressRepository`; `mantra` is already in scope in `show()`. recordRead is internally try/caught + executor-dispatched — the tap handler cannot block or throw.)
- [ ] **7.2** NiyamApplication warm-up thread gains `ProgressRepository.warmUp(this)` after the UserPrefs line (import it).
- [ ] **7.3** Build + full suite 65/65 (no behavior change JVM-side). Grep check: `grep -n "recordRead\|hide(ctx)" app/src/main/java/com/myniyam/app/overlay/OverlayManager.kt` — exactly one recordRead, before hide, inside the click listener only.
- [ ] **7.4** Commit `feat(engine): overlay Continue records the read; app warms Room` + push.

## Task 8: StarterMantras.priorityIds + NextSadhana (TDD)

**Files:** Modify `data/StarterMantras.kt`; Test `app/src/test/java/com/myniyam/app/progress/NextSadhanaTest.kt`; Create `app/src/main/java/com/myniyam/app/progress/NextSadhana.kt`.

- [ ] **8.1** StarterMantras gains (additive, below forIntention):
```kotlin
    /** Full 5-id priority list for an intention (SP-4 next-sadhana seeding). */
    fun priorityIds(intention: Intention): List<String> = PRIORITY.getValue(intention)
```
- [ ] **8.2** Failing test:
```kotlin
package com.myniyam.app.progress

import com.myniyam.app.data.Intention
import com.myniyam.app.data.MantraRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import java.io.File

class NextSadhanaTest {

    @Before
    fun loadCatalog() {
        MantraRepository.resetForTest()
        val json = File("src/main/assets/content/mantras.json").readText()
        check(MantraRepository.initFromJson(json))
    }

    @Test
    fun `excludes current and completed, keeps priority order`() {
        val result = NextSadhana.candidates(
            intention = Intention.CALM,
            completed = setOf("mahamrityunjaya"),
            currentId = "om-sahanavavatu"
        ).map { it.id }
        assertEquals(listOf("om-namah-shivaya", "gita-2-70", "twameva-mata"), result)
    }

    @Test
    fun `backfills from the catalog when the intention list is exhausted`() {
        val calmIds = setOf(
            "mahamrityunjaya", "om-sahanavavatu", "om-namah-shivaya", "gita-2-70", "twameva-mata"
        )
        val result = NextSadhana.candidates(
            intention = Intention.CALM,
            completed = calmIds,
            currentId = "om"
        ).map { it.id }
        assertEquals(3, result.size)
        result.forEach { id ->
            assertFalse("must not suggest completed/current", id in calmIds || id == "om")
        }
    }

    @Test
    fun `always returns at most three`() {
        assertEquals(3, NextSadhana.candidates(Intention.SADHANA, emptySet(), "gayatri").size)
    }
}
```
- [ ] **8.3** Run → FAILS. Implementation:
```kotlin
package com.myniyam.app.progress

import com.myniyam.app.data.Intention
import com.myniyam.app.data.Mantra
import com.myniyam.app.data.MantraRepository
import com.myniyam.app.data.StarterMantras

/**
 * Next-sadhana recommendations (spec §4): the user's intention priority
 * list minus completed/current, backfilled from the rest of the catalog
 * in catalog order. Always ≤3.
 */
object NextSadhana {

    fun candidates(intention: Intention, completed: Set<String>, currentId: String): List<Mantra> {
        val excluded = completed + currentId
        val fromIntention = StarterMantras.priorityIds(intention)
            .filterNot { it in excluded }
            .mapNotNull { MantraRepository.byId(it) }
        if (fromIntention.size >= 3) return fromIntention.take(3)
        val backfill = MantraRepository.all()
            .filterNot { it.id in excluded || fromIntention.any { m -> m.id == it.id } }
        return (fromIntention + backfill).take(3)
    }
}
```
- [ ] **8.4** Focused 3/3; suite → **68/68**. Commit `feat(progress): NextSadhana candidates — exclusion + backfill` + push.

## Task 9: Home rebuild

**Files:** Modify `ui/screens/HomeScreen.kt`, `res/values/strings.xml`.

- [ ] **9.1** Strings (add):
```xml
<!-- Home -->
<string name="home_overline">Your sadhana</string>
<string name="home_day_fmt">Day %1$d of %2$d</string>
<string name="home_streak_fmt">%1$d-day streak</string>
<string name="home_today_fmt">%1$d reads today</string>
<string name="home_protection_ok">Protection active</string>
<string name="home_protection_fix">Needs attention — tap to fix</string>
```
- [ ] **9.2** Replace `HomeScreen.kt` (signature gains `onFixProtection: () -> Unit`):
```kotlin
package com.myniyam.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.myniyam.app.R
import com.myniyam.app.data.CurrentSadhana
import com.myniyam.app.data.MantraRepository
import com.myniyam.app.permissions.PermissionChecker
import com.myniyam.app.progress.ProgressRepository
import com.myniyam.app.ui.theme.SaladGreen

@Composable
fun HomeScreen(onFixProtection: () -> Unit) {
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var refreshKey by remember { mutableStateOf(0) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshKey++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val stats by produceState(ProgressRepository.HomeStats(0, 14, 0, 0), refreshKey) {
        MantraRepository.ensureLoaded(ctx)
        value = ProgressRepository.homeStats(ctx)
    }
    val protectionOk = remember(refreshKey) { PermissionChecker.allPermissionsGranted(ctx) }
    val mantra = MantraRepository.displayMantra(CurrentSadhana.MANTRA_ID)

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.home_overline).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(mantra.canonicalName, style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        mantra.text.forScript(CurrentSadhana.LANGUAGE.script).lineSequence().first(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { if (stats.dayM == 0) 0f else stats.dayN.toFloat() / stats.dayM },
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.home_day_fmt, stats.dayN, stats.dayM),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatChip(stringResource(R.string.home_streak_fmt, stats.streak), highlight = stats.streak >= 2)
                StatChip(stringResource(R.string.home_today_fmt, stats.todayReads), highlight = false)
            }

            Spacer(Modifier.weight(1f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !protectionOk) { onFixProtection() }
                    .padding(vertical = 16.dp)
            ) {
                Box(
                    Modifier
                        .size(10.dp)
                        .background(if (protectionOk) SaladGreen else MaterialTheme.colorScheme.primary, CircleShape)
                )
                Spacer(Modifier.padding(start = 10.dp))
                Text(
                    text = stringResource(if (protectionOk) R.string.home_protection_ok else R.string.home_protection_fix),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun StatChip(text: String, highlight: Boolean) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = if (highlight) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .background(
                if (highlight) SaladGreen else MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}
```
(SaladGreen direct-token use is sanctioned: it IS the success color and colorScheme.secondary maps to it — implementer may use `colorScheme.secondary` instead for purity; pick one and be consistent. Old hardcoded green/red banner colors die here.)
- [ ] **9.3** AppNavHost: `HomeScreen()` call site becomes `HomeScreen(onFixProtection = { navController.navigate(NiyamRoutes.PERMISSION_USAGE) })` — done in T10 with the routes; for THIS task temporarily update the call in AppNavHost to pass the lambda (one line) so it compiles.
- [ ] **9.4** Build + suite 68/68. Commit `feat(home): brand home — mantra card, progress, streak, protection row` + push.

## Task 10: Celebration + NextSadhana screens + routing

**Files:** Create `ui/screens/CelebrationScreen.kt`, `ui/screens/NextSadhanaScreen.kt`; Modify `ui/AppNavHost.kt`, `res/values/strings.xml`.

- [ ] **10.1** Strings:
```xml
<!-- Celebration / next sadhana -->
<string name="celebration_overline">Journey complete</string>
<string name="celebration_title_fmt">%1$d days of %2$s</string>
<string name="celebration_body">You stayed with it. That discipline is the practice.</string>
<string name="celebration_next">Choose your next sadhana</string>
<string name="celebration_keep">Keep going with this mantra</string>
<string name="next_title">Your next sadhana</string>
```
- [ ] **10.2** CelebrationScreen.kt:
```kotlin
package com.myniyam.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.myniyam.app.R
import com.myniyam.app.data.CurrentSadhana
import com.myniyam.app.data.MantraRepository
import com.myniyam.app.ui.theme.SaladGreen

@Composable
fun CelebrationScreen(onChooseNext: () -> Unit, onKeepCurrent: () -> Unit) {
    val mantra = MantraRepository.displayMantra(CurrentSadhana.MANTRA_ID)
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = SaladGreen,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.celebration_overline).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(
                    R.string.celebration_title_fmt,
                    mantra.completionThresholdDays,
                    mantra.canonicalName
                ),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.celebration_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(40.dp))
            Button(
                onClick = onChooseNext,
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) { Text(stringResource(R.string.celebration_next), style = MaterialTheme.typography.labelLarge) }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onKeepCurrent,
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) { Text(stringResource(R.string.celebration_keep), style = MaterialTheme.typography.labelLarge) }
        }
    }
}
```
- [ ] **10.3** NextSadhanaScreen.kt:
```kotlin
package com.myniyam.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.myniyam.app.R
import com.myniyam.app.data.MantraRepository
import com.myniyam.app.data.UserPrefs
import com.myniyam.app.onboarding.SelectableCard
import com.myniyam.app.onboarding.mantraGist
import com.myniyam.app.progress.NextSadhana
import kotlinx.coroutines.launch

@Composable
fun NextSadhanaScreen(onDone: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    MantraRepository.ensureLoaded(ctx)
    val snap = UserPrefs.snapshot()
    val options = remember {
        NextSadhana.candidates(snap.selectedIntention, snap.completedMantraIds, snap.currentMantraId)
    }
    var selectedId by remember { mutableStateOf<String?>(null) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.next_title), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(20.dp))
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                options.forEach { mantra ->
                    SelectableCard(
                        text = mantra.canonicalName,
                        supportingText = mantraGist(mantra.meaning.en),
                        trailingChip = stringResource(R.string.onb_read_time_fmt, mantra.estimatedReadSeconds),
                        selected = selectedId == mantra.id,
                        onClick = { selectedId = mantra.id }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    val id = selectedId ?: return@Button
                    scope.launch {
                        UserPrefs.setCurrentMantra(ctx, id)
                        onDone()
                    }
                },
                enabled = selectedId != null,
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) { Text(stringResource(R.string.onb_continue), style = MaterialTheme.typography.labelLarge) }
            Spacer(Modifier.height(20.dp))
        }
    }
}
```
(`mantraGist` is currently `internal` in MantraPickerScreen.kt within the same module — accessible. SelectableCard is public in the onboarding package.)
- [ ] **10.4** AppNavHost: add routes `CELEBRATION = "celebration"`, `NEXT_SADHANA = "next_sadhana"`. HOME composable becomes:
```kotlin
composable(NiyamRoutes.HOME) {
    val ctx = LocalContext.current
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (com.myniyam.app.data.UserPrefs.snapshot().pendingCelebration) {
            navController.navigate(NiyamRoutes.CELEBRATION)
        }
    }
    HomeScreen(onFixProtection = { navController.navigate(NiyamRoutes.PERMISSION_USAGE) })
}
composable(NiyamRoutes.CELEBRATION) {
    val ctx = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    CelebrationScreen(
        onChooseNext = { navController.navigate(NiyamRoutes.NEXT_SADHANA) },
        onKeepCurrent = {
            scope.launch {
                com.myniyam.app.data.UserPrefs.setCurrentMantra(ctx, com.myniyam.app.data.UserPrefs.snapshot().currentMantraId)
                navController.popBackStack(NiyamRoutes.HOME, inclusive = false)
            }
        }
    )
}
composable(NiyamRoutes.NEXT_SADHANA) {
    NextSadhanaScreen(onDone = { navController.popBackStack(NiyamRoutes.HOME, inclusive = false) })
}
```
(Implementer: hoist the fully-qualified names into proper imports — UserPrefs, LaunchedEffect, rememberCoroutineScope, launch, CelebrationScreen, NextSadhanaScreen.)
- [ ] **10.5** Build + suite 68/68. Commit `feat(progress): celebration + next-sadhana picker wired` + push.

## Task 11: Integrated verification + SP-4 acceptance report

**Files:** Create `docs/superpowers/test-reports/2026-06-10-sp4-acceptance.md`; Modify `SESSION_LOG.md`.

- [ ] **11.1** Gates: full suite `--rerun-tasks` (expect 68/68 from XMLs), `assembleDebug`, APK size note.
- [ ] **11.2** Emulator best-effort (adb devices; if present: install, tap Continue on an overlay, screencap home; else defer).
- [ ] **11.3** Acceptance report scoring spec §6 criteria 1-6 (PASS/OPEN + evidence); SESSION_LOG top entry "SP-4 EXECUTED".
- [ ] **11.4** Commit `docs: SP-4 acceptance report` + push. Report status.

---

## Self-review

**Spec coverage:** rules §2 → ProgressMath/T3 + recordRead/T7 + completion/T4-5; Room §4 → T1-2; UserPrefs fields §3 → T5; intention persistence → T6; engine touch table §7 → T7 exactly; Home §4 → T9; celebration/picker §4 → T8+T10; acceptance §6 → tests in T3/T5/T6/T8 + T11 report. Browse-Library deliberately absent (spec non-goal). No gaps.

**Placeholder scan:** T6.3 contains an explicitly FORBIDDEN draft followed by the real test — kept as a guard, not a placeholder. T4/T5 combined-commit note is explicit instruction, not deferral. Clean otherwise.

**Type consistency:** `ProgressRepository.{warmUp,recordRead,homeStats,HomeStats}` consistent T4/T7/T9; `UserPrefs.{setIntention,markCompleted,setCurrentMantra(ctx,id,startEpochDay)}` consistent T5/T6/T10; `StarterMantras.priorityIds` T8 only; `NextSadhana.candidates(intention,completed,currentId)` T8/T10; `mantraGist`/`SelectableCard` reuse verified against SP-3 files; `HomeScreen(onFixProtection)` signature change matched in T9.3/T10.4. Test math: 55+6+3+1+3=68 ✓.
