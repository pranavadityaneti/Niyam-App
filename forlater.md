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

### 3. Phase 2 real-OEM acceptance test
- **What:** Run the 14 acceptance criteria + 30-min service-survival test on a real OEM phone (ideally MIUI/Redmi; Samsung second-best).
- **Why:** Emulator cannot reproduce OEM battery-killer behavior; this gates any paid-launch decision per the sub-project 1 spec.
- **Scope:** Manual test pass + report in docs/superpowers/test-reports/.
- **Status:** Waiting on Pranav's Android phone (expected 2026-06-10).
- **Date added:** 2026-06-10
- **Originated from:** Sub-project 1 spec, Phase 2 acceptance section.

### 7. Niyam launcher icon
- **What:** The app still ships the default Android robot launcher icon (visible on splash + home screen). Design a Niyam icon — orange/bottle-green, no Om (founder dropped Om from the brand for now).
- **Why:** First thing a user sees on their home screen; default icon reads unfinished. Spotted in SP-8 emulator verification.
- **Scope:** Adaptive icon (foreground/background layers) + splash theming; needs founder approval of the mark.
- **Status:** Deferred — needs a design pass with founder veto.
- **Date added:** 2026-06-11
- **Originated from:** SP-8 v2 screenshot session.

### 8. Full UI chrome translation (Hindi/Telugu/Tamil/+5)
- **What:** Today the display-language setting localises the mantra script + meaning only; all chrome (buttons, labels, settings, dialogs — ~80 strings in `strings.xml`) stays English. Translate the chrome into the 8 supported meaning languages via Android `values-<lang>/strings.xml` resources, driven by the same in-app language choice (per-app locale via `AppCompatDelegate.setApplicationLocales` or manual resource resolution).
- **Why:** India-first product; a Telugu-speaking user who picks తెలుగు reasonably expects "Browse library" to localise too. Pranav saw the gap in the 2026-06-11 language gallery and was told it's a separate, queueable piece.
- **Scope:** 8 × strings.xml translations + locale plumbing + plurals per language (Indic plural rules) + native-speaker review (ties into item 1).
- **Status:** Queued — awaiting founder prioritisation (post-SP-6 candidate).
- **Date added:** 2026-06-11
- **Originated from:** Hindi/Telugu/Tamil screenshot gallery session ("Good to go" turn).

## In progress

_(empty)_

## Done — archived

### 4. Align MantraRepository.FALLBACK om fields with the catalog om entry
- **What:** The hardcoded airbag entry diverges from the catalog's om in two fields (tamil `ஓம்` vs tool-normalized `ௐ`; meaning.en 1 sentence vs 2). Both valid; unreachable unless the asset is corrupt.
- **Why:** Keep the airbag in sync if the om convention is ever revisited. SP-2 final review: "record, don't fix."
- **Scope:** One constant in `MantraRepository.kt`.
- **Status:** Deferred (cosmetic, unreachable on happy path).
- **Date added:** 2026-06-10
- **Originated from:** SP-2 final whole-implementation review.
- **Completed:** 2026-06-11 (SP-10 polish batch, commit 6c814f8)

### 2. OEM permission-screen copy polish
- **What:** The OEM autostart screen copy reads awkwardly on stock devices ("Your phone is Google (GENERIC). So your phone allows us to start when needed."). Rewrite copy per OEM flow.
- **Why:** Observed during Phase 1 emulator acceptance. Cosmetic, lands naturally with the UI-references pass.
- **Scope:** strings.xml + OemAutostartScreen copy only.
- **Status:** Deferred to UI pass.
- **Date added:** 2026-06-10
- **Originated from:** Phase 1 acceptance test observations.
- **Completed:** 2026-06-11 (SP-10 polish batch, commit 6c814f8)

### 5. Dark-mode variant of the unlock overlay
- **What:** The mantra overlay (View XML, `overlay_mantra.xml`) stays eggshell-light in all themes. Add a bottle-green dark variant that follows the user's themePref.
- **Why:** SP-7 shipped in-app dark mode but deliberately excluded the overlay to preserve the zero-engine-touch rule. A dark-mode user who unlocks Instagram at night gets a bright eggshell flash — acceptable for an interruption surface, but worth revisiting.
- **Scope:** `overlay_mantra.xml` + `OverlayManager` color binding — **engine-adjacent**, needs its own careful pass + emulator verification.
- **Status:** Deferred by design (SP-7 spec §2 "Overlay stays light").
- **Date added:** 2026-06-11
- **Originated from:** SP-7 design spec, overlay rule.
- **Completed:** 2026-06-11 (SP-10 polish batch, commit 6c814f8)

### 6. Hide duplicate roman line when display language is English
- **What:** With language = English (Roman script), the overlay and the mantra detail screen render the same roman text twice (main text + italic transliteration line). Suppress the transliteration line when the chosen script IS roman.
- **Why:** Cosmetic duplication, visible in the 2026-06-11 screenshot gallery (11-detail, 16-overlay).
- **Scope:** OverlayManager binding + MantraDetailScreen — one conditional each. Overlay touch is engine-adjacent (view binding only, no detection logic).
- **Status:** Deferred — polish batch.
- **Date added:** 2026-06-11
- **Originated from:** Screenshot walkthrough.
- **Completed:** 2026-06-11 (SP-10 polish batch, commit 6c814f8)

