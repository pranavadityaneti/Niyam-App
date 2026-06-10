# forlater.md — Deferred Work Queue (Niyam)

## Active queue

### 1. Native-speaker review of vernacular meanings
- **What:** Have native readers of Hindi, Telugu, Tamil, Kannada, Marathi, Bengali, Gujarati review the ~208 translated meaning blocks (and spot-check transliterations) shipped in `mantras.json`.
- **Why:** Sub-project 2 ships with machine-verified transliterations and Claude-authored meanings. Transliteration risk is low (deterministic generation, spot-checked); the residual risk is meanings reading stiff/non-native. A devotional app's credibility depends on this.
- **Scope:** Read-through + corrections per language; content-only update (JSON edit + app update), no code changes.
- **Status:** Deferred — Pranav confirmed no reviewers available right now (2026-06-10). **Content shipped 2026-06-10 (26 entries).** Reviewer starting points: the per-batch editorial flags in the SP-2 review transcripts, and `docs/superpowers/test-reports/2026-06-10-content-spotcheck.md` (top flags: Gujarati Chalisa candrabindu-vs-anusvara convention; Tamil visarga `꞉` on long texts; hi/te/kn "good sense" register in the Chalisa meaning; bead/japa imagery in the Lalita meaning).
- **Date added:** 2026-06-10
- **Originated from:** Sub-project 2 design discussion (QA-gate question).
- **Trigger to revisit:** Before public Play Store launch, or when a native reader becomes available — whichever first.

### 4. Align MantraRepository.FALLBACK om fields with the catalog om entry
- **What:** The hardcoded airbag entry diverges from the catalog's om in two fields (tamil `ஓம்` vs tool-normalized `ௐ`; meaning.en 1 sentence vs 2). Both valid; unreachable unless the asset is corrupt.
- **Why:** Keep the airbag in sync if the om convention is ever revisited. SP-2 final review: "record, don't fix."
- **Scope:** One constant in `MantraRepository.kt`.
- **Status:** Deferred (cosmetic, unreachable on happy path).
- **Date added:** 2026-06-10
- **Originated from:** SP-2 final whole-implementation review.

### 2. OEM permission-screen copy polish
- **What:** The OEM autostart screen copy reads awkwardly on stock devices ("Your phone is Google (GENERIC). So your phone allows us to start when needed."). Rewrite copy per OEM flow.
- **Why:** Observed during Phase 1 emulator acceptance. Cosmetic, lands naturally with the UI-references pass.
- **Scope:** strings.xml + OemAutostartScreen copy only.
- **Status:** Deferred to UI pass.
- **Date added:** 2026-06-10
- **Originated from:** Phase 1 acceptance test observations.

### 3. Phase 2 real-OEM acceptance test
- **What:** Run the 14 acceptance criteria + 30-min service-survival test on a real OEM phone (ideally MIUI/Redmi; Samsung second-best).
- **Why:** Emulator cannot reproduce OEM battery-killer behavior; this gates any paid-launch decision per the sub-project 1 spec.
- **Scope:** Manual test pass + report in docs/superpowers/test-reports/.
- **Status:** Waiting on Pranav's Android phone (expected 2026-06-10).
- **Date added:** 2026-06-10
- **Originated from:** Sub-project 1 spec, Phase 2 acceptance section.

## In progress

_(empty)_

## Done — archived

_(empty)_
