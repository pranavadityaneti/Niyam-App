# Niyam — Library — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Browse/filter all 26 mantras, full-text detail view in the user's language, and sadhana switching with an honest restart warning, per [the SP-5 spec](../specs/2026-06-10-library-design.md).

**Architecture:** Additive `sourceCategory` enum on the content schema (hand-tagged, validation-enforced); pure `LibraryFilters` (TDD); two new Compose screens reusing the established brand components; switching rides the existing `UserPrefs.setCurrentMantra`. **Zero engine-file changes.**

**Tech Stack:** existing stack only — no new dependencies.

---

## Execution conventions

Same as SP-3/SP-4: continuous full-control mode, Opus subagents, combined or two-stage review per task, direct commit+push. Baseline: HEAD `4595149`, **69 tests green**. Brand law + ui-ux-pro-max protocol for UI tasks (3, 4, 5).

## File structure

```
app/src/main/java/com/myniyam/app/data/Mantra.kt                 M  T1 (SourceCategory + field)
app/src/main/assets/content/mantras.json                          M  T1 (26 tags + contentVersion bump)
app/src/test/java/com/myniyam/app/data/ContentValidationTest.kt   M  T1 (+2 tests)
app/src/main/java/com/myniyam/app/library/LibraryFilters.kt       C  T2 (+test)
app/src/main/java/com/myniyam/app/library/LibraryScreen.kt        C  T3
app/src/main/java/com/myniyam/app/library/MantraDetailScreen.kt   C  T4
app/src/main/java/com/myniyam/app/ui/AppNavHost.kt                M  T5
app/src/main/java/com/myniyam/app/ui/screens/HomeScreen.kt        M  T5 (Browse button)
app/src/main/res/values/strings.xml                               M  T3, T4, T5
docs/superpowers/test-reports/2026-06-10-sp5-acceptance.md        C  T6
```

Expected test growth: 69 → **76** (validation +2, LibraryFilters +5).

---

## Task 1: SourceCategory — model, content tags, validation

**Files:** Modify `app/src/main/java/com/myniyam/app/data/Mantra.kt`, `app/src/main/assets/content/mantras.json`, `app/src/test/java/com/myniyam/app/data/ContentValidationTest.kt`.

- [ ] **1.1** `Mantra.kt` — add below the `Intention` enum:
```kotlin
@Serializable
enum class SourceCategory {
    @SerialName("vedic") VEDIC,
    @SerialName("upanishad") UPANISHAD,
    @SerialName("gita") GITA,
    @SerialName("stotra") STOTRA
}
```
and add to the `Mantra` data class (after `deity`):
```kotlin
val sourceCategory: SourceCategory = SourceCategory.STOTRA,
```
(The default is a parser safety net only — the asset must still declare it everywhere; test-enforced in 1.3.)
- [ ] **1.2** `mantras.json` — add `"sourceCategory": "<value>"` to every entry directly after its `"deity"` line, per this EXACT mapping (spec §5): vedic = {gayatri, mahamrityunjaya, om-namah-shivaya, purusha-suktam, nasadiya-suktam}; upanishad = {om, asato-ma, om-sahanavavatu, hare-krishna}; gita = {gita-2-47, gita-6-5, gita-2-14, gita-6-6, gita-2-70, gita-4-7-8, gita-18-66, gita-3-35}; stotra = {twameva-mata, vakratunda, saraswati-vandana, guru-brahma, hanuman-chalisa-opening, vishnu-sahasranama-opening, lalita-sahasranama-opening, krishna-ashtakam, ram-raksha-opening}. Bump `"contentVersion"` to `"2026-06-10.2"`. Use a python3 one-liner for reliability (load json → set fields from the map → dump ensure_ascii=False indent=2 + trailing newline) rather than 26 hand edits.
- [ ] **1.3** `ContentValidationTest` — add two tests inside the class:
```kotlin
    @Test
    fun `every entry declares sourceCategory explicitly in the asset`() {
        val root = Json.parseToJsonElement(assetFile.readText()).jsonObject
        val entries = root.getValue("mantras").jsonArray
        entries.forEach { e ->
            val obj = e.jsonObject
            val id = obj.getValue("id").jsonPrimitive.content
            assertTrue("$id: missing explicit sourceCategory", obj.containsKey("sourceCategory"))
        }
    }

    @Test
    fun `sourceCategory tags match the spec table`() {
        val expected = mapOf(
            "gayatri" to SourceCategory.VEDIC, "mahamrityunjaya" to SourceCategory.VEDIC,
            "om-namah-shivaya" to SourceCategory.VEDIC, "purusha-suktam" to SourceCategory.VEDIC,
            "nasadiya-suktam" to SourceCategory.VEDIC,
            "om" to SourceCategory.UPANISHAD, "asato-ma" to SourceCategory.UPANISHAD,
            "om-sahanavavatu" to SourceCategory.UPANISHAD, "hare-krishna" to SourceCategory.UPANISHAD,
            "gita-2-47" to SourceCategory.GITA, "gita-6-5" to SourceCategory.GITA,
            "gita-2-14" to SourceCategory.GITA, "gita-6-6" to SourceCategory.GITA,
            "gita-2-70" to SourceCategory.GITA, "gita-4-7-8" to SourceCategory.GITA,
            "gita-18-66" to SourceCategory.GITA, "gita-3-35" to SourceCategory.GITA,
            "twameva-mata" to SourceCategory.STOTRA, "vakratunda" to SourceCategory.STOTRA,
            "saraswati-vandana" to SourceCategory.STOTRA, "guru-brahma" to SourceCategory.STOTRA,
            "hanuman-chalisa-opening" to SourceCategory.STOTRA,
            "vishnu-sahasranama-opening" to SourceCategory.STOTRA,
            "lalita-sahasranama-opening" to SourceCategory.STOTRA,
            "krishna-ashtakam" to SourceCategory.STOTRA, "ram-raksha-opening" to SourceCategory.STOTRA
        )
        assertEquals(26, expected.size)
        catalog.mantras.forEach { m ->
            assertEquals("${m.id}: wrong category", expected.getValue(m.id), m.sourceCategory)
        }
    }
```
New imports for the test file: `kotlinx.serialization.json.jsonObject`, `jsonArray`, `jsonPrimitive` (the `Json` import already exists).
- [ ] **1.4** Verify: full suite `--rerun-tasks` → **71/71** (69 + 2; ContentValidationTest = 12); `cd tools && .venv/bin/python generate_scripts.py --check` → **exit 0** (the tool preserves unknown fields — its write path round-trips the whole JSON tree); asset still ≤400KB.
- [ ] **1.5** Commit `feat(content): sourceCategory on all 26 entries — model, tags, validation` + push.

## Task 2: LengthBucket + LibraryFilters (TDD)

**Files:** Test `app/src/test/java/com/myniyam/app/library/LibraryFiltersTest.kt`, Create `app/src/main/java/com/myniyam/app/library/LibraryFilters.kt`.

- [ ] **2.1** Failing test:
```kotlin
package com.myniyam.app.library

import com.myniyam.app.data.Deity
import com.myniyam.app.data.Intention
import com.myniyam.app.data.MantraRepository
import com.myniyam.app.data.SourceCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class LibraryFiltersTest {

    @Before
    fun loadCatalog() {
        MantraRepository.resetForTest()
        check(MantraRepository.initFromJson(File("src/main/assets/content/mantras.json").readText()))
    }

    @Test
    fun `length buckets have correct boundaries`() {
        assertEquals(LengthBucket.UNDER_30S, LengthBucket.of(29))
        assertEquals(LengthBucket.S30_TO_60, LengthBucket.of(30))
        assertEquals(LengthBucket.S30_TO_60, LengthBucket.of(60))
        assertEquals(LengthBucket.OVER_60S, LengthBucket.of(61))
    }

    @Test
    fun `empty selection passes everything through in catalog order`() {
        val all = MantraRepository.all()
        assertEquals(all.map { it.id }, LibraryFilters.apply(all, LibraryFilters.Selection()).map { it.id })
    }

    @Test
    fun `category filter narrows to the gita eight`() {
        val result = LibraryFilters.apply(
            MantraRepository.all(),
            LibraryFilters.Selection(category = SourceCategory.GITA)
        )
        assertEquals(8, result.size)
        assertTrue(result.all { it.sourceCategory == SourceCategory.GITA })
    }

    @Test
    fun `dimensions combine with AND`() {
        val result = LibraryFilters.apply(
            MantraRepository.all(),
            LibraryFilters.Selection(category = SourceCategory.GITA, intention = Intention.CALM)
        )
        assertEquals(listOf("gita-2-14", "gita-2-70"), result.map { it.id })
    }

    @Test
    fun `deity and length filters work`() {
        val shiva = LibraryFilters.apply(MantraRepository.all(), LibraryFilters.Selection(deity = Deity.SHIVA))
        assertEquals(setOf("mahamrityunjaya", "om-namah-shivaya"), shiva.map { it.id }.toSet())
        val long = LibraryFilters.apply(MantraRepository.all(), LibraryFilters.Selection(length = LengthBucket.OVER_60S))
        assertEquals(setOf("hanuman-chalisa-opening", "vishnu-sahasranama-opening", "lalita-sahasranama-opening"), long.map { it.id }.toSet())
    }
}
```
- [ ] **2.2** Run → FAILS. Implementation:
```kotlin
package com.myniyam.app.library

import com.myniyam.app.data.Deity
import com.myniyam.app.data.Intention
import com.myniyam.app.data.Mantra
import com.myniyam.app.data.SourceCategory

enum class LengthBucket {
    UNDER_30S, S30_TO_60, OVER_60S;

    companion object {
        fun of(seconds: Int): LengthBucket = when {
            seconds < 30 -> UNDER_30S
            seconds <= 60 -> S30_TO_60
            else -> OVER_60S
        }
    }
}

/** Pure library filtering (spec §4): single-select per dimension, AND across dimensions, catalog order preserved. */
object LibraryFilters {

    data class Selection(
        val category: SourceCategory? = null,
        val length: LengthBucket? = null,
        val intention: Intention? = null,
        val deity: Deity? = null
    )

    fun apply(all: List<Mantra>, sel: Selection): List<Mantra> = all.filter { m ->
        (sel.category == null || m.sourceCategory == sel.category) &&
            (sel.length == null || LengthBucket.of(m.estimatedReadSeconds) == sel.length) &&
            (sel.intention == null || sel.intention in m.intentions) &&
            (sel.deity == null || m.deity == sel.deity)
    }
}
```
- [ ] **2.3** Focused 5/5; full suite → **76/76**. Commit `feat(library): LengthBucket + LibraryFilters — pure, tested` + push.

## Task 3: LibraryScreen

**Files:** Create `app/src/main/java/com/myniyam/app/library/LibraryScreen.kt`; Modify `strings.xml`. (UI protocol: consult ui-ux-pro-max `--domain ux "filter chips list"` + `--stack jetpack-compose`; brand law.)

- [ ] **3.1** Strings:
```xml
<!-- Library -->
<string name="library_overline">Library</string>
<string name="library_title">All mantras</string>
<string name="library_filter_all">All</string>
<string name="library_empty">Nothing matches — clear a filter.</string>
<string name="library_current_marker">Current</string>
<string name="library_completed_marker">Completed</string>
```
- [ ] **3.2** Screen (structural spec — clean idiomatic Compose; filter chips via `FilterChip` from material3):
```kotlin
package com.myniyam.app.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.myniyam.app.R
import com.myniyam.app.data.Deity
import com.myniyam.app.data.Intention
import com.myniyam.app.data.MantraRepository
import com.myniyam.app.data.SourceCategory
import com.myniyam.app.data.UserPrefs
import com.myniyam.app.onboarding.mantraGist

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(onOpenDetail: (String) -> Unit) {
    val ctx = LocalContext.current
    MantraRepository.ensureLoaded(ctx)
    val all = MantraRepository.all()
    val snap = UserPrefs.snapshot()

    var selection by remember { mutableStateOf(LibraryFilters.Selection()) }
    val results = remember(selection, all) { LibraryFilters.apply(all, selection) }

    Scaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                stringResource(R.string.library_overline).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.library_title), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(14.dp))

            FilterRow(
                options = SourceCategory.entries.map { it to it.label() },
                selected = selection.category,
                onSelect = { selection = selection.copy(category = it) }
            )
            FilterRow(
                options = LengthBucket.entries.map { it to it.label() },
                selected = selection.length,
                onSelect = { selection = selection.copy(length = it) }
            )
            FilterRow(
                options = Intention.entries.map { it to it.label() },
                selected = selection.intention,
                onSelect = { selection = selection.copy(intention = it) }
            )
            FilterRow(
                options = Deity.entries.map { it to it.label() },
                selected = selection.deity,
                onSelect = { selection = selection.copy(deity = it) }
            )

            Spacer(Modifier.height(8.dp))

            if (results.isEmpty()) {
                Text(
                    stringResource(R.string.library_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                LazyColumn(state = rememberLazyListState(), modifier = Modifier.weight(1f)) {
                    items(results, key = { it.id }) { mantra ->
                        Card(
                            onClick = { onOpenDetail(mantra.id) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        mantra.canonicalName,
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    when {
                                        mantra.id == snap.currentMantraId -> MarkerChip(
                                            stringResource(R.string.library_current_marker),
                                            MaterialTheme.colorScheme.primary
                                        )
                                        mantra.id in snap.completedMantraIds -> MarkerChip(
                                            stringResource(R.string.library_completed_marker),
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    mantraGist(mantra.meaning.en),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "${mantra.sourceCategory.label()} · ~${mantra.estimatedReadSeconds}s",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> FilterRow(options: List<Pair<T, String>>, selected: T?, onSelect: (T?) -> Unit) {
    Row(
        Modifier
            .horizontalScroll(rememberScrollState())
            .padding(bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            label = { Text(stringResource(R.string.library_filter_all)) }
        )
        options.forEach { (value, label) ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelect(if (selected == value) null else value) },
                label = { Text(label) }
            )
        }
    }
}

internal fun SourceCategory.label(): String = when (this) {
    SourceCategory.VEDIC -> "Vedic"
    SourceCategory.UPANISHAD -> "Upanishads"
    SourceCategory.GITA -> "Bhagavad Gita"
    SourceCategory.STOTRA -> "Stotras"
}

internal fun LengthBucket.label(): String = when (this) {
    LengthBucket.UNDER_30S -> "Under 30s"
    LengthBucket.S30_TO_60 -> "30–60s"
    LengthBucket.OVER_60S -> "Over 1 min"
}

internal fun Intention.label(): String = when (this) {
    Intention.FOCUS -> "Focus"
    Intention.CALM -> "Calm"
    Intention.SADHANA -> "Sadhana"
    Intention.DHARMA -> "Dharma"
    Intention.DEVOTION -> "Devotion"
}

internal fun Deity.label(): String = when (this) {
    Deity.SHIVA -> "Shiva"; Deity.VISHNU -> "Vishnu"; Deity.DEVI -> "Devi"
    Deity.GANESHA -> "Ganesha"; Deity.HANUMAN -> "Hanuman"; Deity.KRISHNA -> "Krishna"
    Deity.RAMA -> "Rama"; Deity.SARASWATI -> "Saraswati"; Deity.LAKSHMI -> "Lakshmi"
    Deity.UNIVERSAL -> "Universal"
}
```
(Implementer: split the multi-statement `when` branches onto separate lines if ktlint-style complaints arise; `FilterChip` theming inherits colorScheme — selected state uses secondaryContainer by default which is NOT branded — override via `FilterChipDefaults.filterChipColors(selectedContainerColor = OrangeTint, selectedLabelColor = BottleGreen)` importing the two tokens, and note it. Orange selection per brand law.)
- [ ] **3.3** Build + suite 76/76. Commit `feat(library): browse + filter screen` + push.

## Task 4: MantraDetailScreen + switch dialog

**Files:** Create `app/src/main/java/com/myniyam/app/library/MantraDetailScreen.kt`; Modify `strings.xml`.

- [ ] **4.1** Strings:
```xml
<string name="detail_make_current">Make this my sadhana</string>
<string name="detail_is_current">Your current sadhana</string>
<string name="detail_practice_again">Practice again</string>
<string name="detail_switch_title">Switch your sadhana?</string>
<string name="detail_switch_body_fmt">You\'re %1$d days into %2$s. If you switch, this journey will start over when you return to it.</string>
<string name="detail_switch_body_nocount_fmt">You\'re partway into %1$s. If you switch, this journey will start over when you return to it.</string>
<string name="detail_switch_confirm">Switch</string>
<string name="detail_switch_cancel">Keep current</string>
```
- [ ] **4.2** Screen:
```kotlin
package com.myniyam.app.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.myniyam.app.R
import com.myniyam.app.data.CurrentSadhana
import com.myniyam.app.data.MantraRepository
import com.myniyam.app.data.Script
import com.myniyam.app.data.UserPrefs
import com.myniyam.app.progress.ProgressRepository
import kotlinx.coroutines.launch

@Composable
fun MantraDetailScreen(mantraId: String, onSwitched: () -> Unit, onMissing: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    MantraRepository.ensureLoaded(ctx)
    val mantra = MantraRepository.byId(mantraId)
    if (mantra == null) {
        LaunchedEffect(Unit) { onMissing() }
        return
    }

    val snap = UserPrefs.snapshot()
    val isCurrent = mantra.id == snap.currentMantraId
    val isCompleted = mantra.id in snap.completedMantraIds
    val lang = CurrentSadhana.LANGUAGE

    var showDialog by remember { mutableStateOf(false) }
    var currentDayN by remember { mutableStateOf<Int?>(null) }

    fun performSwitch() {
        scope.launch {
            UserPrefs.setCurrentMantra(ctx, mantra.id)
            onSwitched()
        }
    }

    Scaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                "${mantra.sourceCategory.label().uppercase()} · ${mantra.source.uppercase()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Text(mantra.canonicalName, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    mantra.text.forScript(lang.script),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                        lineHeight = 30.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    mantra.text.forScript(Script.ROMAN),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    mantra.meaning.forLang(lang.meaningLang),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "~${mantra.estimatedReadSeconds}s · ${mantra.completionThresholdDays}-day journey",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    val currentHasProgress = !snap.pendingCelebration &&
                        snap.currentMantraId !in snap.completedMantraIds
                    if (isCurrent) return@Button
                    if (currentHasProgress) {
                        scope.launch {
                            currentDayN = runCatching { ProgressRepository.homeStats(ctx).dayN }.getOrNull()
                            showDialog = true
                        }
                    } else {
                        performSwitch()
                    }
                },
                enabled = !isCurrent,
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    stringResource(
                        when {
                            isCurrent -> R.string.detail_is_current
                            isCompleted -> R.string.detail_practice_again
                            else -> R.string.detail_make_current
                        }
                    ),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Spacer(Modifier.height(20.dp))
        }
    }

    if (showDialog) {
        val currentName = MantraRepository.displayMantra(snap.currentMantraId).canonicalName
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.detail_switch_title)) },
            text = {
                Text(
                    currentDayN?.takeIf { it > 0 }?.let {
                        stringResource(R.string.detail_switch_body_fmt, it, currentName)
                    } ?: stringResource(R.string.detail_switch_body_nocount_fmt, currentName)
                )
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false; performSwitch() }) {
                    Text(stringResource(R.string.detail_switch_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.detail_switch_cancel))
                }
            }
        )
    }
}
```
(Implementer notes: add `import androidx.compose.ui.unit.sp`; spec rule — the warning shows only when the CURRENT sadhana has ≥1 counted day and isn't completed: the `currentHasProgress` gate above approximates "not completed"; refine to also skip the dialog when `currentDayN == 0` by checking AFTER loading stats — i.e., load dayN first, and if it's 0 call performSwitch() directly instead of showing the dialog. Implement that exact refinement.)
- [ ] **4.3** Build + suite 76/76. Commit `feat(library): mantra detail + honest switch warning` + push.

## Task 5: Routes + Home button

**Files:** Modify `ui/AppNavHost.kt`, `ui/screens/HomeScreen.kt`, `strings.xml`.

- [ ] **5.1** String: `<string name="home_browse_library">Browse library</string>`
- [ ] **5.2** NavHost: add to NiyamRoutes: `const val LIBRARY = "library"`, `const val MANTRA_DETAIL = "mantra_detail/{mantraId}"`; add composables:
```kotlin
composable(NiyamRoutes.LIBRARY) {
    LibraryScreen(onOpenDetail = { id -> navController.navigate("mantra_detail/$id") })
}
composable(
    NiyamRoutes.MANTRA_DETAIL,
    arguments = listOf(navArgument("mantraId") { type = NavType.StringType })
) { backStackEntry ->
    MantraDetailScreen(
        mantraId = backStackEntry.arguments?.getString("mantraId") ?: "",
        onSwitched = { navController.popBackStack(NiyamRoutes.HOME, inclusive = false) },
        onMissing = { navController.popBackStack() }
    )
}
```
HomeScreen gains a parameter `onBrowseLibrary: () -> Unit` and renders, below the stat-chips row:
```kotlin
Spacer(Modifier.height(16.dp))
OutlinedButton(
    onClick = onBrowseLibrary,
    shape = RoundedCornerShape(999.dp),
    modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
) {
    Text(stringResource(R.string.home_browse_library), style = MaterialTheme.typography.labelLarge)
}
```
HOME composable call site passes `onBrowseLibrary = { navController.navigate(NiyamRoutes.LIBRARY) }`. New imports in NavHost: `androidx.navigation.NavType`, `androidx.navigation.navArgument`, `com.myniyam.app.library.LibraryScreen`, `com.myniyam.app.library.MantraDetailScreen`; in HomeScreen: `OutlinedButton`, `RoundedCornerShape`.
- [ ] **5.3** Build + suite 76/76. Commit `feat(library): routes + Browse library on Home` + push.

## Task 6: Verification + SP-5 acceptance report

**Files:** Create `docs/superpowers/test-reports/2026-06-10-sp5-acceptance.md`; Modify `SESSION_LOG.md`.

- [ ] **6.1** Gates: suite `--rerun-tasks` 76/76 from XMLs; `assembleDebug`; `--check` exit 0; asset size; confirm zero engine files in `git diff 4595149..HEAD --name-only` (no service/, no OverlayManager, no BlockList).
- [ ] **6.2** Emulator best-effort or defer.
- [ ] **6.3** Report scoring spec §7 criteria; SESSION_LOG entry "SP-5 EXECUTED".
- [ ] **6.4** Commit `docs: SP-5 acceptance report` + push.

---

## Self-review

**Spec coverage:** §2 rules → T1 (category), T2 (buckets/AND), T4 (honest dialog + CTA states), T5 (Home button); §4 architecture → T1-T5 one-to-one; §5 table → T1.2/1.3 (the test IS the table); §6 errors → T4 (onMissing, nocount fallback), T3 (empty state); §7 acceptance → T1/T2 tests + T6; §8 zero engine touches → T6.1 explicit check. No gaps.

**Placeholder scan:** clean — implementer notes are bounded instructions (chip color override, dialog refinement) with exact guidance, not deferrals.

**Type consistency:** `LibraryFilters.Selection/apply`, `LengthBucket.of` consistent T2/T3; `label()` extensions defined T3 and used T4 (same package ✓ — both files in `com.myniyam.app.library`, `internal` visibility works); `MantraDetailScreen(mantraId, onSwitched, onMissing)` matches T5 call; `HomeScreen(onFixProtection, onBrowseLibrary)` — T5 updates the only call site; `mantraGist` import path matches SP-3. Test math: 69 + 2 + 5 = 76 ✓.
