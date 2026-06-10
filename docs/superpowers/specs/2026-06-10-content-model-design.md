# Niyam — Content Data Model & Seed Library Design

**Date:** 2026-06-10
**Product:** Niyam (नियम) — www.myniyam.com
**Sub-project:** 2 of 7 — Content data model + seed mantras
**Status:** Design approved by Pranav 2026-06-10 — moving to implementation plan

---

## Table of Contents

1. [Purpose](#purpose)
2. [Scope and non-goals](#scope-and-non-goals)
3. [Decisions locked during design](#decisions-locked-during-design)
4. [Schema](#schema)
5. [Seed library — the 26 entries](#seed-library--the-26-entries)
6. [Content production pipeline](#content-production-pipeline)
7. [Storage and loading architecture](#storage-and-loading-architecture)
8. [Verification strategy and error handling](#verification-strategy-and-error-handling)
9. [Integration scope](#integration-scope)
10. [Acceptance criteria](#acceptance-criteria)
11. [Known risks](#known-risks)
12. [Decisions deferred](#decisions-deferred)
13. [Dependencies on Pranav](#dependencies-on-pranav)

---

## Purpose

Build the content store every future Niyam feature reads from: the schema for a mantra entry, the storage/loading mechanism inside the app, and 26 fully populated seed entries. Prove the pipe end-to-end by making the unlock overlay read its mantra from the store instead of the hardcoded `PlaceholderMantra` constants.

**In plain English:** right now the mantra is a sticker glued onto the engine. This sub-project builds the bookshelf, fills it with 26 properly sourced mantras in all supported scripts and languages, and teaches the engine to pick its mantra off the shelf.

Downstream consumers of this store: onboarding mantra picker (SP-3), home/progress (SP-4), library + filters (SP-5). They all read this schema; none of them are built here.

---

## Scope and non-goals

### In scope

- JSON schema for mantra entries (versioned).
- `MantraRepository` in the app: parse-once, in-memory, immutable, with a hard fallback.
- 26 seed entries fully populated: Devanagari master, 6 generated scripts + readable roman, 8 meaning languages, source attribution, deity tag, intention tags, read-time estimate, completion threshold.
- Offline content-production tooling (`tools/generate_scripts.py` using Aksharamukha) — runs on Pranav's Mac at authoring time, never ships in the APK.
- Build-time validation test that fails the build on malformed/incomplete content.
- Overlay integration: display Gayatri (Devanagari + roman + English meaning) served from the repository.

### Explicit non-goals

- Library/browse UI, filters, mantra selection UI (SP-5).
- Onboarding language picker UI (SP-3) — a `CURRENT_LANGUAGE` constant stands in.
- Progress tracking, Room database, streaks (SP-4).
- Timer changes — stays fixed at 15s even for long entries (tension noted in Deferred).
- Audio recitations.
- Remote content delivery / CMS — content ships in the APK, updates ship as app updates.
- Native-speaker review — queued in `forlater.md`, gates public launch, not this sub-project.
- Any visual/theming changes to the overlay beyond swapping its text source.

---

## Decisions locked during design

| Decision | Choice | Why / context |
|---|---|---|
| Meaning languages | **All 8** (en, hi, te, ta, kn, mr, bn, gu) | Pranav's call 2026-06-10. Picker becomes meaningful for every option. |
| "Devanagari (Sanskrit)" meaning language | English | Purist option pairs original script with the audience's default explanation language; Hindi-meaning users have the dedicated Hindi option. Confirmed by Pranav. |
| Review gate | **None for this sub-project** | Pranav confirmed no reviewers available. Verification is mechanical (see §8); native review queued in forlater.md as a pre-launch item. |
| Content sourcing | Original Devanagari masters cross-checked against ≥2 authoritative sources; scripts machine-generated; meanings written original | Avoids site-to-site convention inconsistency and copy-paste errors; vignanam.org / stotranidhi.com used read-only as verification corpus. |
| Copyright stance on meanings | Pranav assessed copyright as a non-issue; pipeline writes original meanings anyway for single-voice tone consistency | Recorded 2026-06-10. |
| Tamil convention | Tamil script with Grantha consonants (ஜ ஷ ஸ ஹ) | Standard in Tamil devotional publishing; Aksharamukha's supported mode. |
| Roman convention | Simplified readable roman, no diacritics ("Karmanyevadhikaraste") | Matches the product brief's own examples; IAST toggle is a possible future setting. |
| Vedic svara (accent) marks | Omitted in v1 | Standard app practice; svaras don't carry across scripts. |
| Deity tag | Single value per entry | Keeps the SP-5 filter simple. Gita verses tagged `krishna`. |
| Completion thresholds | All 26 entries at 14 days for v1 | Brief allows per-mantra override; tuning deferred until real usage data exists (SP-4+). |
| 26th entry | `om` added to the brief's 25 | Already the skeleton's mantra; doubles as the runtime fallback entry. |

---

## Schema

One JSON file: `app/src/main/assets/content/mantras.json`.

```json
{
  "schemaVersion": 1,
  "contentVersion": "2026-06-10.1",
  "mantras": [
    {
      "id": "gayatri",
      "canonicalName": "Gayatri Mantra",
      "originalLanguage": "sanskrit",
      "text": {
        "devanagari": "ॐ भूर्भुवः स्वः । तत्सवितुर्वरेण्यं …",
        "telugu": "…",
        "tamil": "…",
        "kannada": "…",
        "bengali": "…",
        "gujarati": "…",
        "roman": "Om Bhur Bhuvah Svah | Tat Savitur Varenyam …"
      },
      "meaning": {
        "en": "…", "hi": "…", "te": "…", "ta": "…",
        "kn": "…", "mr": "…", "bn": "…", "gu": "…"
      },
      "source": "Rig Veda 3.62.10",
      "sourceRefs": [
        "https://sanskritdocuments.org/…",
        "https://www.vignanam.org/…"
      ],
      "deity": "universal",
      "intentions": ["sadhana", "focus"],
      "estimatedReadSeconds": 30,
      "completionThresholdDays": 14
    }
  ]
}
```

### Field semantics

| Field | Type | Rules |
|---|---|---|
| `schemaVersion` | int | Bumped only on breaking schema change; parser rejects unknown major versions. |
| `contentVersion` | string | `YYYY-MM-DD.n`; bumped on any content edit. Displayed nowhere; used in bug reports. |
| `id` | string | Stable slug, lowercase-kebab, never changes once shipped (progress tracking joins on it in SP-4). |
| `canonicalName` | string | English display name. |
| `originalLanguage` | enum | `sanskrit` \| `awadhi`. Exists because Hanuman Chalisa is Awadhi — the schema must not claim everything is Sanskrit. |
| `text` | object | Exactly 7 keys: `devanagari` (the master), `telugu`, `tamil`, `kannada`, `bengali`, `gujarati` (generated), `roman` (generated, readable). All non-empty. |
| `meaning` | object | Exactly 8 keys: `en, hi, te, ta, kn, mr, bn, gu`. All non-empty, 2-3 accessible lines each, single authorial voice. |
| `source` | string | User-facing attribution (e.g., "Bhagavad Gita 2.47"). |
| `sourceRefs` | string[] | ≥1 verification URLs. Internal only — never rendered in UI. |
| `deity` | enum | `shiva, vishnu, devi, ganesha, hanuman, krishna, rama, saraswati, lakshmi, universal`. |
| `intentions` | enum[] | ≥1 of `focus, calm, sadhana, dharma, devotion`. Drives onboarding recommendations (SP-3) and library filters (SP-5). |
| `estimatedReadSeconds` | int | Hand-set display estimate; length filter buckets derive from it at runtime (<30 / 30-60 / >60). |
| `completionThresholdDays` | int | Positive; 14 for all v1 entries. |

### Language picker mapping

The 9-option picker resolves to a (script, meaning-language) pair — 7 scripts + 8 meaning languages, not 9×9:

| Picker choice | Script | Meaning |
|---|---|---|
| Devanagari (Sanskrit) | `devanagari` | `en` |
| Hindi | `devanagari` | `hi` |
| Marathi | `devanagari` | `mr` |
| English | `roman` | `en` |
| Telugu | `telugu` | `te` |
| Tamil | `tamil` | `ta` |
| Kannada | `kannada` | `kn` |
| Bengali | `bengali` | `bn` |
| Gujarati | `gujarati` | `gu` |

This mapping lives in one Kotlin function (`DisplayLanguage.resolve()`), unit-tested, used by every future screen.

Content volume: 26 entries × 7 scripts = 182 text blocks; 26 × 8 = **208 meaning blocks**.

---

## Seed library — the 26 entries

All thresholds 14 days. Read-times are hand-set estimates.

| # | id | Name | Source | Orig. lang | Deity | Intentions | ~Read s |
|---|---|---|---|---|---|---|---|
| 1 | `gita-2-47` | Karmanyevadhikaraste | Bhagavad Gita 2.47 | sanskrit | krishna | focus, dharma | 20 |
| 2 | `gita-6-5` | Uddhared Atmanatmanam | Bhagavad Gita 6.5 | sanskrit | krishna | focus | 20 |
| 3 | `gita-2-14` | Matra-sparshas Tu | Bhagavad Gita 2.14 | sanskrit | krishna | focus, calm | 20 |
| 4 | `asato-ma` | Asato Ma Sadgamaya | Brihadaranyaka Upanishad 1.3.28 | sanskrit | universal | focus, sadhana | 15 |
| 5 | `gita-6-6` | Bandhur Atmatmanas | Bhagavad Gita 6.6 | sanskrit | krishna | focus | 20 |
| 6 | `mahamrityunjaya` | Mahamrityunjaya Mantra | Rig Veda 7.59.12 | sanskrit | shiva | calm, devotion | 25 |
| 7 | `om-sahanavavatu` | Om Sahanavavatu (Shanti Mantra) | Taittiriya Upanishad (invocation) | sanskrit | universal | calm, sadhana | 20 |
| 8 | `om-namah-shivaya` | Om Namah Shivaya | Panchakshara — Sri Rudram (Yajur Veda) | sanskrit | shiva | calm, devotion | 10 |
| 9 | `gita-2-70` | Apuryamanam | Bhagavad Gita 2.70 | sanskrit | krishna | calm | 20 |
| 10 | `twameva-mata` | Twameva Mata | Traditional prayer (Pandava Gita) | sanskrit | universal | calm, devotion | 15 |
| 11 | `gayatri` | Gayatri Mantra | Rig Veda 3.62.10 | sanskrit | universal | sadhana, focus | 30 |
| 12 | `vakratunda` | Vakratunda Mahakaya | Traditional Ganesha Vandana | sanskrit | ganesha | sadhana, focus | 15 |
| 13 | `saraswati-vandana` | Ya Kundendu Tushara Hara Dhavala | Saraswati Stotram (traditional) | sanskrit | saraswati | sadhana, focus | 30 |
| 14 | `guru-brahma` | Guru Brahma Guru Vishnu | Guru Stotram (traditional) | sanskrit | universal | sadhana, devotion | 15 |
| 15 | `hare-krishna` | Hare Krishna Mahamantra | Kali-Santarana Upanishad | sanskrit | krishna | sadhana, devotion | 20 |
| 16 | `gita-4-7-8` | Yada Yada Hi Dharmasya | Bhagavad Gita 4.7-8 | sanskrit | krishna | dharma | 35 |
| 17 | `gita-18-66` | Sarva-dharman Parityajya | Bhagavad Gita 18.66 | sanskrit | krishna | dharma, devotion | 20 |
| 18 | `gita-3-35` | Shreyan Svadharmo | Bhagavad Gita 3.35 | sanskrit | krishna | dharma, focus | 20 |
| 19 | `purusha-suktam` | Purusha Suktam (opening verse) | Rig Veda 10.90.1 | sanskrit | universal | dharma, sadhana | 25 |
| 20 | `nasadiya-suktam` | Nasadiya Suktam (opening verse) | Rig Veda 10.129.1 | sanskrit | universal | dharma | 25 |
| 21 | `hanuman-chalisa-opening` | Hanuman Chalisa (Doha + chaupais 1-5) | Hanuman Chalisa — Tulsidas | **awadhi** | hanuman | devotion | 90 |
| 22 | `vishnu-sahasranama-opening` | Vishnu Sahasranama (verses 1-8) | Mahabharata, Anushasana Parva | sanskrit | vishnu | devotion | 120 |
| 23 | `lalita-sahasranama-opening` | Lalita Sahasranama (verses 1-8) | Brahmanda Purana | sanskrit | devi | devotion | 120 |
| 24 | `krishna-ashtakam` | Krishna Ashtakam (verse 1) | Traditional (attr. Adi Shankaracharya) | sanskrit | krishna | devotion | 20 |
| 25 | `ram-raksha-opening` | Ram Raksha Stotra (opening) | Budha Kaushika | sanskrit | rama | devotion, calm | 30 |
| 26 | `om` | Om (Pranava) | Mandukya Upanishad | sanskrit | universal | calm, sadhana | 5 |

Notes:
- Entry 21 is the schema's reason for `originalLanguage` — Chalisa is Awadhi, and claiming otherwise would be the kind of error this product cannot afford.
- Entries 22-23 (120s reads) far exceed the fixed 15s unlock timer — tension recorded in [Decisions deferred](#decisions-deferred).
- `om` (26) doubles as the hardcoded runtime fallback entry (§8).

---

## Content production pipeline

```
[Claude writes Devanagari master]──cross-checked vs ≥2 sources, URLs recorded──► mantras.json (master fields)
        │
        ▼
[tools/generate_scripts.py — Aksharamukha, offline on Pranav's Mac]
        │   one tool, one frozen config → identical conventions across all 26
        ▼
telugu · tamil(+Grantha) · kannada · bengali · gujarati · roman   (written back into mantras.json)
        │
        ▼
[Claude writes meaning.en]──► [Claude translates into hi, te, ta, kn, mr, bn, gu — tone-matched]
        │
        ▼
[build-time validation test] ──► [spot-check: 3 entries/script vs vignanam.org / stotranidhi.com, log committed]
```

Properties:
- **No copy-paste anywhere.** Master → scripts is mechanical; meanings are authored once in one voice.
- **Convention consistency by construction:** one tool, one config, frozen in the repo. Tamil mode: Grantha consonants. Roman mode: readable, no diacritics.
- **Deterministic regeneration:** edit a master, re-run the tool, derived fields update identically. The tool is committed at `tools/generate_scripts.py` with a pinned `aksharamukha` version in `tools/requirements.txt`.
- **Licensing:** Aksharamukha (AGPL) runs offline at authoring time only; nothing of it ships in the APK. Its output — script renderings of public-domain texts — carries no license burden into the app.

---

## Storage and loading architecture

- **`assets/content/mantras.json`** — single file, target ≤400KB (estimate ~250KB).
- **`MantraRepository`** (new, `data/` package): lazy parse-once off the main thread via kotlinx-serialization (`ignoreUnknownKeys = true` for forward compatibility). Exposes `all(): List<Mantra>`, `byId(id: String): Mantra?`. Immutable after load.
- **`Mantra`** Kotlin data class mirrors the schema; enums for `deity`/`intentions` with `@SerialName` mappings.
- **`DisplayLanguage`** enum (9 picker values) with `resolve(): Pair<Script, MeaningLang>` implementing the mapping table.
- **New dependency:** `org.jetbrains.kotlinx:kotlinx-serialization-json` + the Kotlin serialization Gradle plugin (matches Kotlin 2.0.21).
- **No database.** Room arrives in SP-4 for user progress and joins content by `id`. Content updates ship as app updates with a `contentVersion` bump; git history is the audit trail.

**In plain English:** the mantras live in one bundled file the app reads at startup into memory. No internet needed, ever. Updating content = shipping an app update, and every change is a reviewable line in git.

---

## Verification strategy and error handling

Adapted for the no-reviewer constraint (Pranav, 2026-06-10):

| Layer | Risk | Mitigation |
|---|---|---|
| Devanagari masters | Typo in source text | Cross-checked against ≥2 independent authoritative sources (sanskritdocuments.org, Gita Press editions, GRETIL where applicable); `sourceRefs` recorded per entry |
| Generated scripts | Wrong tool configuration | Deterministic generation makes errors systematic, not random — spot-checking 3 entries per script against native devotional sites catches config-level mistakes; spot-check log committed under `docs/superpowers/test-reports/` (dated at execution time) |
| Meanings (8 languages) | Stiff or non-native phrasing | Short simple sentences; single authorial voice; **residual risk accepted** — the easiest layer to hot-fix via a content update; native review queued in forlater.md as a launch gate |
| File integrity | Missing/malformed fields | Build-time validation test — the APK cannot build with broken content (counts, non-empty checks, enum legality, unique ids, positive numbers) |
| Runtime parse failure | Corrupt asset on-device | `MantraRepository` falls back to a built-in `om` entry (today's `PlaceholderMantra`, demoted from "the content" to "the airbag"); the overlay can never crash from bad content; failure logged |

---

## Integration scope

Changes to the existing engine — deliberately minimal:

1. `OverlayManager.show()` reads `MantraRepository.byId(CurrentSadhana.MANTRA_ID)` instead of `PlaceholderMantra` constants. `CurrentSadhana.MANTRA_ID = "gayatri"` — a hardcoded constant standing in for SP-3's onboarding choice. Gayatri chosen because a real three-line entry exercises Devanagari rendering harder than single-glyph ॐ.
2. Overlay fills the same three slots it has today: script text (devanagari), transliteration (roman), meaning (en) — per `CurrentSadhana.LANGUAGE = DisplayLanguage.DEVANAGARI_SANSKRIT`, also a stand-in constant.
3. Fallback path wired: repository failure → `om` fallback entry → overlay renders normally.
4. Nothing else in the engine changes. No timer change, no layout change, no service changes.

---

## Acceptance criteria

All verifiable on the emulator / JVM — no human reviewer in the loop:

1. **Validation test passes:** 26 entries; unique ids; every entry has all 7 scripts and all 8 meanings non-empty; legal enum values; positive read-times and thresholds; ≥1 sourceRef each.
2. **Repository unit tests pass:** load-and-parse, `byId` hit and miss, `DisplayLanguage.resolve()` for all 9 picker values, fallback path on a deliberately corrupt test asset.
3. **Spot-check log committed:** 3 entries per generated script compared against external native-script sources; every discrepancy explained (convention difference vs error) in the log.
4. **Emulator:** blocked-app launch shows Gayatri — Devanagari text, roman transliteration, English meaning — served from the repository.
5. **Font sanity:** Devanagari, Telugu, and Tamil render on the emulator without tofu boxes (validates Android's bundled Noto fonts cover our scripts).
6. **Size:** `mantras.json` ≤ 400KB; APK gains no new permissions.
7. **Regression:** all 20 existing unit tests (BlockList 8, OemAutostartHelper 12) still pass; Phase-1 overlay behavior unchanged apart from the displayed text.

---

## Known risks

1. **Vernacular meaning quality (highest residual risk).** Claude-authored translations into 7 Indian languages without native review may read slightly stiff. Accepted by Pranav for this phase; queued for native review pre-launch; hot-fixable by content update.
2. **Long-text rendering.** Vishnu/Lalita Sahasranama 8-verse blocks are large for the overlay's current fixed layout — text may overflow on small screens. The skeleton displays only Gayatri, so this bites in SP-5 when users can select long entries; flagged for the UI pass (scrollable overlay text region is the likely fix).
3. **Aksharamukha edge cases.** Rare conjuncts or Vedic characters may transliterate imperfectly into Tamil/Bengali. Spot-checks target the entries most likely to expose this (Sahasranamas, Vedic suktas).
4. **Attribution disputes.** A few entries use traditional attributions ("attr. Adi Shankaracharya", "Pandava Gita") where scholarship is genuinely uncertain — wording stays at "traditional" to remain honest.

---

## Decisions deferred

- **Timer vs read-time:** 120s entries behind a 15s unlock timer — should the timer scale with `estimatedReadSeconds`? SP-4's call (it owns the "what counts as a completed read" rule).
- **Native-speaker review** — forlater.md, pre-launch gate.
- **Per-mantra threshold tuning** — needs real usage data (SP-4+).
- **IAST diacritics toggle** for roman script — future settings candidate.
- **Malayalam (or any 10th language)** — schema makes it additive; not in v1.
- **Audio recitations** — not designed.
- **Library UI, selection, recommendation graph** — SP-3/SP-5 consume `intentions` + `deity`; nothing here blocks them.

---

## Dependencies on Pranav

1. **Review this spec** — confirm before the implementation plan is written.
2. **Python on the Mac** — the generation tool needs Python 3.9+ (`pip install aksharamukha`); I'll check availability during implementation and surface anything missing.
3. Nothing else. No reviewers required for this sub-project's definition of done.
