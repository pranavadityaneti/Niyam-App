# Niyam — Onboarding & Brand Theme — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Apply the founder's brand (eggshell/bottle-green/pumpkin-orange, Playfair+Inter) app-wide including the unlock overlay, and build the persisted 4-step onboarding (intention → starter mantra → language → apps) that drives `CurrentSadhana` and `BlockList`, per [the SP-3 spec](../specs/2026-06-10-onboarding-and-brand-theme-design.md).

**Goal in plain English:** the app stops looking like a prototype and starts looking like Pranav's reference; a new user's choices during setup are saved and actually control what the blocker shows and blocks.

**Architecture:** Compose Material3 theme rebuilt from brand tokens (single source `ui/theme/Color.kt`); bundled variable fonts; overlay View XML restyled (logic untouched). Jetpack DataStore behind a `UserPrefs` object exposing a `@Volatile` snapshot (same warm-up pattern as `MantraRepository`); `CurrentSadhana`/`BlockList` read the snapshot with today's values as defaults. Onboarding = NavHost extension + one `OnboardingViewModel` + a shared step scaffold.

**Tech Stack:** Kotlin 2.0.21, Compose BOM 2024.12.01, Navigation-Compose, NEW: `androidx.datastore:datastore-preferences:1.1.1`, `androidx.lifecycle:lifecycle-viewmodel-compose` (2.8.7, same ref as lifecycle), variable TTFs (Playfair Display, Inter — OFL).

---

## Execution conventions

1. **Continuous full-control mode (Pranav, 2026-06-10):** no per-task STOP gates. Two-stage review (spec + quality) per task still mandatory. Stop only for BLOCKED/ambiguity.
2. Subagents: `model: opus` (founder policy). Direct commit+push to origin main per task (authorized workflow).
3. Verification: `./gradlew :app:compileDebugKotlin` / `:app:testDebugUnitTest --rerun-tasks` (count from XMLs) / `:app:assembleDebug`. Baseline at plan start: **43 tests green**, HEAD `2f661a0`.
4. Failures twice → ERRORS.md + escalate. No files outside each task's list.
5. **Brand law (memory `hindu-app-ui-references`):** palette/typography below are founder-locked. The `ui-ux-pro-max` skill (at `.claude/skills/ui-ux-pro-max/`) supplies UX rules — its generated palettes/fonts NEVER override the brand. UI tasks (8-13) MUST consult: `python3 .claude/skills/ui-ux-pro-max/scripts/search.py "<topic>" --domain ux` and `--stack jetpack-compose`, and apply the SKILL.md pre-delivery checklist (§Interaction, §Layout, §Accessibility) before committing.
6. Brand tokens (exact): Eggshell `#F5EBE1`, Card `#FFFDF8`, Hairline `#E8DCCD`, BottleGreen `#003223`, InkMuted `#33524A`, LabelMuted `#8A7F72`, PumpkinOrange `#FF6400`, OrangeTint `#FFF3EA`, SaladGreen `#8CC850`, ChipFill `#ECE0D2`. Orange = CTAs/selection/active-progress fills only; never small body text.

---

## File structure

```
gradle/libs.versions.toml                                  M  T1
app/build.gradle.kts                                       M  T1
app/src/main/res/font/playfair_display.ttf                 C  T2 (variable)
app/src/main/res/font/inter.ttf                            C  T2 (variable)
app/src/main/java/com/myniyam/app/ui/theme/Color.kt        M  T3
app/src/main/java/com/myniyam/app/ui/theme/Type.kt         M  T3
app/src/main/java/com/myniyam/app/ui/theme/Theme.kt        M  T3
app/src/main/res/values/colors.xml                         M  T4
app/src/main/res/layout/overlay_mantra.xml                 M  T4
app/src/main/java/com/myniyam/app/data/UserPrefs.kt        C  T5  (+test)
app/src/main/java/com/myniyam/app/NiyamApplication.kt      M  T6
app/src/main/java/com/myniyam/app/data/CurrentSadhana.kt   M  T6
app/src/main/java/com/myniyam/app/data/BlockList.kt        M  T6  (+test update)
app/src/main/java/com/myniyam/app/data/StarterMantras.kt   C  T7  (+test)
app/src/main/java/com/myniyam/app/onboarding/OnboardingViewModel.kt  C  T8 (+test)
app/src/main/java/com/myniyam/app/onboarding/OnboardingScaffold.kt   C  T9
app/src/main/java/com/myniyam/app/onboarding/IntentionScreen.kt      C  T9
app/src/main/java/com/myniyam/app/onboarding/MantraPickerScreen.kt   C  T10
app/src/main/java/com/myniyam/app/onboarding/LanguageScreen.kt       C  T11
app/src/main/java/com/myniyam/app/onboarding/AppsScreen.kt           C  T12
app/src/main/java/com/myniyam/app/ui/screens/WelcomeScreen.kt        M  T13
app/src/main/java/com/myniyam/app/ui/AppNavHost.kt                   M  T13
app/src/main/java/com/myniyam/app/MainActivity.kt                    M  T13
app/src/main/res/values/strings.xml                                  M  T9-T13
docs/superpowers/test-reports/                                       C  T14 (SP-3 report)
```

---

## Task 1: Dependencies (DataStore + ViewModel-Compose)

**Files:** Modify `gradle/libs.versions.toml`, `app/build.gradle.kts`.

- [ ] **1.1** In `libs.versions.toml` `[versions]` add `datastore = "1.1.1"`. In `[libraries]` add:
```toml
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleRuntimeKtx" }
```
- [ ] **1.2** In `app/build.gradle.kts` dependencies add:
```kotlin
implementation(libs.androidx.datastore.preferences)
implementation(libs.androidx.lifecycle.viewmodel.compose)
```
- [ ] **1.3** Run `./gradlew :app:compileDebugKotlin` → BUILD SUCCESSFUL. If `datastore 1.1.1` fails to resolve, check newest 1.1.x via the error's available-versions list, pin it, record in report.
- [ ] **1.4** Commit `chore: add DataStore + viewmodel-compose for onboarding persistence` (+ Co-Authored-By Claude Fable 5 trailer, as ALL commits in this plan) and push.

## Task 2: Bundle brand fonts

**Files:** Create `app/src/main/res/font/playfair_display.ttf`, `app/src/main/res/font/inter.ttf`.

- [ ] **2.1** Download the two variable fonts (Google Fonts repo, OFL):
```bash
mkdir -p app/src/main/res/font
curl -L -o app/src/main/res/font/playfair_display.ttf "https://raw.githubusercontent.com/google/fonts/main/ofl/playfairdisplay/PlayfairDisplay%5Bwght%5D.ttf"
curl -L -o app/src/main/res/font/inter.ttf "https://raw.githubusercontent.com/google/fonts/main/ofl/inter/Inter%5Bopsz%2Cwght%5D.ttf"
ls -la app/src/main/res/font/
```
Each file must be >100KB and start with a valid sfnt header (`file` reports TrueType). If a URL 404s: find the current path with `curl -s https://api.github.com/repos/google/fonts/contents/ofl/playfairdisplay` (resp. `ofl/inter`) and use the listed `download_url` for the `[wght]`/`[opsz,wght]` TTF. Resource names MUST be lowercase a-z0-9_ (hence the renames).
- [ ] **2.2** Verify license: both are OFL (no APK attribution requirement; note in commit message).
- [ ] **2.3** `./gradlew :app:assembleDebug` → BUILD SUCCESSFUL (fonts are valid resources). Record APK size delta in the report (budget: ≤1MB growth).
- [ ] **2.4** Commit `feat(theme): bundle Playfair Display + Inter variable fonts (OFL)` + push.

## Task 3: Brand Compose theme

**Files:** Modify `ui/theme/Color.kt`, `ui/theme/Type.kt`, `ui/theme/Theme.kt`.

- [ ] **3.1** Replace `Color.kt` content:
```kotlin
package com.myniyam.app.ui.theme

import androidx.compose.ui.graphics.Color

// Founder-locked brand tokens (SP-3 spec §4). Single source of truth —
// screens must consume MaterialTheme.colorScheme, never these directly.
val Eggshell = Color(0xFFF5EBE1)
val CardWarm = Color(0xFFFFFDF8)
val Hairline = Color(0xFFE8DCCD)
val BottleGreen = Color(0xFF003223)
val InkMuted = Color(0xFF33524A)
val LabelMuted = Color(0xFF8A7F72)
val PumpkinOrange = Color(0xFFFF6400)
val OrangeTint = Color(0xFFFFF3EA)
val SaladGreen = Color(0xFF8CC850)
val ChipFill = Color(0xFFECE0D2)
```
- [ ] **3.2** Replace `Type.kt` content:
```kotlin
package com.myniyam.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.myniyam.app.R

val PlayfairFamily = FontFamily(
    Font(R.font.playfair_display, FontWeight.Medium),
    Font(R.font.playfair_display, FontWeight.SemiBold)
)

val InterFamily = FontFamily(
    Font(R.font.inter, FontWeight.Normal),
    Font(R.font.inter, FontWeight.Medium)
)

// Variable fonts: weight axes resolve on API 28+; API 26-27 render the
// default instance — accepted v1 limitation (spec §11).
val NiyamTypography = Typography(
    displayMedium = TextStyle(fontFamily = PlayfairFamily, fontWeight = FontWeight.SemiBold, fontSize = 40.sp),
    headlineMedium = TextStyle(fontFamily = PlayfairFamily, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Medium, fontSize = 18.sp),
    bodyLarge = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelSmall = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 1.5.sp)
)
```
- [ ] **3.3** Replace `Theme.kt` content (light-only per spec; the `darkTheme` parameter is dropped deliberately):
```kotlin
package com.myniyam.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NiyamLightColors = lightColorScheme(
    primary = PumpkinOrange,
    onPrimary = Color.White,
    background = Eggshell,
    onBackground = BottleGreen,
    surface = CardWarm,
    onSurface = BottleGreen,
    surfaceVariant = ChipFill,
    onSurfaceVariant = InkMuted,
    secondary = SaladGreen,
    onSecondary = BottleGreen,
    outline = Hairline,
    outlineVariant = Hairline
)

@Composable
fun NiyamTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NiyamLightColors,
        typography = NiyamTypography,
        content = content
    )
}
```
- [ ] **3.4** `MainActivity` calls `NiyamTheme { ... }` with no args already — but the old signature had `darkTheme` default param, so this compiles without call-site changes. Verify: `./gradlew :app:assembleDebug` → BUILD SUCCESSFUL; full suite still 43/43.
- [ ] **3.5** Commit `feat(theme): brand Compose theme — eggshell/bottle-green/orange + Playfair/Inter` + push.

## Task 4: Overlay restyle (View XML — logic untouched)

**Files:** Modify `app/src/main/res/values/colors.xml`, `app/src/main/res/layout/overlay_mantra.xml`.

- [ ] **4.1** Replace `colors.xml` content:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="black">#FF000000</color>
    <color name="white">#FFFFFFFF</color>
    <color name="overlay_scrim">#FFF5EBE1</color>
    <color name="overlay_ink">#FF003223</color>
    <color name="overlay_ink_muted">#FF33524A</color>
    <color name="overlay_label_muted">#FF8A7F72</color>
    <color name="overlay_roman">#FF6B8377</color>
    <color name="overlay_chip_fill">#FFECE0D2</color>
    <color name="overlay_chip_text">#FF5F5648</color>
    <color name="overlay_cta">#FFFF6400</color>
</resources>
```
(`overlay_scrim` becomes opaque Eggshell — same resource name so nothing else changes.)
- [ ] **4.2** Replace `overlay_mantra.xml` content (same ids — `OverlayManager` binds them unchanged):
```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/overlay_scrim"
    android:clickable="true"
    android:focusable="true">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:paddingStart="28dp"
        android:paddingEnd="28dp"
        android:paddingTop="64dp"
        android:paddingBottom="28dp">

        <TextView
            android:id="@+id/overlay_label"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:letterSpacing="0.12"
            android:text="@string/overlay_label"
            android:textAllCaps="true"
            android:textColor="@color/overlay_label_muted"
            android:textSize="11sp" />

        <TextView
            android:id="@+id/overlay_devanagari"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="24dp"
            android:fontFamily="serif"
            android:lineSpacingMultiplier="1.6"
            android:textColor="@color/overlay_ink"
            android:textSize="22sp"
            android:autoSizeTextType="uniform"
            android:autoSizeMinTextSize="16sp"
            android:autoSizeMaxTextSize="24sp"
            android:maxLines="14" />

        <TextView
            android:id="@+id/overlay_transliteration"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="14dp"
            android:textStyle="italic"
            android:lineSpacingMultiplier="1.4"
            android:textColor="@color/overlay_roman"
            android:textSize="12sp"
            android:maxLines="6"
            android:ellipsize="end" />

        <TextView
            android:id="@+id/overlay_meaning"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="14dp"
            android:lineSpacingMultiplier="1.5"
            android:textColor="@color/overlay_ink_muted"
            android:textSize="13sp"
            android:maxLines="6"
            android:ellipsize="end" />

        <Space
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1" />

        <TextView
            android:id="@+id/overlay_countdown"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center_horizontal"
            android:layout_marginBottom="12dp"
            android:background="@drawable/bg_overlay_chip"
            android:paddingStart="16dp"
            android:paddingEnd="16dp"
            android:paddingTop="6dp"
            android:paddingBottom="6dp"
            android:textColor="@color/overlay_chip_text"
            android:textSize="12sp" />

        <Button
            android:id="@+id/overlay_continue"
            android:layout_width="match_parent"
            android:layout_height="52dp"
            android:background="@drawable/bg_overlay_cta"
            android:enabled="false"
            android:stateListAnimator="@null"
            android:text="@string/overlay_continue"
            android:textAllCaps="false"
            android:textColor="@color/white"
            android:textSize="15sp" />

    </LinearLayout>

</FrameLayout>
```
NOTE the layout adds one NEW id (`overlay_label`) which `OverlayManager` does not bind yet — it shows the static string `@string/overlay_label`. Binding the dynamic mantra name happens in **Task 6** (one line). Two drawables + one string are needed:
- [ ] **4.3** Create `app/src/main/res/drawable/bg_overlay_chip.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <solid android:color="@color/overlay_chip_fill" />
    <corners android:radius="999dp" />
</shape>
```
Create `app/src/main/res/drawable/bg_overlay_cta.xml` (orange pill, dimmed when disabled):
```xml
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_enabled="false">
        <shape android:shape="rectangle">
            <solid android:color="#66FF6400" />
            <corners android:radius="999dp" />
        </shape>
    </item>
    <item>
        <shape android:shape="rectangle">
            <solid android:color="@color/overlay_cta" />
            <corners android:radius="999dp" />
        </shape>
    </item>
</selector>
```
Add to `strings.xml`: `<string name="overlay_label">Your sadhana</string>`
- [ ] **4.4** `./gradlew :app:assembleDebug` → BUILD SUCCESSFUL; suite 43/43 (`OverlayManager` untouched this task).
- [ ] **4.5** Commit `feat(overlay): restyle to brand — eggshell, serif ink, orange pill CTA` + push.

## Task 5: UserPrefs (DataStore + snapshot, TDD)

**Files:** Test `app/src/test/java/com/myniyam/app/data/UserPrefsTest.kt`, Create `app/src/main/java/com/myniyam/app/data/UserPrefs.kt`.

- [ ] **5.1** Failing test (pure JVM — tests the Snapshot mapping core, not DataStore I/O):
```kotlin
package com.myniyam.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class UserPrefsTest {

    @Test
    fun `defaults match the pre-SP3 hardcoded behavior`() {
        val s = UserPrefs.Snapshot.DEFAULTS
        assertFalse(s.onboardingComplete)
        assertEquals("gayatri", s.currentMantraId)
        assertEquals(DisplayLanguage.DEVANAGARI_SANSKRIT, s.displayLanguage)
        assertEquals(
            setOf("com.instagram.android", "com.facebook.katana", "com.google.android.youtube"),
            s.blockedPackages
        )
    }

    @Test
    fun `fromRaw maps stored values`() {
        val s = UserPrefs.Snapshot.fromRaw(
            onboardingComplete = true,
            mantraId = "mahamrityunjaya",
            language = "TAMIL",
            blocked = setOf("com.instagram.android")
        )
        assertEquals(true, s.onboardingComplete)
        assertEquals("mahamrityunjaya", s.currentMantraId)
        assertEquals(DisplayLanguage.TAMIL, s.displayLanguage)
        assertEquals(setOf("com.instagram.android"), s.blockedPackages)
    }

    @Test
    fun `fromRaw falls back on unknown language and null fields`() {
        val s = UserPrefs.Snapshot.fromRaw(
            onboardingComplete = null,
            mantraId = null,
            language = "KLINGON",
            blocked = null
        )
        assertEquals(UserPrefs.Snapshot.DEFAULTS, s)
    }

    @Test
    fun `snapshot is replaced atomically for tests`() {
        UserPrefs.setSnapshotForTest(UserPrefs.Snapshot.DEFAULTS.copy(currentMantraId = "om"))
        assertEquals("om", UserPrefs.snapshot().currentMantraId)
        UserPrefs.resetForTest()
        assertEquals(UserPrefs.Snapshot.DEFAULTS, UserPrefs.snapshot())
    }
}
```
- [ ] **5.2** Run focused → FAILS `Unresolved reference 'UserPrefs'`.
- [ ] **5.3** Implementation:
```kotlin
package com.myniyam.app.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.niyamDataStore by preferencesDataStore(name = "niyam_user_prefs")

/**
 * User choices persisted via DataStore, exposed to engine code as a
 * synchronous @Volatile snapshot (spec §7). Warm-up: ensureLoaded() at
 * Application start, same pattern as MantraRepository.
 */
object UserPrefs {

    private const val TAG = "UserPrefs"

    private val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    private val KEY_CURRENT_MANTRA_ID = stringPreferencesKey("current_mantra_id")
    private val KEY_DISPLAY_LANGUAGE = stringPreferencesKey("display_language")
    private val KEY_BLOCKED_PACKAGES = stringSetPreferencesKey("blocked_packages")

    data class Snapshot(
        val onboardingComplete: Boolean,
        val currentMantraId: String,
        val displayLanguage: DisplayLanguage,
        val blockedPackages: Set<String>
    ) {
        companion object {
            val DEFAULTS = Snapshot(
                onboardingComplete = false,
                currentMantraId = "gayatri",
                displayLanguage = DisplayLanguage.DEVANAGARI_SANSKRIT,
                blockedPackages = BlockList.DEFAULT_PACKAGES
            )

            fun fromRaw(
                onboardingComplete: Boolean?,
                mantraId: String?,
                language: String?,
                blocked: Set<String>?
            ): Snapshot = Snapshot(
                onboardingComplete = onboardingComplete ?: DEFAULTS.onboardingComplete,
                currentMantraId = mantraId?.takeIf { it.isNotBlank() } ?: DEFAULTS.currentMantraId,
                displayLanguage = language?.let { raw ->
                    DisplayLanguage.entries.firstOrNull { it.name == raw }
                } ?: DEFAULTS.displayLanguage,
                blockedPackages = blocked?.takeIf { it.isNotEmpty() } ?: DEFAULTS.blockedPackages
            )
        }
    }

    @Volatile private var current: Snapshot = Snapshot.DEFAULTS
    @Volatile private var loadAttempted = false

    fun snapshot(): Snapshot = current

    /** Blocking read of persisted prefs into the snapshot. Call from the warm-up thread. */
    fun ensureLoaded(context: Context) {
        if (loadAttempted) return
        synchronized(this) {
            if (loadAttempted) return
            loadAttempted = true
            try {
                val p = runBlocking { context.niyamDataStore.data.first() }
                current = Snapshot.fromRaw(
                    onboardingComplete = p[KEY_ONBOARDING_COMPLETE],
                    mantraId = p[KEY_CURRENT_MANTRA_ID],
                    language = p[KEY_DISPLAY_LANGUAGE],
                    blocked = p[KEY_BLOCKED_PACKAGES]
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load prefs; using defaults", e)
                current = Snapshot.DEFAULTS
            }
        }
    }

    suspend fun setCurrentMantra(context: Context, mantraId: String) {
        context.niyamDataStore.edit { it[KEY_CURRENT_MANTRA_ID] = mantraId }
        current = current.copy(currentMantraId = mantraId)
    }

    suspend fun setDisplayLanguage(context: Context, language: DisplayLanguage) {
        context.niyamDataStore.edit { it[KEY_DISPLAY_LANGUAGE] = language.name }
        current = current.copy(displayLanguage = language)
    }

    suspend fun setBlockedPackages(context: Context, packages: Set<String>) {
        context.niyamDataStore.edit { it[KEY_BLOCKED_PACKAGES] = packages }
        current = current.copy(blockedPackages = packages)
    }

    suspend fun setOnboardingComplete(context: Context) {
        context.niyamDataStore.edit { it[KEY_ONBOARDING_COMPLETE] = true }
        current = current.copy(onboardingComplete = true)
    }

    fun setSnapshotForTest(snapshot: Snapshot) { current = snapshot }

    fun resetForTest() { current = Snapshot.DEFAULTS; loadAttempted = false }
}
```
NOTE: references `BlockList.DEFAULT_PACKAGES` which exists only after Task 6's rename — to keep THIS task compiling, use the literal set here and switch to the constant in Task 6:
```kotlin
blockedPackages = setOf("com.instagram.android", "com.facebook.katana", "com.google.android.youtube")
```
- [ ] **5.4** Focused test → 4/4; full suite → **47/47**.
- [ ] **5.5** Commit `feat(data): add UserPrefs — DataStore-backed user choices with sync snapshot` + push.

## Task 6: Wire engine to the snapshot (CurrentSadhana, BlockList, warm-up, overlay label)

**Files:** Modify `CurrentSadhana.kt`, `BlockList.kt`, `BlockListTest.kt`, `NiyamApplication.kt`, `OverlayManager.kt`, `UserPrefs.kt` (constant swap).

- [ ] **6.1** Replace `BlockList.kt`:
```kotlin
package com.myniyam.app.data

object BlockList {

    /** First-run default + prefs-load fallback — the pre-SP3 hardcoded set. */
    val DEFAULT_PACKAGES: Set<String> = setOf(
        "com.instagram.android",
        "com.facebook.katana",
        "com.google.android.youtube"
    )

    fun matches(packageName: String): Boolean =
        packageName in UserPrefs.snapshot().blockedPackages
}
```
- [ ] **6.2** In `UserPrefs.kt` swap the literal default set for `BlockList.DEFAULT_PACKAGES`.
- [ ] **6.3** Update `BlockListTest.kt` — add `@Before`/`@After` reset and two override tests, keep all 8 existing assertions valid under DEFAULTS:
```kotlin
package com.myniyam.app.data

import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BlockListTest {

    @Before fun setUp() = UserPrefs.resetForTest()
    @After fun tearDown() = UserPrefs.resetForTest()

    @Test fun `matches returns true for instagram package`() { assertTrue(BlockList.matches("com.instagram.android")) }
    @Test fun `matches returns true for facebook main app package`() { assertTrue(BlockList.matches("com.facebook.katana")) }
    @Test fun `matches returns true for youtube main app package`() { assertTrue(BlockList.matches("com.google.android.youtube")) }
    @Test fun `matches returns false for messenger`() { assertFalse(BlockList.matches("com.facebook.orca")) }
    @Test fun `matches returns false for youtube music`() { assertFalse(BlockList.matches("com.google.android.apps.youtube.music")) }
    @Test fun `matches returns false for facebook lite`() { assertFalse(BlockList.matches("com.facebook.lite")) }
    @Test fun `matches returns false for chrome`() { assertFalse(BlockList.matches("com.android.chrome")) }
    @Test fun `matches returns false for empty string`() { assertFalse(BlockList.matches("")) }

    @Test
    fun `user-selected set overrides defaults`() {
        UserPrefs.setSnapshotForTest(
            UserPrefs.Snapshot.DEFAULTS.copy(blockedPackages = setOf("com.twitter.android"))
        )
        assertTrue(BlockList.matches("com.twitter.android"))
        assertFalse(BlockList.matches("com.instagram.android"))
    }

    @Test
    fun `defaults apply before any prefs load`() {
        assertTrue(BlockList.matches("com.instagram.android"))
    }
}
```
- [ ] **6.4** Replace `CurrentSadhana.kt`:
```kotlin
package com.myniyam.app.data

/**
 * The user's active sadhana selection, backed by UserPrefs (SP-3).
 * Defaults (pre-onboarding) match the previous hardcoded values.
 */
object CurrentSadhana {
    val MANTRA_ID: String get() = UserPrefs.snapshot().currentMantraId
    val LANGUAGE: DisplayLanguage get() = UserPrefs.snapshot().displayLanguage
}
```
(`const val` → `val get()` — call sites compile unchanged.)
- [ ] **6.5** `NiyamApplication.onCreate` warm-up line becomes:
```kotlin
Thread {
    MantraRepository.ensureLoaded(this)
    UserPrefs.ensureLoaded(this)
}.start()
```
(add `import com.myniyam.app.data.UserPrefs`).
- [ ] **6.6** In `OverlayManager.show()` bind the new label (after the meaning binding line):
```kotlin
view.findViewById<TextView>(R.id.overlay_label).text =
    ctx.getString(R.string.overlay_label_fmt, mantra.canonicalName)
```
Add to `strings.xml`: `<string name="overlay_label_fmt">Your sadhana · %1$s</string>` and REMOVE the now-unused static `overlay_label` string from Task 4 (the XML keeps a placeholder `android:text="@string/overlay_label_fmt"`? NO — set `android:text=""` in the XML for `overlay_label` instead, and delete the `overlay_label` string resource).
- [ ] **6.7** Full suite → **49/49** (47 + 2 new BlockList tests). `assembleDebug` green.
- [ ] **6.8** Commit `feat(engine): CurrentSadhana + BlockList read UserPrefs snapshot; overlay label bound` + push.

## Task 7: StarterMantras curation (TDD)

**Files:** Test `app/src/test/java/com/myniyam/app/data/StarterMantrasTest.kt`, Create `app/src/main/java/com/myniyam/app/data/StarterMantras.kt`.

- [ ] **7.1** Failing test:
```kotlin
package com.myniyam.app.data

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

class StarterMantrasTest {

    @Before
    fun loadRealCatalog() {
        MantraRepository.resetForTest()
        val json = File("src/main/assets/content/mantras.json").readText()
        check(MantraRepository.initFromJson(json))
    }

    @Test
    fun `each intention returns its top-3 curated entries in order`() {
        assertEquals(listOf("gita-2-47", "gita-6-5", "gita-2-14"), ids(Intention.FOCUS))
        assertEquals(listOf("mahamrityunjaya", "om-sahanavavatu", "om-namah-shivaya"), ids(Intention.CALM))
        assertEquals(listOf("gayatri", "vakratunda", "saraswati-vandana"), ids(Intention.SADHANA))
        assertEquals(listOf("gita-4-7-8", "gita-18-66", "gita-3-35"), ids(Intention.DHARMA))
        assertEquals(
            listOf("hanuman-chalisa-opening", "vishnu-sahasranama-opening", "lalita-sahasranama-opening"),
            ids(Intention.DEVOTION)
        )
    }

    @Test
    fun `unknown ids are dropped and backfilled from the priority list`() {
        // The full priority lists have 5 entries; if the top entry vanished from
        // the catalog, position 4 backfills. Simulate via a reduced catalog.
        MantraRepository.resetForTest()
        val json = File("src/main/assets/content/mantras.json").readText()
            .replace("\"id\": \"gita-2-47\"", "\"id\": \"renamed-away\"")
        check(MantraRepository.initFromJson(json))
        assertEquals(listOf("gita-6-5", "gita-2-14", "asato-ma"), ids(Intention.FOCUS))
    }

    private fun ids(intention: Intention) =
        StarterMantras.forIntention(intention).map { it.id }
}
```
- [ ] **7.2** Run → FAILS unresolved `StarterMantras`.
- [ ] **7.3** Implementation:
```kotlin
package com.myniyam.app.data

/**
 * Deterministic per-intention starter recommendations (SP-3 spec §6) —
 * the brief's own groupings as ordered priority lists; the picker shows
 * the first 3 present in the catalog. Also seeds SP-4's next-sadhana logic.
 */
object StarterMantras {

    private val PRIORITY: Map<Intention, List<String>> = mapOf(
        Intention.FOCUS to listOf("gita-2-47", "gita-6-5", "gita-2-14", "asato-ma", "gita-6-6"),
        Intention.CALM to listOf("mahamrityunjaya", "om-sahanavavatu", "om-namah-shivaya", "gita-2-70", "twameva-mata"),
        Intention.SADHANA to listOf("gayatri", "vakratunda", "saraswati-vandana", "guru-brahma", "hare-krishna"),
        Intention.DHARMA to listOf("gita-4-7-8", "gita-18-66", "gita-3-35", "purusha-suktam", "nasadiya-suktam"),
        Intention.DEVOTION to listOf(
            "hanuman-chalisa-opening", "vishnu-sahasranama-opening",
            "lalita-sahasranama-opening", "krishna-ashtakam", "ram-raksha-opening"
        )
    )

    fun forIntention(intention: Intention): List<Mantra> =
        PRIORITY.getValue(intention).mapNotNull { MantraRepository.byId(it) }.take(3)
}
```
- [ ] **7.4** Focused → 2/2; full suite → **51/51**.
- [ ] **7.5** Commit `feat(data): StarterMantras — deterministic per-intention curation` + push.

## Task 8: OnboardingViewModel (TDD on selection logic)

**Files:** Test `app/src/test/java/com/myniyam/app/onboarding/OnboardingViewModelTest.kt`, Create `app/src/main/java/com/myniyam/app/onboarding/OnboardingViewModel.kt`.

- [ ] **8.1** Failing test:
```kotlin
package com.myniyam.app.onboarding

import com.myniyam.app.data.BlockList
import com.myniyam.app.data.Intention
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingViewModelTest {

    @Test
    fun `intention continue gating`() {
        val vm = OnboardingViewModel()
        assertFalse(vm.canContinueFromIntention())
        vm.selectIntention(Intention.CALM)
        assertTrue(vm.canContinueFromIntention())
        assertEquals(Intention.CALM, vm.selectedIntention)
    }

    @Test
    fun `mantra continue gating`() {
        val vm = OnboardingViewModel()
        assertFalse(vm.canContinueFromMantra())
        vm.selectMantra("mahamrityunjaya")
        assertTrue(vm.canContinueFromMantra())
    }

    @Test
    fun `apps default to BlockList defaults and toggle`() {
        val vm = OnboardingViewModel()
        assertEquals(BlockList.DEFAULT_PACKAGES, vm.selectedPackages)
        vm.togglePackage("com.twitter.android")
        assertTrue("com.twitter.android" in vm.selectedPackages)
        vm.togglePackage("com.instagram.android")
        assertFalse("com.instagram.android" in vm.selectedPackages)
    }

    @Test
    fun `cannot continue from apps with empty selection`() {
        val vm = OnboardingViewModel()
        BlockList.DEFAULT_PACKAGES.forEach { vm.togglePackage(it) }
        assertFalse(vm.canContinueFromApps())
    }
}
```
- [ ] **8.2** Run → FAILS.
- [ ] **8.3** Implementation:
```kotlin
package com.myniyam.app.onboarding

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myniyam.app.data.BlockList
import com.myniyam.app.data.DisplayLanguage
import com.myniyam.app.data.Intention
import com.myniyam.app.data.UserPrefs
import kotlinx.coroutines.launch

/** Holds onboarding selections across the 4 steps and persists each on confirm (spec §5/§7). */
class OnboardingViewModel : ViewModel() {

    var selectedIntention: Intention? by mutableStateOf(null)
        private set
    var selectedMantraId: String? by mutableStateOf(null)
        private set
    var selectedLanguage: DisplayLanguage by mutableStateOf(DisplayLanguage.ENGLISH)
        private set
    var selectedPackages: Set<String> by mutableStateOf(BlockList.DEFAULT_PACKAGES)
        private set

    fun selectIntention(intention: Intention) { selectedIntention = intention }
    fun selectMantra(id: String) { selectedMantraId = id }
    fun selectLanguage(language: DisplayLanguage) { selectedLanguage = language }
    fun togglePackage(pkg: String) {
        selectedPackages = if (pkg in selectedPackages) selectedPackages - pkg else selectedPackages + pkg
    }

    fun canContinueFromIntention() = selectedIntention != null
    fun canContinueFromMantra() = selectedMantraId != null
    fun canContinueFromApps() = selectedPackages.isNotEmpty()

    fun persistMantra(context: Context) {
        val id = selectedMantraId ?: return
        viewModelScope.launch { UserPrefs.setCurrentMantra(context, id) }
    }

    fun persistLanguage(context: Context) {
        viewModelScope.launch { UserPrefs.setDisplayLanguage(context, selectedLanguage) }
    }

    fun persistApps(context: Context) {
        viewModelScope.launch { UserPrefs.setBlockedPackages(context, selectedPackages) }
    }

    fun persistOnboardingComplete(context: Context) {
        viewModelScope.launch { UserPrefs.setOnboardingComplete(context) }
    }
}
```
- [ ] **8.4** Focused 4/4; full suite **55/55**. (Compose `mutableStateOf` in JVM tests works — it's `runtime`, no Android framework.)
- [ ] **8.5** Commit `feat(onboarding): OnboardingViewModel with selection gating + persistence` + push.

## Task 9: OnboardingScaffold + IntentionScreen

**Files:** Create `onboarding/OnboardingScaffold.kt`, `onboarding/IntentionScreen.kt`; Modify `res/values/strings.xml`.

UI tasks 9-13: consult `ui-ux-pro-max` (`--domain ux "touch targets selection cards"` and `--stack jetpack-compose`), apply its pre-delivery checklist; brand law overrides skill palette suggestions.

- [ ] **9.1** Strings (add):
```xml
<!-- Onboarding -->
<string name="onb_step_fmt">Step %1$d of 4</string>
<string name="onb_continue">Continue</string>
<string name="onb_intention_title">Why are you here?</string>
<string name="onb_intention_focus">Focus better, scroll less</string>
<string name="onb_intention_calm">Calm a busy mind</string>
<string name="onb_intention_sadhana">Start a daily sadhana</string>
<string name="onb_intention_dharma">Feel more connected to dharma</string>
<string name="onb_intention_devotion">Deepen my devotion</string>
```
- [ ] **9.2** `OnboardingScaffold.kt` — the shared step frame (overline, serif title, content slot, bottom orange pill):
```kotlin
package com.myniyam.app.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.myniyam.app.R

@Composable
fun OnboardingScaffold(
    step: Int,
    title: String,
    ctaEnabled: Boolean,
    onContinue: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.onb_step_fmt, step).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Text(text = title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(20.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) { content() }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onContinue,
                enabled = ctaEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(bottom = 0.dp)
            ) {
                Text(stringResource(R.string.onb_continue), style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
```
- [ ] **9.3** Shared selectable card (same file, below the scaffold):
```kotlin
@Composable
fun SelectableCard(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    supportingText: String? = null,
    trailingChip: String? = null
) {
    val border = if (selected)
        androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    else
        androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
    val container = if (selected) com.myniyam.app.ui.theme.OrangeTint else MaterialTheme.colorScheme.surface

    androidx.compose.material3.Card(
        onClick = onClick,
        border = border,
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = container),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(text, style = MaterialTheme.typography.bodyLarge)
                if (supportingText != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        supportingText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
            if (trailingChip != null) {
                Text(
                    trailingChip,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .androidx.compose.foundation.background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            androidx.compose.foundation.shape.RoundedCornerShape(999.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            if (selected) {
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
        }
    }
}
```
IMPLEMENTER NOTE: the `.androidx.compose.foundation.background(...)` chain above is pseudo-syntax from plan compression — write it as a normal `Modifier.background(color, shape)` with proper imports, and add `material-icons` via the existing Compose BOM (`androidx.compose.material:material-icons-extended` NOT needed — `Icons.Default.Check` is in `material-icons-core`, already a BOM transitive of material3; if unresolved, add `implementation("androidx.compose.material:material-icons-core")` under the BOM). Clean imports; no fully-qualified inline references in the final code.
- [ ] **9.4** `IntentionScreen.kt`:
```kotlin
package com.myniyam.app.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.myniyam.app.R
import com.myniyam.app.data.Intention

@Composable
fun IntentionScreen(vm: OnboardingViewModel, onContinue: () -> Unit) {
    val options = listOf(
        Intention.FOCUS to stringResource(R.string.onb_intention_focus),
        Intention.CALM to stringResource(R.string.onb_intention_calm),
        Intention.SADHANA to stringResource(R.string.onb_intention_sadhana),
        Intention.DHARMA to stringResource(R.string.onb_intention_dharma),
        Intention.DEVOTION to stringResource(R.string.onb_intention_devotion)
    )
    OnboardingScaffold(
        step = 1,
        title = stringResource(R.string.onb_intention_title),
        ctaEnabled = vm.canContinueFromIntention(),
        onContinue = onContinue
    ) {
        options.forEach { (intention, label) ->
            SelectableCard(
                text = label,
                selected = vm.selectedIntention == intention,
                onClick = { vm.selectIntention(intention) }
            )
        }
    }
}
```
- [ ] **9.5** `assembleDebug` green; suite 55/55.
- [ ] **9.6** Commit `feat(onboarding): step scaffold, selectable card, intention screen` + push.

## Task 10: MantraPickerScreen

**Files:** Create `onboarding/MantraPickerScreen.kt`; Modify `strings.xml`.

- [ ] **10.1** Strings: `<string name="onb_mantra_title">Pick your starter mantra</string>` and `<string name="onb_read_time_fmt">~%1$d sec</string>`.
- [ ] **10.2** Screen:
```kotlin
package com.myniyam.app.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.myniyam.app.R
import com.myniyam.app.data.Intention
import com.myniyam.app.data.MantraRepository
import com.myniyam.app.data.StarterMantras

@Composable
fun MantraPickerScreen(vm: OnboardingViewModel, onContinue: () -> Unit) {
    val ctx = LocalContext.current
    MantraRepository.ensureLoaded(ctx)
    val intention = vm.selectedIntention ?: Intention.SADHANA
    val options = StarterMantras.forIntention(intention)

    OnboardingScaffold(
        step = 2,
        title = stringResource(R.string.onb_mantra_title),
        ctaEnabled = vm.canContinueFromMantra(),
        onContinue = {
            vm.persistMantra(ctx)
            onContinue()
        }
    ) {
        options.forEach { mantra ->
            SelectableCard(
                text = mantra.canonicalName,
                supportingText = mantra.meaning.en.substringBefore(". ") + ".",
                trailingChip = stringResource(R.string.onb_read_time_fmt, mantra.estimatedReadSeconds),
                selected = vm.selectedMantraId == mantra.id,
                onClick = { vm.selectMantra(mantra.id) }
            )
        }
    }
}
```
- [ ] **10.3** Build green; suite stable. Commit `feat(onboarding): starter-mantra picker (curated 3 by intention)` + push.

## Task 11: LanguageScreen

**Files:** Create `onboarding/LanguageScreen.kt`; Modify `strings.xml`.

- [ ] **11.1** Strings: `<string name="onb_language_title">Your language</string>`.
- [ ] **11.2** Screen (native-script labels are code-level constants — they are scripts, not translatable copy):
```kotlin
package com.myniyam.app.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.myniyam.app.R
import com.myniyam.app.data.DisplayLanguage

private val LANGUAGE_LABELS: List<Triple<DisplayLanguage, String, String>> = listOf(
    Triple(DisplayLanguage.ENGLISH, "English", "Roman script"),
    Triple(DisplayLanguage.HINDI, "हिन्दी", "Hindi"),
    Triple(DisplayLanguage.DEVANAGARI_SANSKRIT, "संस्कृतम् — देवनागरी", "Sanskrit (Devanagari)"),
    Triple(DisplayLanguage.MARATHI, "मराठी", "Marathi"),
    Triple(DisplayLanguage.TELUGU, "తెలుగు", "Telugu"),
    Triple(DisplayLanguage.TAMIL, "தமிழ்", "Tamil"),
    Triple(DisplayLanguage.KANNADA, "ಕನ್ನಡ", "Kannada"),
    Triple(DisplayLanguage.BENGALI, "বাংলা", "Bengali"),
    Triple(DisplayLanguage.GUJARATI, "ગુજરાતી", "Gujarati")
)

@Composable
fun LanguageScreen(vm: OnboardingViewModel, onContinue: () -> Unit) {
    val ctx = LocalContext.current
    OnboardingScaffold(
        step = 3,
        title = stringResource(R.string.onb_language_title),
        ctaEnabled = true,
        onContinue = {
            vm.persistLanguage(ctx)
            onContinue()
        }
    ) {
        LANGUAGE_LABELS.forEach { (lang, native, caption) ->
            SelectableCard(
                text = native,
                supportingText = caption,
                selected = vm.selectedLanguage == lang,
                onClick = { vm.selectLanguage(lang) }
            )
        }
    }
}
```
- [ ] **11.3** Build green. Commit `feat(onboarding): language picker with native-script labels` + push.

## Task 12: AppsScreen

**Files:** Create `onboarding/AppsScreen.kt`; Modify `strings.xml`.

- [ ] **12.1** Strings: `<string name="onb_apps_title">What pulls you in?</string>`.
- [ ] **12.2** Screen (app catalog: name + package; checkbox semantics via SelectableCard):
```kotlin
package com.myniyam.app.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.myniyam.app.R

private val APP_CATALOG: List<Pair<String, String>> = listOf(
    "Instagram" to "com.instagram.android",
    "YouTube" to "com.google.android.youtube",
    "Facebook" to "com.facebook.katana",
    "X" to "com.twitter.android",
    "Reddit" to "com.reddit.frontpage",
    "Snapchat" to "com.snapchat.android",
    "TikTok" to "com.zhiliaoapp.musically"
)

@Composable
fun AppsScreen(vm: OnboardingViewModel, onContinue: () -> Unit) {
    val ctx = LocalContext.current
    OnboardingScaffold(
        step = 4,
        title = stringResource(R.string.onb_apps_title),
        ctaEnabled = vm.canContinueFromApps(),
        onContinue = {
            vm.persistApps(ctx)
            onContinue()
        }
    ) {
        APP_CATALOG.forEach { (name, pkg) ->
            SelectableCard(
                text = name,
                selected = pkg in vm.selectedPackages,
                onClick = { vm.togglePackage(pkg) }
            )
        }
    }
}
```
- [ ] **12.3** Build green. Commit `feat(onboarding): apps-to-block checklist` + push.

## Task 13: Navigation wiring + Welcome restyle + routing

**Files:** Modify `ui/AppNavHost.kt`, `ui/screens/WelcomeScreen.kt`, `MainActivity.kt`; `strings.xml` (welcome copy already exists).

- [ ] **13.1** `WelcomeScreen.kt` — keep API (`onGetStarted`), restyle body:
```kotlin
package com.myniyam.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.myniyam.app.R

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.welcome_title),
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(48.dp))
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(stringResource(R.string.welcome_cta), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
```
- [ ] **13.2** `AppNavHost.kt` — add the four onboarding routes between welcome and the permission flow; shared ViewModel scoped to the activity; final permission step writes `onboardingComplete`. Replace file content:
```kotlin
package com.myniyam.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.myniyam.app.R
import com.myniyam.app.data.UserPrefs
import com.myniyam.app.onboarding.AppsScreen
import com.myniyam.app.onboarding.IntentionScreen
import com.myniyam.app.onboarding.LanguageScreen
import com.myniyam.app.onboarding.MantraPickerScreen
import com.myniyam.app.onboarding.OnboardingViewModel
import com.myniyam.app.permissions.PermissionChecker
import com.myniyam.app.ui.screens.HomeScreen
import com.myniyam.app.ui.screens.OemAutostartScreen
import com.myniyam.app.ui.screens.PermissionScreen
import com.myniyam.app.ui.screens.WelcomeScreen

object NiyamRoutes {
    const val WELCOME = "welcome"
    const val ONB_INTENTION = "onboarding_intention"
    const val ONB_MANTRA = "onboarding_mantra"
    const val ONB_LANGUAGE = "onboarding_language"
    const val ONB_APPS = "onboarding_apps"
    const val PERMISSION_USAGE = "permission_usage_stats"
    const val PERMISSION_OVERLAY = "permission_overlay"
    const val PERMISSION_ACCESSIBILITY = "permission_accessibility"
    const val PERMISSION_BATTERY = "permission_battery"
    const val PERMISSION_OEM = "permission_oem_autostart"
    const val HOME = "home"
}

@Composable
fun AppNavHost(
    startDestination: String,
    navController: NavHostController = rememberNavController()
) {
    val onboardingVm: OnboardingViewModel = viewModel()

    NavHost(navController = navController, startDestination = startDestination) {

        composable(NiyamRoutes.WELCOME) {
            WelcomeScreen(onGetStarted = { navController.navigate(NiyamRoutes.ONB_INTENTION) })
        }

        composable(NiyamRoutes.ONB_INTENTION) {
            IntentionScreen(onboardingVm) { navController.navigate(NiyamRoutes.ONB_MANTRA) }
        }
        composable(NiyamRoutes.ONB_MANTRA) {
            MantraPickerScreen(onboardingVm) { navController.navigate(NiyamRoutes.ONB_LANGUAGE) }
        }
        composable(NiyamRoutes.ONB_LANGUAGE) {
            LanguageScreen(onboardingVm) { navController.navigate(NiyamRoutes.ONB_APPS) }
        }
        composable(NiyamRoutes.ONB_APPS) {
            AppsScreen(onboardingVm) { navController.navigate(NiyamRoutes.PERMISSION_USAGE) }
        }

        composable(NiyamRoutes.PERMISSION_USAGE) {
            val ctx = LocalContext.current
            PermissionScreen(
                titleResId = R.string.perm_usage_title,
                bodyResId = R.string.perm_usage_body,
                isGranted = { PermissionChecker.hasUsageStatsAccess(ctx) },
                launchSettings = { PermissionChecker.openUsageAccessSettings(ctx) },
                onGranted = { navController.navigate(NiyamRoutes.PERMISSION_OVERLAY) }
            )
        }

        composable(NiyamRoutes.PERMISSION_OVERLAY) {
            val ctx = LocalContext.current
            PermissionScreen(
                titleResId = R.string.perm_overlay_title,
                bodyResId = R.string.perm_overlay_body,
                isGranted = { PermissionChecker.hasOverlayPermission(ctx) },
                launchSettings = { PermissionChecker.openOverlayPermissionSettings(ctx) },
                onGranted = { navController.navigate(NiyamRoutes.PERMISSION_ACCESSIBILITY) }
            )
        }

        composable(NiyamRoutes.PERMISSION_ACCESSIBILITY) {
            val ctx = LocalContext.current
            PermissionScreen(
                titleResId = R.string.perm_accessibility_title,
                bodyResId = R.string.perm_accessibility_body,
                isGranted = { PermissionChecker.isAccessibilityServiceEnabled(ctx) },
                launchSettings = { PermissionChecker.openAccessibilitySettings(ctx) },
                onGranted = { navController.navigate(NiyamRoutes.PERMISSION_BATTERY) }
            )
        }

        composable(NiyamRoutes.PERMISSION_BATTERY) {
            val ctx = LocalContext.current
            PermissionScreen(
                titleResId = R.string.perm_battery_title,
                bodyResId = R.string.perm_battery_body,
                isGranted = { PermissionChecker.isIgnoringBatteryOptimizations(ctx) },
                launchSettings = { PermissionChecker.openIgnoreBatteryOptimizationSettings(ctx) },
                onGranted = { navController.navigate(NiyamRoutes.PERMISSION_OEM) }
            )
        }

        composable(NiyamRoutes.PERMISSION_OEM) {
            val ctx = LocalContext.current
            OemAutostartScreen(onDone = {
                onboardingVm.persistOnboardingComplete(ctx)
                navController.navigate(NiyamRoutes.HOME) {
                    popUpTo(NiyamRoutes.WELCOME) { inclusive = true }
                }
            })
        }

        composable(NiyamRoutes.HOME) { HomeScreen() }
    }
}
```
- [ ] **13.3** `MainActivity.kt` — route by onboarding state:
```kotlin
package com.myniyam.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.myniyam.app.data.UserPrefs
import com.myniyam.app.ui.AppNavHost
import com.myniyam.app.ui.NiyamRoutes
import com.myniyam.app.ui.theme.NiyamTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UserPrefs.ensureLoaded(this)
        val start = if (UserPrefs.snapshot().onboardingComplete) NiyamRoutes.HOME else NiyamRoutes.WELCOME
        setContent {
            NiyamTheme {
                AppNavHost(startDestination = start)
            }
        }
    }
}
```
(`ensureLoaded` here is belt-and-suspenders with the Application warm-up; it's a one-time blocking read of a tiny prefs file at activity start — acceptable, and on every subsequent call it's a no-op.)
- [ ] **13.4** Full suite (55/55) + `assembleDebug` green.
- [ ] **13.5** Commit `feat(onboarding): nav flow wired — welcome → 4 steps → permissions → home; returning users skip` + push.

## Task 14: Integrated verification + SP-3 report

**Files:** Create `docs/superpowers/test-reports/2026-06-10-sp3-acceptance.md`; Modify `SESSION_LOG.md` (consolidated entry).

- [ ] **14.1** Full gates: suite (expect 55), `assembleDebug`, APK size delta vs pre-SP3 (budget ≤1MB growth; record exact numbers).
- [ ] **14.2** Emulator best-effort (if `adb devices` non-empty): `installDebug`; walk Welcome→onboarding→permissions→home with `adb shell input tap` where feasible or report manual-needed; screencap each onboarding screen + the overlay (launch YouTube) to `/tmp/sp3_*.png`; verify acceptance criteria 1-4+6 from the spec; criterion 7 (visual sign-off) is Pranav's. No device → record "deferred to Pranav," continue.
- [ ] **14.3** Write the acceptance report (criteria 1-8 from spec §10 with PASS/OPEN + evidence), update SESSION_LOG.
- [ ] **14.4** Commit `docs: SP-3 acceptance report` + push. Report SP-3 status to controller.

---

## Self-review

**Spec coverage:** theme tokens/typography/shape (§4) → T2-T3; overlay restyle (§4) → T4 + label binding T6; onboarding screens + copy (§5) → T9-T13; curation (§6) → T7; persistence (§7) → T5; engine touches (§8) → T6 exactly as founder-approved; error handling (§9) → defaults in T5 (`fromRaw` fallbacks), CTA gating T8/T12, repository fallback pre-existing; acceptance (§10) → T14 (criteria 5 covered by tests added in T5/T6/T7/T8; criterion 7 explicitly Pranav's). No gaps.

**Placeholder scan:** T9.3 contains flagged pseudo-syntax with an explicit IMPLEMENTER NOTE to normalize imports — deliberate, bounded, not a TBD. No TODOs elsewhere.

**Type consistency:** `UserPrefs.Snapshot/snapshot()/ensureLoaded/setSnapshotForTest/resetForTest` consistent across T5/T6/T8/T13; `BlockList.DEFAULT_PACKAGES` introduced T6, referenced T5 (with explicit ordering note), T8; `StarterMantras.forIntention(Intention): List<Mantra>` consistent T7/T10; `OnboardingViewModel` API consistent T8-T13; `AppNavHost(startDestination)` signature change matched in MainActivity T13. Test-count arithmetic: 43+4(T5)+2(T6)+2(T7)+4(T8)=55 ✓.
