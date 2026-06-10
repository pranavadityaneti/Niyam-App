# Niyam — Content Model & Seed Library — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship the versioned mantra content store (schema + `MantraRepository` + 26 fully populated entries in 7 scripts and 8 meaning languages) and make the unlock overlay read Gayatri from it, satisfying all 7 acceptance criteria in [the design spec](../specs/2026-06-10-content-model-design.md).

**Goal in plain English:** build the bookshelf, fill it with 26 verified mantras, and teach the engine to read off the shelf instead of off a sticker.

**Architecture:** Content lives in one JSON asset (`assets/content/mantras.json`), parsed once at startup by kotlinx-serialization into an immutable in-memory `MantraRepository` with a hardcoded `om` fallback. Script variants are machine-generated offline from each entry's Devanagari master by `tools/generate_scripts.py` (Aksharamukha). A JVM validation test enforces content completeness on every build.

**Tech Stack:** Kotlin 2.0.21 + kotlinx-serialization-json 1.7.3 · Python 3.9+ + aksharamukha (offline tooling only) · existing JUnit 4 test setup.

---

## Execution conventions (override defaults — from Pranav's CLAUDE.md)

1. **One task at a time**, STOP after each, wait for approval — unless Pranav grants an autonomous run for a stretch (he has done this before; the grant must be explicit in-session).
2. After each task report: files changed · verification output (exact command + tail) · what's next.
3. A step failing twice → stop, log to `ERRORS.md` (create at repo root if missing), ask Pranav.
4. No files touched outside the task's `Files:` list.
5. Verification commands: `./gradlew :app:compileDebugKotlin` (type-check), `./gradlew :app:testDebugUnitTest` (tests), `./gradlew :app:assembleDebug` (APK).
6. Commit + push to `origin main` after every task (established repo workflow).
7. **Content-task methodology:** Tasks 7-11 author scripture text and meanings at execution time. The plan gives per-entry metadata (final), named verification sources, and the complete `om` entry (Task 6) as the structural template. "Done" for content is enforced mechanically by `ContentValidationTest`, not by prose in this plan. This is deliberate, not a placeholder violation: the text IS the deliverable.

---

## File structure

```
gradle/libs.versions.toml                                    M  Task 1
build.gradle.kts                                             M  Task 1
app/build.gradle.kts                                         M  Task 1
app/src/main/java/com/myniyam/app/data/
  Mantra.kt                                                  C  Task 2   (model + enums)
  DisplayLanguage.kt                                         C  Task 3   (picker mapping)
  MantraRepository.kt                                        C  Task 4   (parse-once store + fallback)
  CurrentSadhana.kt                                          C  Task 14  (stand-in constants)
app/src/test/java/com/myniyam/app/data/
  MantraModelTest.kt                                         C  Task 2
  DisplayLanguageTest.kt                                     C  Task 3
  MantraRepositoryTest.kt                                    C  Task 4
  ContentValidationTest.kt                                   C  Task 6, M Task 12
tools/
  generate_scripts.py                                        C  Task 5
  requirements.txt                                           C  Task 5
  README.md                                                  C  Task 5
app/src/main/assets/content/mantras.json                     C  Task 6, M Tasks 7-11
app/src/main/java/com/myniyam/app/overlay/OverlayManager.kt  M  Task 14
docs/superpowers/test-reports/2026-06-DD-content-spotcheck.md C Task 13 (DD = execution date)
```

---

## Task 1: kotlinx-serialization plugin + dependency

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `build.gradle.kts`
- Modify: `app/build.gradle.kts`

- [ ] **Step 1.1: Add to `gradle/libs.versions.toml`**

Under `[versions]` add:
```toml
kotlinxSerializationJson = "1.7.3"
```
Under `[libraries]` add:
```toml
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinxSerializationJson" }
```
Under `[plugins]` add:
```toml
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

- [ ] **Step 1.2: Register plugin in root `build.gradle.kts`**

Add inside the `plugins {}` block:
```kotlin
alias(libs.plugins.kotlin.serialization) apply false
```

- [ ] **Step 1.3: Apply in `app/build.gradle.kts`**

Add inside the `plugins {}` block:
```kotlin
alias(libs.plugins.kotlin.serialization)
```
Add inside `dependencies {}`:
```kotlin
implementation(libs.kotlinx.serialization.json)
```
Add inside `android {}` (sibling of `buildFeatures`) so `android.util.Log` calls return defaults in JVM tests instead of throwing:
```kotlin
testOptions {
    unitTests.isReturnDefaultValues = true
}
```

- [ ] **Step 1.4: Verify**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 1.5: Commit**

```bash
git add gradle/libs.versions.toml build.gradle.kts app/build.gradle.kts
git commit -m "chore: add kotlinx-serialization for content store

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
git push origin main
```

**STOP. Wait for approval (unless autonomous run granted).**

---

## Task 2: Mantra data model (TDD)

**Files:**
- Test: `app/src/test/java/com/myniyam/app/data/MantraModelTest.kt`
- Create: `app/src/main/java/com/myniyam/app/data/Mantra.kt`

- [ ] **Step 2.1: Write the failing test**

```kotlin
package com.myniyam.app.data

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class MantraModelTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val sampleJson = """
    {
      "schemaVersion": 1,
      "contentVersion": "2026-06-10.1",
      "mantras": [{
        "id": "om",
        "canonicalName": "Om (Pranava)",
        "originalLanguage": "sanskrit",
        "text": {
          "devanagari": "ॐ", "telugu": "ఓం", "tamil": "ஓம்",
          "kannada": "ಓಂ", "bengali": "ওঁ", "gujarati": "ૐ", "roman": "Om"
        },
        "meaning": {
          "en": "e", "hi": "h", "te": "t", "ta": "a",
          "kn": "k", "mr": "m", "bn": "b", "gu": "g"
        },
        "source": "Mandukya Upanishad",
        "sourceRefs": ["https://example.org/om"],
        "deity": "universal",
        "intentions": ["calm", "sadhana"],
        "estimatedReadSeconds": 5,
        "completionThresholdDays": 14
      }]
    }
    """.trimIndent()

    @Test
    fun `catalog decodes from json`() {
        val catalog = json.decodeFromString<MantraCatalog>(sampleJson)
        assertEquals(1, catalog.schemaVersion)
        assertEquals("2026-06-10.1", catalog.contentVersion)
        assertEquals(1, catalog.mantras.size)
    }

    @Test
    fun `entry fields decode with enum mapping`() {
        val m = json.decodeFromString<MantraCatalog>(sampleJson).mantras.first()
        assertEquals("om", m.id)
        assertEquals(OriginalLanguage.SANSKRIT, m.originalLanguage)
        assertEquals(Deity.UNIVERSAL, m.deity)
        assertEquals(listOf(Intention.CALM, Intention.SADHANA), m.intentions)
        assertEquals(5, m.estimatedReadSeconds)
        assertEquals(14, m.completionThresholdDays)
    }

    @Test
    fun `text forScript returns every script`() {
        val t = json.decodeFromString<MantraCatalog>(sampleJson).mantras.first().text
        assertEquals("ॐ", t.forScript(Script.DEVANAGARI))
        assertEquals("ఓం", t.forScript(Script.TELUGU))
        assertEquals("ஓம்", t.forScript(Script.TAMIL))
        assertEquals("ಓಂ", t.forScript(Script.KANNADA))
        assertEquals("ওঁ", t.forScript(Script.BENGALI))
        assertEquals("ૐ", t.forScript(Script.GUJARATI))
        assertEquals("Om", t.forScript(Script.ROMAN))
    }

    @Test
    fun `meaning forLang returns every language`() {
        val m = json.decodeFromString<MantraCatalog>(sampleJson).mantras.first().meaning
        assertEquals("e", m.forLang(MeaningLang.EN))
        assertEquals("h", m.forLang(MeaningLang.HI))
        assertEquals("t", m.forLang(MeaningLang.TE))
        assertEquals("a", m.forLang(MeaningLang.TA))
        assertEquals("k", m.forLang(MeaningLang.KN))
        assertEquals("m", m.forLang(MeaningLang.MR))
        assertEquals("b", m.forLang(MeaningLang.BN))
        assertEquals("g", m.forLang(MeaningLang.GU))
    }
}
```

- [ ] **Step 2.2: Run — verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.myniyam.app.data.MantraModelTest"`
Expected: `BUILD FAILED`, `Unresolved reference 'MantraCatalog'`.

- [ ] **Step 2.3: Write the model**

```kotlin
package com.myniyam.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MantraCatalog(
    val schemaVersion: Int,
    val contentVersion: String,
    val mantras: List<Mantra>
)

@Serializable
data class Mantra(
    val id: String,
    val canonicalName: String,
    val originalLanguage: OriginalLanguage,
    val text: MantraText,
    val meaning: MantraMeaning,
    val source: String,
    val sourceRefs: List<String>,
    val deity: Deity,
    val intentions: List<Intention>,
    val estimatedReadSeconds: Int,
    val completionThresholdDays: Int
)

@Serializable
data class MantraText(
    val devanagari: String,
    val telugu: String,
    val tamil: String,
    val kannada: String,
    val bengali: String,
    val gujarati: String,
    val roman: String
) {
    fun forScript(script: Script): String = when (script) {
        Script.DEVANAGARI -> devanagari
        Script.TELUGU -> telugu
        Script.TAMIL -> tamil
        Script.KANNADA -> kannada
        Script.BENGALI -> bengali
        Script.GUJARATI -> gujarati
        Script.ROMAN -> roman
    }
}

@Serializable
data class MantraMeaning(
    val en: String,
    val hi: String,
    val te: String,
    val ta: String,
    val kn: String,
    val mr: String,
    val bn: String,
    val gu: String
) {
    fun forLang(lang: MeaningLang): String = when (lang) {
        MeaningLang.EN -> en
        MeaningLang.HI -> hi
        MeaningLang.TE -> te
        MeaningLang.TA -> ta
        MeaningLang.KN -> kn
        MeaningLang.MR -> mr
        MeaningLang.BN -> bn
        MeaningLang.GU -> gu
    }
}

@Serializable
enum class OriginalLanguage {
    @SerialName("sanskrit") SANSKRIT,
    @SerialName("awadhi") AWADHI
}

@Serializable
enum class Deity {
    @SerialName("shiva") SHIVA,
    @SerialName("vishnu") VISHNU,
    @SerialName("devi") DEVI,
    @SerialName("ganesha") GANESHA,
    @SerialName("hanuman") HANUMAN,
    @SerialName("krishna") KRISHNA,
    @SerialName("rama") RAMA,
    @SerialName("saraswati") SARASWATI,
    @SerialName("lakshmi") LAKSHMI,
    @SerialName("universal") UNIVERSAL
}

@Serializable
enum class Intention {
    @SerialName("focus") FOCUS,
    @SerialName("calm") CALM,
    @SerialName("sadhana") SADHANA,
    @SerialName("dharma") DHARMA,
    @SerialName("devotion") DEVOTION
}

enum class Script { DEVANAGARI, TELUGU, TAMIL, KANNADA, BENGALI, GUJARATI, ROMAN }

enum class MeaningLang { EN, HI, TE, TA, KN, MR, BN, GU }
```

- [ ] **Step 2.4: Run — verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.myniyam.app.data.MantraModelTest"`
Expected: `BUILD SUCCESSFUL`, 4 tests passed.

- [ ] **Step 2.5: Commit**

```bash
git add app/src/main/java/com/myniyam/app/data/Mantra.kt app/src/test/java/com/myniyam/app/data/MantraModelTest.kt
git commit -m "feat(data): add Mantra model + enums with serialization

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
git push origin main
```

**STOP. Wait for approval (unless autonomous run granted).**

---

## Task 3: DisplayLanguage picker mapping (TDD)

**Files:**
- Test: `app/src/test/java/com/myniyam/app/data/DisplayLanguageTest.kt`
- Create: `app/src/main/java/com/myniyam/app/data/DisplayLanguage.kt`

- [ ] **Step 3.1: Write the failing test**

```kotlin
package com.myniyam.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class DisplayLanguageTest {

    @Test
    fun `nine picker values exist`() {
        assertEquals(9, DisplayLanguage.entries.size)
    }

    @Test
    fun `each picker value resolves to spec mapping`() {
        val expected = mapOf(
            DisplayLanguage.DEVANAGARI_SANSKRIT to (Script.DEVANAGARI to MeaningLang.EN),
            DisplayLanguage.HINDI to (Script.DEVANAGARI to MeaningLang.HI),
            DisplayLanguage.MARATHI to (Script.DEVANAGARI to MeaningLang.MR),
            DisplayLanguage.ENGLISH to (Script.ROMAN to MeaningLang.EN),
            DisplayLanguage.TELUGU to (Script.TELUGU to MeaningLang.TE),
            DisplayLanguage.TAMIL to (Script.TAMIL to MeaningLang.TA),
            DisplayLanguage.KANNADA to (Script.KANNADA to MeaningLang.KN),
            DisplayLanguage.BENGALI to (Script.BENGALI to MeaningLang.BN),
            DisplayLanguage.GUJARATI to (Script.GUJARATI to MeaningLang.GU),
        )
        for ((lang, pair) in expected) {
            assertEquals("script for $lang", pair.first, lang.script)
            assertEquals("meaning for $lang", pair.second, lang.meaningLang)
        }
    }
}
```

- [ ] **Step 3.2: Run — verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.myniyam.app.data.DisplayLanguageTest"`
Expected: `BUILD FAILED`, `Unresolved reference 'DisplayLanguage'`.

- [ ] **Step 3.3: Write the enum**

```kotlin
package com.myniyam.app.data

/**
 * The 9 user-facing language picker choices (spec §4) and the
 * (script, meaning-language) pair each resolves to. Single source of
 * truth for every screen that renders mantra content.
 */
enum class DisplayLanguage(val script: Script, val meaningLang: MeaningLang) {
    DEVANAGARI_SANSKRIT(Script.DEVANAGARI, MeaningLang.EN),
    HINDI(Script.DEVANAGARI, MeaningLang.HI),
    MARATHI(Script.DEVANAGARI, MeaningLang.MR),
    ENGLISH(Script.ROMAN, MeaningLang.EN),
    TELUGU(Script.TELUGU, MeaningLang.TE),
    TAMIL(Script.TAMIL, MeaningLang.TA),
    KANNADA(Script.KANNADA, MeaningLang.KN),
    BENGALI(Script.BENGALI, MeaningLang.BN),
    GUJARATI(Script.GUJARATI, MeaningLang.GU)
}
```

- [ ] **Step 3.4: Run — verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.myniyam.app.data.DisplayLanguageTest"`
Expected: `BUILD SUCCESSFUL`, 2 tests passed.

- [ ] **Step 3.5: Commit**

```bash
git add app/src/main/java/com/myniyam/app/data/DisplayLanguage.kt app/src/test/java/com/myniyam/app/data/DisplayLanguageTest.kt
git commit -m "feat(data): add DisplayLanguage picker mapping (9 options -> 7 scripts + 8 meanings)

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
git push origin main
```

**STOP. Wait for approval (unless autonomous run granted).**

---

## Task 4: MantraRepository (TDD)

**Files:**
- Test: `app/src/test/java/com/myniyam/app/data/MantraRepositoryTest.kt`
- Create: `app/src/main/java/com/myniyam/app/data/MantraRepository.kt`

Design: parsing core is pure-JVM (`initFromJson(String)`), Android asset loading is a thin `ensureLoaded(Context)` wrapper. Fallback is a complete built-in `om` entry; `displayMantra(id)` never returns null.

- [ ] **Step 4.1: Write the failing test**

```kotlin
package com.myniyam.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MantraRepositoryTest {

    private val validJson = """
    {
      "schemaVersion": 1,
      "contentVersion": "2026-06-10.1",
      "mantras": [{
        "id": "om",
        "canonicalName": "Om (Pranava)",
        "originalLanguage": "sanskrit",
        "text": {"devanagari": "ॐ", "telugu": "ఓం", "tamil": "ஓம்",
                 "kannada": "ಓಂ", "bengali": "ওঁ", "gujarati": "ૐ", "roman": "Om"},
        "meaning": {"en": "e", "hi": "h", "te": "t", "ta": "a",
                    "kn": "k", "mr": "m", "bn": "b", "gu": "g"},
        "source": "Mandukya Upanishad",
        "sourceRefs": ["https://example.org/om"],
        "deity": "universal",
        "intentions": ["calm"],
        "estimatedReadSeconds": 5,
        "completionThresholdDays": 14
      }]
    }
    """.trimIndent()

    @Before
    fun reset() {
        MantraRepository.resetForTest()
    }

    @Test
    fun `initFromJson succeeds on valid catalog`() {
        assertTrue(MantraRepository.initFromJson(validJson))
        assertEquals(1, MantraRepository.all().size)
    }

    @Test
    fun `byId returns entry and null for unknown`() {
        MantraRepository.initFromJson(validJson)
        assertEquals("Om (Pranava)", MantraRepository.byId("om")?.canonicalName)
        assertNull(MantraRepository.byId("nope"))
    }

    @Test
    fun `initFromJson fails on corrupt json without throwing`() {
        assertFalse(MantraRepository.initFromJson("{ not json"))
        assertTrue(MantraRepository.all().isEmpty())
    }

    @Test
    fun `initFromJson rejects wrong schemaVersion`() {
        val wrong = validJson.replace("\"schemaVersion\": 1", "\"schemaVersion\": 99")
        assertFalse(MantraRepository.initFromJson(wrong))
    }

    @Test
    fun `displayMantra falls back to built-in om when not loaded`() {
        val m = MantraRepository.displayMantra("gayatri")
        assertEquals("om", m.id)
        assertEquals("ॐ", m.text.devanagari)
    }

    @Test
    fun `displayMantra returns requested entry when loaded`() {
        MantraRepository.initFromJson(validJson)
        assertEquals("om", MantraRepository.displayMantra("om").id)
    }

    @Test
    fun `displayMantra falls back for unknown id even when loaded`() {
        MantraRepository.initFromJson(validJson)
        assertEquals("om", MantraRepository.displayMantra("does-not-exist").id)
    }
}
```

- [ ] **Step 4.2: Run — verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.myniyam.app.data.MantraRepositoryTest"`
Expected: `BUILD FAILED`, `Unresolved reference 'MantraRepository'`.

- [ ] **Step 4.3: Write the repository**

```kotlin
package com.myniyam.app.data

import android.content.Context
import android.util.Log
import kotlinx.serialization.json.Json

/**
 * Parse-once, in-memory, immutable content store (spec §7).
 * Never throws to callers; on any failure serves the built-in om FALLBACK.
 */
object MantraRepository {

    private const val TAG = "MantraRepository"
    private const val ASSET_PATH = "content/mantras.json"
    private const val SUPPORTED_SCHEMA_VERSION = 1

    @Volatile private var catalog: MantraCatalog? = null
    @Volatile private var loadAttempted = false

    private val json = Json { ignoreUnknownKeys = true }

    /** Built-in emergency entry — the overlay can never crash from bad content (spec §8). */
    val FALLBACK = Mantra(
        id = "om",
        canonicalName = "Om (Pranava)",
        originalLanguage = OriginalLanguage.SANSKRIT,
        text = MantraText(
            devanagari = "ॐ", telugu = "ఓం", tamil = "ஓம்",
            kannada = "ಓಂ", bengali = "ওঁ", gujarati = "ૐ", roman = "Om"
        ),
        meaning = MantraMeaning(
            en = "The primordial sound of the universe.",
            hi = "ब्रह्मांड की आदि ध्वनि।",
            te = "విశ్వపు ఆది నాదం.",
            ta = "பிரபஞ்சத்தின் ஆதி ஒலி.",
            kn = "ವಿಶ್ವದ ಆದಿ ನಾದ.",
            mr = "विश्वाचा आदिनाद.",
            bn = "বিশ্বের আদি ধ্বনি।",
            gu = "બ્રહ્માંડનો આદિ નાદ."
        ),
        source = "Mandukya Upanishad",
        sourceRefs = listOf("https://sanskritdocuments.org/doc_upanishhat/mandukya.html"),
        deity = Deity.UNIVERSAL,
        intentions = listOf(Intention.CALM, Intention.SADHANA),
        estimatedReadSeconds = 5,
        completionThresholdDays = 14
    )

    /** Pure-JVM parsing core. Returns false (and clears state) on any failure. */
    fun initFromJson(jsonText: String): Boolean {
        return try {
            val parsed = json.decodeFromString<MantraCatalog>(jsonText)
            if (parsed.schemaVersion != SUPPORTED_SCHEMA_VERSION) {
                Log.e(TAG, "Unsupported schemaVersion ${parsed.schemaVersion}")
                catalog = null
                false
            } else {
                catalog = parsed
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse mantra catalog", e)
            catalog = null
            false
        }
    }

    /** Android entry point: loads the bundled asset once. Safe to call repeatedly. */
    fun ensureLoaded(context: Context) {
        if (catalog != null || loadAttempted) return
        synchronized(this) {
            if (catalog != null || loadAttempted) return
            loadAttempted = true
            val text = try {
                context.assets.open(ASSET_PATH).bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read $ASSET_PATH", e)
                return
            }
            initFromJson(text)
        }
    }

    fun all(): List<Mantra> = catalog?.mantras ?: emptyList()

    fun byId(id: String): Mantra? = catalog?.mantras?.firstOrNull { it.id == id }

    /** Overlay-facing accessor: always returns a renderable entry. */
    fun displayMantra(id: String): Mantra = byId(id) ?: FALLBACK

    /** Test hook — clears parsed state between unit tests. */
    fun resetForTest() {
        catalog = null
        loadAttempted = false
    }
}
```

- [ ] **Step 4.4: Run — verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.myniyam.app.data.MantraRepositoryTest"`
Expected: `BUILD SUCCESSFUL`, 7 tests passed. (`Log.e` returns 0 in JVM tests via `isReturnDefaultValues` from Task 1.)

- [ ] **Step 4.5: Commit**

```bash
git add app/src/main/java/com/myniyam/app/data/MantraRepository.kt app/src/test/java/com/myniyam/app/data/MantraRepositoryTest.kt
git commit -m "feat(data): add MantraRepository with om fallback

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
git push origin main
```

**STOP. Wait for approval (unless autonomous run granted).**

---

## Task 5: Python generation tool (Aksharamukha) + calibration

**Files:**
- Create: `tools/generate_scripts.py`
- Create: `tools/requirements.txt`
- Create: `tools/README.md`

- [ ] **Step 5.1: Check Python availability**

Run: `python3 --version`
Expected: `Python 3.9` or newer. If missing → STOP, surface to Pranav (Homebrew `brew install python` is the fix, but get approval first).

- [ ] **Step 5.2: Create `tools/requirements.txt`**

```
aksharamukha==2.2.1
```
If `pip` later reports the version doesn't exist, run `python3 -m pip index versions aksharamukha`, pin the newest 2.x, and update this file in the same commit.

- [ ] **Step 5.3: Create venv + install**

```bash
cd "/Users/pranavaditya/projects/Hindu Distraction App/tools"
python3 -m venv .venv
.venv/bin/pip install -r requirements.txt
```
Expected: `Successfully installed aksharamukha-...`. Add `tools/.venv/` to `.gitignore` (append line `tools/.venv/`).

- [ ] **Step 5.4: Create `tools/generate_scripts.py`**

```python
#!/usr/bin/env python3
"""Regenerate derived script fields in mantras.json from each entry's
Devanagari master. Deterministic: same input + same config = same output.

Usage:  .venv/bin/python generate_scripts.py [--check]
  --check : verify derived fields match what would be generated (CI-style); exit 1 on drift.

Config is FROZEN after calibration (Step 5.5) — do not edit TARGETS without
re-running the spot-check (plan Task 13)."""

import argparse
import json
import sys
from pathlib import Path

from aksharamukha import transliterate

MANTRAS_JSON = Path(__file__).resolve().parent.parent / "app/src/main/assets/content/mantras.json"

SRC = "Devanagari"

# field -> (aksharamukha target, pre_options, post_options)
# Tamil: Aksharamukha's Tamil target uses Grantha consonants (ஜ ஷ ஸ ஹ) per
# standard devotional convention. Calibration (Step 5.5) locks post_options.
TARGETS = {
    "telugu":   ("Telugu",        [], []),
    "tamil":    ("Tamil",         [], ["TamilRemoveApostrophe", "TamilRemoveNumbers"]),
    "kannada":  ("Kannada",       [], []),
    "bengali":  ("Bengali",       [], []),
    "gujarati": ("Gujarati",      [], []),
    "roman":    ("RomanReadable", [], []),
}


def derive(devanagari: str) -> dict:
    out = {}
    for field, (target, pre, post) in TARGETS.items():
        out[field] = transliterate.process(
            SRC, target, devanagari, pre_options=pre, post_options=post
        ).strip()
    return out


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()

    data = json.loads(MANTRAS_JSON.read_text(encoding="utf-8"))
    drift = []
    for m in data["mantras"]:
        derived = derive(m["text"]["devanagari"])
        for field, value in derived.items():
            if m["text"].get(field) != value:
                drift.append(f'{m["id"]}.text.{field}')
                m["text"][field] = value

    if args.check:
        if drift:
            print("DRIFT in derived fields:\n  " + "\n  ".join(drift))
            return 1
        print("OK: all derived fields match generation config.")
        return 0

    MANTRAS_JSON.write_text(
        json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    print(f"Regenerated {len(drift)} field(s) across {len(data['mantras'])} entries.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
```

- [ ] **Step 5.5: Calibration run on known strings**

Create a throwaway check (run from `tools/`, does not touch the repo):
```bash
.venv/bin/python - <<'EOF'
from aksharamukha import transliterate
gayatri = "ॐ भूर्भुवः स्वः । तत्सवितुर्वरेण्यं भर्गो देवस्य धीमहि । धियो यो नः प्रचोदयात् ॥"
for tgt in ["Telugu", "Tamil", "Kannada", "Bengali", "Gujarati", "RomanReadable"]:
    print(tgt, "→", transliterate.process("Devanagari", tgt, gayatri))
EOF
```
Compare each line by eye against the Gayatri Mantra pages on vignanam.org and stotranidhi.com (open in browser). Checks: Tamil shows Grantha ஸ/ஹ where expected; no stray apostrophes or digits; roman is readable without diacritics. If an option needs changing (e.g., Tamil post_options), edit `TARGETS`, re-run, and record the final config rationale in `tools/README.md`. If an option name errors, list valid ones with `python3 -c "import aksharamukha.transliterate as t; help(t.process)"` and consult https://github.com/virtualvinodh/aksharamukha-python.

- [ ] **Step 5.6: Create `tools/README.md`**

```markdown
# Niyam content tooling

`generate_scripts.py` regenerates the 6 derived script fields (telugu, tamil,
kannada, bengali, gujarati, roman) in `app/src/main/assets/content/mantras.json`
from each entry's `text.devanagari` master, via Aksharamukha.

- Setup: `python3 -m venv .venv && .venv/bin/pip install -r requirements.txt`
- Regenerate: `.venv/bin/python generate_scripts.py`
- Verify no drift: `.venv/bin/python generate_scripts.py --check`

Config notes (frozen at calibration, 2026-06-DD):
- Tamil: Grantha consonants per standard devotional convention; post_options
  strip apostrophes/superscript numerals.
- Roman: Aksharamukha "RomanReadable" (no diacritics) per spec §3.
- Record any config change here AND re-run the spot-check
  (docs/superpowers/test-reports/).

Aksharamukha is AGPL — authoring-time tool only; never ships in the APK.
```
(Replace `2026-06-DD` with the actual calibration date.)

- [ ] **Step 5.7: Commit**

```bash
git add tools/generate_scripts.py tools/requirements.txt tools/README.md .gitignore
git commit -m "feat(tools): add Aksharamukha script-generation tool (offline, authoring-time)

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
git push origin main
```

**STOP. Wait for approval (unless autonomous run granted).**

---

## Task 6: mantras.json scaffold + complete om entry + ContentValidationTest

**Files:**
- Create: `app/src/main/assets/content/mantras.json`
- Test: `app/src/test/java/com/myniyam/app/data/ContentValidationTest.kt`

- [ ] **Step 6.1: Create the asset with the complete om entry**

`app/src/main/assets/content/mantras.json` — this is the structural template every later entry follows:

```json
{
  "schemaVersion": 1,
  "contentVersion": "2026-06-10.1",
  "mantras": [
    {
      "id": "om",
      "canonicalName": "Om (Pranava)",
      "originalLanguage": "sanskrit",
      "text": {
        "devanagari": "ॐ",
        "telugu": "ఓం",
        "tamil": "ஓம்",
        "kannada": "ಓಂ",
        "bengali": "ওঁ",
        "gujarati": "ૐ",
        "roman": "Om"
      },
      "meaning": {
        "en": "The primordial sound of the universe. One syllable holding creation, preservation, and dissolution — a single breath that settles the mind.",
        "hi": "ब्रह्मांड की आदि ध्वनि। एक अक्षर में सृष्टि, स्थिति और लय समाए हैं — एक सांस जो मन को शांत कर देती है।",
        "te": "విశ్వపు ఆది నాదం. సృష్టి, స్థితి, లయలను తనలో ఇముడ్చుకున్న ఏకాక్షరం — మనసును నిలిపే ఒక్క శ్వాస.",
        "ta": "பிரபஞ்சத்தின் ஆதி ஒலி. படைத்தல், காத்தல், ஒடுக்கம் அனைத்தையும் உள்ளடக்கிய ஓர் எழுத்து — மனதை அமைதிப்படுத்தும் ஒரு மூச்சு.",
        "kn": "ವಿಶ್ವದ ಆದಿ ನಾದ. ಸೃಷ್ಟಿ, ಸ್ಥಿತಿ, ಲಯಗಳನ್ನು ಒಳಗೊಂಡ ಏಕಾಕ್ಷರ — ಮನಸ್ಸನ್ನು ನೆಲೆಗೊಳಿಸುವ ಒಂದು ಉಸಿರು.",
        "mr": "विश्वाचा आदिनाद. उत्पत्ती, स्थिती आणि लय सामावणारे एकाक्षर — मनाला स्थिर करणारा एक श्वास.",
        "bn": "বিশ্বের আদি ধ্বনি। সৃষ্টি, স্থিতি ও লয় ধারণ করা এক অক্ষর — এক নিঃশ্বাসে মনকে স্থির করে।",
        "gu": "બ્રહ્માંડનો આદિ નાદ. સર્જન, સ્થિતિ અને લયને સમાવતો એક અક્ષર — મનને સ્થિર કરતો એક શ્વાસ."
      },
      "source": "Mandukya Upanishad",
      "sourceRefs": [
        "https://sanskritdocuments.org/doc_upanishhat/mandukya.html"
      ],
      "deity": "universal",
      "intentions": ["calm", "sadhana"],
      "estimatedReadSeconds": 5,
      "completionThresholdDays": 14
    }
  ]
}
```

Note: om's script fields are hand-seeded; the tool may normalize them on its first full run (e.g., Bengali anusvara form) — that's expected and correct; the tool's output wins.

- [ ] **Step 6.2: Write `ContentValidationTest`**

This is the mechanical review gate (spec §8). It reads the real asset off disk (JVM test, no Robolectric). Count assertion starts at ≥1 and is flipped to ==26 in Task 12.

```kotlin
package com.myniyam.app.data

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ContentValidationTest {

    private val assetFile = File("src/main/assets/content/mantras.json")
    private val catalog: MantraCatalog by lazy {
        Json { ignoreUnknownKeys = true }.decodeFromString(assetFile.readText())
    }

    @Test
    fun `asset exists and parses`() {
        assertTrue("asset missing at ${assetFile.absolutePath}", assetFile.exists())
        assertEquals(1, catalog.schemaVersion)
        assertTrue("contentVersion must be set", catalog.contentVersion.isNotBlank())
    }

    @Test
    fun `entry count`() {
        // Flipped to assertEquals(26, ...) in Task 12 once all batches land.
        assertTrue("at least one entry", catalog.mantras.isNotEmpty())
    }

    @Test
    fun `ids are unique kebab-case`() {
        val ids = catalog.mantras.map { it.id }
        assertEquals("duplicate ids", ids.size, ids.toSet().size)
        ids.forEach { id ->
            assertTrue("id '$id' must be lowercase-kebab", id.matches(Regex("[a-z0-9]+(-[a-z0-9]+)*")))
        }
    }

    @Test
    fun `every entry has all scripts non-empty`() {
        catalog.mantras.forEach { m ->
            Script.entries.forEach { s ->
                assertTrue("${m.id}: blank script $s", m.text.forScript(s).isNotBlank())
            }
        }
    }

    @Test
    fun `every entry has all meanings non-empty`() {
        catalog.mantras.forEach { m ->
            MeaningLang.entries.forEach { l ->
                assertTrue("${m.id}: blank meaning $l", m.meaning.forLang(l).isNotBlank())
            }
        }
    }

    @Test
    fun `every entry has source attribution and at least one sourceRef`() {
        catalog.mantras.forEach { m ->
            assertTrue("${m.id}: blank source", m.source.isNotBlank())
            assertTrue("${m.id}: needs >=1 sourceRef", m.sourceRefs.isNotEmpty())
        }
    }

    @Test
    fun `every entry has at least one intention and positive numbers`() {
        catalog.mantras.forEach { m ->
            assertTrue("${m.id}: needs >=1 intention", m.intentions.isNotEmpty())
            assertTrue("${m.id}: read seconds > 0", m.estimatedReadSeconds > 0)
            assertTrue("${m.id}: threshold > 0", m.completionThresholdDays > 0)
        }
    }

    @Test
    fun `asset stays within size budget`() {
        assertTrue("mantras.json must be <= 400KB", assetFile.length() <= 400 * 1024)
    }
}
```

- [ ] **Step 6.3: Run the validation test**

Run: `./gradlew :app:testDebugUnitTest --tests "com.myniyam.app.data.ContentValidationTest"`
Expected: `BUILD SUCCESSFUL`, 8 tests passed.

- [ ] **Step 6.4: Run the generation tool in --check mode against om**

```bash
cd tools && .venv/bin/python generate_scripts.py --check; cd ..
```
Expected: either `OK` or a listed drift on om's hand-seeded fields. If drift: run without `--check` to let the tool normalize, eyeball the diff (`git diff app/src/main/assets/content/mantras.json`), re-run the validation test.

- [ ] **Step 6.5: Commit**

```bash
git add app/src/main/assets/content/mantras.json app/src/test/java/com/myniyam/app/data/ContentValidationTest.kt
git commit -m "feat(content): scaffold mantras.json with complete om entry + validation gate

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
git push origin main
```

**STOP. Wait for approval (unless autonomous run granted).**

---

## Tasks 7-11: Content batches (one task per intention group)

These five tasks share one structure; each authors 5 entries end-to-end. Metadata below is FINAL (from spec §5); the scripture text and meanings are authored at execution time following the om template from Task 6. Per-batch steps:

> **Step a — author masters:** for each entry, write `id`, `canonicalName`, `originalLanguage`, `source`, `deity`, `intentions`, `estimatedReadSeconds`, `completionThresholdDays` exactly as in the batch table, then author `text.devanagari` by consulting BOTH named sources, resolving any character-level differences toward the more authoritative edition, and recording the two real page URLs in `sourceRefs`. Set the six derived script fields to `"pending"` (placeholder the tool overwrites in step b — never committed: step d gates).
> **Step b — generate scripts:** `cd tools && .venv/bin/python generate_scripts.py && cd ..` then eyeball `git diff` for the new entries (no mojibake, no empty fields).
> **Step c — author meanings:** write `meaning.en` (2-3 accessible lines, consistent voice with om), then translate into hi, te, ta, kn, mr, bn, gu — tone-matched, simple sentences.
> **Step d — validate:** `./gradlew :app:testDebugUnitTest --tests "com.myniyam.app.data.ContentValidationTest"` → all green (this catches any leftover "pending").
> **Step e — commit + push** with message `feat(content): add <group> batch (5 entries)` + the standard Co-Authored-By trailer.
> **STOP after each batch. Wait for approval (unless autonomous run granted).**

Source guidance (verification corpus): Bhagavad Gita verses → gitasupersite.iitk.ac.in (IIT Kanpur) + sanskritdocuments.org; Vedic suktas/mantras → sanskritdocuments.org + vedicheritage.gov.in; stotras/vandanas → sanskritdocuments.org + stotranidhi.com (or vignanam.org); Hanuman Chalisa → Gita Press edition text cross-checked with two independent renderings.

### Task 7 — Batch A: Focus

| id | canonicalName | source | origLang | deity | intentions | readS | threshold |
|---|---|---|---|---|---|---|---|
| `gita-2-47` | Karmanyevadhikaraste | Bhagavad Gita 2.47 | sanskrit | krishna | focus, dharma | 20 | 14 |
| `gita-6-5` | Uddhared Atmanatmanam | Bhagavad Gita 6.5 | sanskrit | krishna | focus | 20 | 14 |
| `gita-2-14` | Matra-sparshas Tu | Bhagavad Gita 2.14 | sanskrit | krishna | focus, calm | 20 | 14 |
| `asato-ma` | Asato Ma Sadgamaya | Brihadaranyaka Upanishad 1.3.28 | sanskrit | universal | focus, sadhana | 15 | 14 |
| `gita-6-6` | Bandhur Atmatmanas | Bhagavad Gita 6.6 | sanskrit | krishna | focus | 20 | 14 |

- [ ] **Step 0 (Task-6 quality-review carry-forward): strengthen ContentValidationTest before authoring.** Add to `app/src/test/java/com/myniyam/app/data/ContentValidationTest.kt` (inside the class) these two tests + helpers, bringing it to 10 tests:

```kotlin
    private val scriptBlocks = mapOf(
        Script.DEVANAGARI to 0x0900..0x097F,
        Script.TELUGU to 0x0C00..0x0C7F,
        Script.TAMIL to 0x0B80..0x0BFF,
        Script.KANNADA to 0x0C80..0x0CFF,
        Script.BENGALI to 0x0980..0x09FF,
        Script.GUJARATI to 0x0A80..0x0AFF,
    )

    // Shared characters allowed in any script field: whitespace, ASCII
    // punctuation/digits (danda maps to "." in most scripts), Devanagari
    // dandas (। ॥), ZWJ/ZWNJ, and the calibrated Tamil visarga ꞉ (U+A789).
    // ASCII letters are NOT shared — Latin text in an Indic field is a paste error.
    private fun isSharedChar(c: Char): Boolean =
        c.isWhitespace() || (c.code < 0x80 && !c.isLetter()) || c == '।' || c == '॥' ||
            c.code == 0x200C || c.code == 0x200D || c.code == 0xA789

    @Test
    fun `script fields contain only their own script`() {
        catalog.mantras.forEach { m ->
            scriptBlocks.forEach { (script, range) ->
                m.text.forScript(script).forEach { c ->
                    assertTrue(
                        "${m.id}.$script: stray char '$c' (U+${"%04X".format(c.code)})",
                        isSharedChar(c) || c.code in range
                    )
                }
            }
        }
    }

    @Test
    fun `meanings have plausible length`() {
        catalog.mantras.forEach { m ->
            MeaningLang.entries.forEach { l ->
                assertTrue(
                    "${m.id}: meaning $l suspiciously short (truncation?)",
                    m.meaning.forLang(l).length >= 20
                )
            }
        }
    }
```

(The roman field is deliberately not block-checked — it is ASCII by construction from the tool.) Run the validation test (10/10 on the om-only catalog) BEFORE starting step a.

- [ ] Steps a-e as defined above. (Files for this task additionally include `ContentValidationTest.kt` per step 0.)

### Task 8 — Batch B: Calm

| id | canonicalName | source | origLang | deity | intentions | readS | threshold |
|---|---|---|---|---|---|---|---|
| `mahamrityunjaya` | Mahamrityunjaya Mantra | Rig Veda 7.59.12 | sanskrit | shiva | calm, devotion | 25 | 14 |
| `om-sahanavavatu` | Om Sahanavavatu (Shanti Mantra) | Taittiriya Upanishad (invocation) | sanskrit | universal | calm, sadhana | 20 | 14 |
| `om-namah-shivaya` | Om Namah Shivaya | Panchakshara — Sri Rudram (Yajur Veda) | sanskrit | shiva | calm, devotion | 10 | 14 |
| `gita-2-70` | Apuryamanam | Bhagavad Gita 2.70 | sanskrit | krishna | calm | 20 | 14 |
| `twameva-mata` | Twameva Mata | Traditional prayer (Pandava Gita) | sanskrit | universal | calm, devotion | 15 | 14 |

- [ ] Steps a-e as defined above.

### Task 9 — Batch C: Sadhana

| id | canonicalName | source | origLang | deity | intentions | readS | threshold |
|---|---|---|---|---|---|---|---|
| `gayatri` | Gayatri Mantra | Rig Veda 3.62.10 | sanskrit | universal | sadhana, focus | 30 | 14 |
| `vakratunda` | Vakratunda Mahakaya | Traditional Ganesha Vandana | sanskrit | ganesha | sadhana, focus | 15 | 14 |
| `saraswati-vandana` | Ya Kundendu Tushara Hara Dhavala | Saraswati Stotram (traditional) | sanskrit | saraswati | sadhana, focus | 30 | 14 |
| `guru-brahma` | Guru Brahma Guru Vishnu | Guru Stotram (traditional) | sanskrit | universal | sadhana, devotion | 15 | 14 |
| `hare-krishna` | Hare Krishna Mahamantra | Kali-Santarana Upanishad | sanskrit | krishna | sadhana, devotion | 20 | 14 |

- [ ] Steps a-e as defined above.

### Task 10 — Batch D: Dharma

| id | canonicalName | source | origLang | deity | intentions | readS | threshold |
|---|---|---|---|---|---|---|---|
| `gita-4-7-8` | Yada Yada Hi Dharmasya | Bhagavad Gita 4.7-8 | sanskrit | krishna | dharma | 35 | 14 |
| `gita-18-66` | Sarva-dharman Parityajya | Bhagavad Gita 18.66 | sanskrit | krishna | dharma, devotion | 20 | 14 |
| `gita-3-35` | Shreyan Svadharmo | Bhagavad Gita 3.35 | sanskrit | krishna | dharma, focus | 20 | 14 |
| `purusha-suktam` | Purusha Suktam (opening verse) | Rig Veda 10.90.1 | sanskrit | universal | dharma, sadhana | 25 | 14 |
| `nasadiya-suktam` | Nasadiya Suktam (opening verse) | Rig Veda 10.129.1 | sanskrit | universal | dharma | 25 | 14 |

- [ ] Steps a-e as defined above.

### Task 11 — Batch E: Devotion

| id | canonicalName | source | origLang | deity | intentions | readS | threshold |
|---|---|---|---|---|---|---|---|
| `hanuman-chalisa-opening` | Hanuman Chalisa (Doha + chaupais 1-5) | Hanuman Chalisa — Tulsidas | **awadhi** | hanuman | devotion | 90 | 14 |
| `vishnu-sahasranama-opening` | Vishnu Sahasranama (verses 1-8) | Mahabharata, Anushasana Parva | sanskrit | vishnu | devotion | 120 | 14 |
| `lalita-sahasranama-opening` | Lalita Sahasranama (verses 1-8) | Brahmanda Purana | sanskrit | devi | devotion | 120 | 14 |
| `krishna-ashtakam` | Krishna Ashtakam (verse 1) | Traditional (attr. Adi Shankaracharya) | sanskrit | krishna | devotion | 20 | 14 |
| `ram-raksha-opening` | Ram Raksha Stotra (opening) | Budha Kaushika | sanskrit | rama | devotion, calm | 30 | 14 |

- [ ] Steps a-e as defined above. Note: `hanuman-chalisa-opening` is Awadhi — `originalLanguage: "awadhi"`, and its Devanagari master is the Awadhi text (the tool transliterates it like any other Devanagari input).

---

## Task 12: Flip validation to the full 26-entry contract

**Files:**
- Modify: `app/src/test/java/com/myniyam/app/data/ContentValidationTest.kt`

- [ ] **Step 12.1: Replace the `entry count` test**

Replace:
```kotlin
    @Test
    fun `entry count`() {
        // Flipped to assertEquals(26, ...) in Task 12 once all batches land.
        assertTrue("at least one entry", catalog.mantras.isNotEmpty())
    }
```
with:
```kotlin
    @Test
    fun `exactly the 26 spec entries exist`() {
        val expectedIds = setOf(
            "om",
            "gita-2-47", "gita-6-5", "gita-2-14", "asato-ma", "gita-6-6",
            "mahamrityunjaya", "om-sahanavavatu", "om-namah-shivaya", "gita-2-70", "twameva-mata",
            "gayatri", "vakratunda", "saraswati-vandana", "guru-brahma", "hare-krishna",
            "gita-4-7-8", "gita-18-66", "gita-3-35", "purusha-suktam", "nasadiya-suktam",
            "hanuman-chalisa-opening", "vishnu-sahasranama-opening", "lalita-sahasranama-opening",
            "krishna-ashtakam", "ram-raksha-opening"
        )
        assertEquals(26, catalog.mantras.size)
        assertEquals(expectedIds, catalog.mantras.map { it.id }.toSet())
    }
```
(`assertTrue` import stays — other tests use it.)

- [ ] **Step 12.2: Run full suite + generation drift check**

```bash
./gradlew :app:testDebugUnitTest
cd tools && .venv/bin/python generate_scripts.py --check; cd ..
```
Expected: all tests pass; `OK: all derived fields match generation config.`

- [ ] **Step 12.3: Commit**

```bash
git add app/src/test/java/com/myniyam/app/data/ContentValidationTest.kt
git commit -m "test(content): enforce full 26-entry contract

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
git push origin main
```

**STOP. Wait for approval (unless autonomous run granted).**

---

## Task 13: Spot-check log vs external native-script sources

**Files:**
- Create: `docs/superpowers/test-reports/2026-06-DD-content-spotcheck.md` (DD = execution date)

- [ ] **Step 13.1: Pick the sample**

3 entries per generated script, biased toward the hardest cases: `gayatri` (Vedic), `vishnu-sahasranama-opening` (long, conjunct-heavy), `hanuman-chalisa-opening` (Awadhi) — same 3 across telugu, tamil, kannada, bengali, gujarati; for roman, eyeball readability instead (no diacritics, brief-style spelling).

- [ ] **Step 13.2: Compare against vignanam.org / stotranidhi.com**

For each (entry, script) pair fetch the corresponding page on either site and compare character-by-character against our generated field. Classify every difference as **convention** (site chose a different valid rendering — explain which convention) or **error** (our output is wrong — fix the master or the tool config, regenerate, re-validate).

**Task-5 carry-forward (calibration findings, 2026-06-10):** (a) Both sites 403 direct automated fetch — use web search to surface the native-script text, or browser-based fetching; (b) **Bengali and Gujarati are the priority scripts** — they could NOT be cross-checked at calibration (no clean reference text found), while Telugu/Tamil/Kannada/roman verified clean against community sources; (c) two accepted convention choices to re-confirm on long texts: Tamil visarga rendered as `꞉` (U+A789) and om as the `ௐ` glyph; (d) roman scheme is `RomanColloquial` (not `RomanReadable` — produced apostrophes/doubled vowels).

- [ ] **Step 13.3: Write the log**

```markdown
# Content spot-check — generated scripts vs native devotional sources

- **Date:** 2026-06-DD
- **Tool config:** tools/generate_scripts.py TARGETS as of commit <hash>
- **Sample:** gayatri, vishnu-sahasranama-opening, hanuman-chalisa-opening × {telugu, tamil, kannada, bengali, gujarati} + roman readability pass

| Entry | Script | Source URL | Verdict | Notes |
|---|---|---|---|---|
| gayatri | telugu | <url> | match / convention / error→fixed | <one line> |
| ... (15 rows) | | | | |

## Roman readability pass
<2-3 sentences>

## Outcome
<N match, M convention differences (explained), K errors (all fixed + regenerated)>
```

- [ ] **Step 13.4: Commit**

```bash
git add docs/superpowers/test-reports/
git commit -m "test(content): spot-check log — generated scripts vs native sources

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
git push origin main
```

**STOP. Wait for approval (unless autonomous run granted).**

---

## Task 14: Overlay integration + emulator verification

**Files:**
- Create: `app/src/main/java/com/myniyam/app/data/CurrentSadhana.kt`
- Modify: `app/src/main/java/com/myniyam/app/overlay/OverlayManager.kt`
- Modify: `app/src/main/java/com/myniyam/app/NiyamApplication.kt`

- [ ] **Step 14.1: Create `CurrentSadhana.kt`**

```kotlin
package com.myniyam.app.data

/**
 * Stand-in for the user's sadhana selection until onboarding (SP-3) exists.
 * SP-3 replaces these constants with persisted user choices.
 */
object CurrentSadhana {
    const val MANTRA_ID: String = "gayatri"
    val LANGUAGE: DisplayLanguage = DisplayLanguage.DEVANAGARI_SANSKRIT
}
```

- [ ] **Step 14.2: Rewire `OverlayManager.show()`**

In `OverlayManager.kt`, replace the import:
```kotlin
import com.myniyam.app.data.PlaceholderMantra
```
with:
```kotlin
import com.myniyam.app.data.CurrentSadhana
import com.myniyam.app.data.MantraRepository
import com.myniyam.app.data.Script
```
Replace the three text-binding lines and the countdown seed in `show()`:
```kotlin
        view.findViewById<TextView>(R.id.overlay_devanagari).text = PlaceholderMantra.DEVANAGARI
        view.findViewById<TextView>(R.id.overlay_transliteration).text = PlaceholderMantra.TRANSLITERATION
        view.findViewById<TextView>(R.id.overlay_meaning).text = PlaceholderMantra.ENGLISH_MEANING
```
with:
```kotlin
        MantraRepository.ensureLoaded(ctx)
        val mantra = MantraRepository.displayMantra(CurrentSadhana.MANTRA_ID)
        val lang = CurrentSadhana.LANGUAGE
        view.findViewById<TextView>(R.id.overlay_devanagari).text = mantra.text.forScript(lang.script)
        view.findViewById<TextView>(R.id.overlay_transliteration).text = mantra.text.forScript(Script.ROMAN)
        view.findViewById<TextView>(R.id.overlay_meaning).text = mantra.meaning.forLang(lang.meaningLang)
```
In `startTimer(...)` and the countdown seed, replace both uses of `PlaceholderMantra.UNLOCK_TIMER_SECONDS` with a local constant added to `OverlayManager`:
```kotlin
    private const val UNLOCK_TIMER_SECONDS = 15
```
(The timer is the engine's rule, not the content's — spec §9 keeps it fixed.)

`PlaceholderMantra.kt` becomes unused. Do NOT delete it in this task (deletion needs Pranav's explicit confirmation per global rules) — add a header comment:
```kotlin
// Superseded by MantraRepository.FALLBACK (SP-2). Safe to delete after Pranav confirms.
```

- [ ] **Step 14.2b: Warm the repository off the main thread (Task-4 quality-review carry-forward)**

`OverlayManager.show()` runs on the main thread (Service.onStartCommand dispatch), so the first parse must be pre-warmed. In `NiyamApplication.kt`, add the import `com.myniyam.app.data.MantraRepository` and change `onCreate()` to:

```kotlin
override fun onCreate() {
    super.onCreate()
    registerForegroundServiceChannel()
    Thread { MantraRepository.ensureLoaded(this) }.start()
}
```

The `ensureLoaded` call inside `OverlayManager.show()` stays — it's idempotent and free once loaded (belt-and-suspenders if the warm-up thread hasn't finished on a cold start; worst case is the designed fallback for one overlay).

- [ ] **Step 14.3: Full regression + build**

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```
Expected: BUILD SUCCESSFUL, all tests green (20 pre-existing + 4 model + 2 display-language + 7 repository + 10 validation = **43 total**).

- [ ] **Step 14.4: Emulator verification (acceptance criteria 4 + 5)**

```bash
./gradlew :app:installDebug
```
On the Pixel 9 emulator: open YouTube → overlay must show Gayatri (3-line Devanagari, "Om Bhur Bhuvah Svah…" roman, English meaning), countdown from 15, Continue at 0.
Font sanity: temporarily change `CurrentSadhana.LANGUAGE` to `DisplayLanguage.TELUGU`, reinstall, open YouTube — Telugu renders without tofu boxes; repeat with `DisplayLanguage.TAMIL`; then **revert to `DEVANAGARI_SANSKRIT`**, reinstall, verify once more.

- [ ] **Step 14.5: Size check**

Run: `ls -la "app/src/main/assets/content/mantras.json"`
Expected: ≤ 409600 bytes (also test-enforced).

- [ ] **Step 14.6: Commit**

```bash
git add app/src/main/java/com/myniyam/app/data/CurrentSadhana.kt app/src/main/java/com/myniyam/app/overlay/OverlayManager.kt app/src/main/java/com/myniyam/app/data/PlaceholderMantra.kt
git commit -m "feat(overlay): serve mantra from MantraRepository (Gayatri via CurrentSadhana)

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
git push origin main
```

**STOP. Final task — report SP-2 acceptance status against spec §10 and wait for Pranav's sign-off.**

---

## Self-review

**Spec coverage:** schema fields (§4) → Task 2; picker mapping (§4) → Task 3; repository + fallback (§7, §8) → Task 4; generation pipeline + frozen config + AGPL note (§6) → Task 5; 26 entries incl. om + metadata (§5) → Tasks 6-11; validation gate (§8) → Tasks 6 + 12; spot-check log (§8, acceptance 3) → Task 13; overlay integration + Gayatri + font sanity + size + regression (§9, acceptance 4-7) → Task 14. Acceptance criterion 1 → Tasks 6+12; criterion 2 → Tasks 3+4. No gaps found.

**Placeholder scan:** content-authoring steps intentionally specify process + metadata (declared in Execution Conventions #7); the only literal `"pending"` values are tool-overwritten within the same task and gated by validation before commit. `2026-06-DD` placeholders are execution-date slots, instructed to be replaced. No TBDs elsewhere.

**Type consistency:** `MantraRepository.initFromJson/ensureLoaded/all/byId/displayMantra/resetForTest/FALLBACK` consistent across Tasks 4 and 14; `forScript(Script)`/`forLang(MeaningLang)` consistent across Tasks 2, 6 (via parser), and 14; `DisplayLanguage.script/.meaningLang` consistent across Tasks 3 and 14; `CurrentSadhana.MANTRA_ID/LANGUAGE` defined and consumed only in Task 14. Test-count arithmetic: 20 + 4 + 2 + 7 + 8 = 41 ✓.
