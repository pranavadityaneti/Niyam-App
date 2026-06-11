# Niyam — Session Log

## 2026-06-10 — SP-4 EXECUTED — home + sadhana progress + streak code-complete

**Sub-project 4 of 7 shipped as code** under the full-control grant. Range `0f4da3f..b734bf6`.

- **Test suite 55 → 69** (all green): ProgressMath 7 (streak/dayN/isComplete boundary), UserPrefs 7 (4 new progress fields), OnboardingViewModel 5, NextSadhana 3.
- **What shipped:** Room `read_events` + `ProgressRepository` (fire-and-forget recordRead from the overlay Continue tap + completion detection); `ProgressMath` pure date math; UserPrefs += selectedIntention / sadhanaStartEpochDay / completedMantraIds / pendingCelebration (`setCurrentMantra` re-stamps journeys atomically); onboarding persists intention; **Home rebuilt to brand** (mantra card, orange Day-N-of-14 bar, streak/today chips, compact protection row — old green/red hex banner gone); Celebration → NextSadhana picker (curated, excludes completed/current, backfills).
- **Engine touch verified to bytecode-level standards:** OverlayManager diff = 1 import + 2-line listener; unlock path provably non-blocking.
- **Session-interruption recovery:** per-task review records for T4-T9 were lost mid-execution (model switches); recovered via one integrated final review (verdict: code-complete, no Critical/Important). T10 recovered from working tree, verified, committed. Two optional test gaps closed (`b734bf6`).
- **OPEN (manual, one emulator session closes all):** SP-4 criteria 3/4-spot/6 + SP-3 criteria 1-4,6,7 + SP-2 criteria 4-5. Acceptance report: `docs/superpowers/test-reports/2026-06-10-sp4-acceptance.md`.
- **Next:** SP-5 (library + filters + sadhana switching) → SP-7 (settings/notifications/dark) → SP-6 (paywall code) → landing page. Monetization questionnaire still awaiting Pranav's answers (gates SP-6 live billing only).

---

## 2026-06-10 — SP-3 EXECUTED — onboarding + brand theme code-complete

**Sub-project 3 of 7 shipped as code.** Plan tasks 1–14 all executed; Task 14 = integrated verification + acceptance report.

### Headline facts
- **Test suite 43 → 55** (all green, 0 fail/0 err, `--rerun-tasks`): new `StarterMantrasTest` (2, curation), `UserPrefsTest` (4, snapshot/defaults), `OnboardingViewModelTest` (4, gating); `BlockListTest` expanded to 10 (default/overridden snapshot).
- **APK:** 11,393,847 bytes — total growth **+1.27 MB** vs pre-SP3 baseline (10,063,583 B). Honest split: fonts ≈ +0.57 MB (within ≈600KB budget); non-font ≈ +0.70 MB = DataStore + lifecycle-viewmodel-compose deps (baseline predates them). Criterion 8 flagged: PASS on font budget, total over the 1 MB line — Pranav to accept.
- **What shipped:** `NiyamTheme` (eggshell/bottle-green/orange in `Color.kt`), bundled Playfair Display + Inter fonts, overlay restyle (same ids/timer/unlock), `UserPrefs` DataStore + sync snapshot, `CurrentSadhana`/`BlockList` snapshot-backed (services untouched), `StarterMantras` curation, first ViewModel (`OnboardingViewModel`), 5 onboarding screens, NavHost flow (returning users skip to Home).
- **Two review-driven fixes:** gist word-boundary fallback (`1bcf5bd`); overlay ScrollView + sadhana label for long mantras (`31edba0`).
- **Acceptance:** criteria 5 (tests) + 8 (size, with caveat) automatable → PASS; criteria 1–4, 6 (emulator) + 7 (Pranav visual sign-off) **OPEN** — `adb devices` empty, no emulator at verification time, deferred to Pranav (rides with the pending SP-2 emulator check).
- **Commit range:** `1d887c9..HEAD` (`486e447`), 12 commits. Acceptance report: `docs/superpowers/test-reports/2026-06-10-sp3-acceptance.md`.

---

## 2026-06-10 — Session 2 (cont.): SP-2 EXECUTED END-TO-END — sub-project 2 code-complete

### What happened

Pranav approved the SP-2 plan and chose **subagent-driven execution** (implementer + spec reviewer + quality reviewer per task, all on `model: opus`; main loop Fable 5 — see memory `niyam-model-policy`). All 14 plan tasks executed and reviewed. **42 subagent dispatches total** across implementation, two-stage reviews, surgical fixes, and the final whole-implementation review.

### Headline results

- **Catalog complete: 26 entries** in `app/src/main/assets/content/mantras.json` (158.9KB) — 7 scripts + 8 meaning languages each, every Devanagari master verified char-by-char against ≥2 independent sources by implementers AND re-verified against different sources by scripture reviewers.
- **Test suite 20 → 43** (model 4, display-language 2, repository 7, content-validation 10 incl. per-script Unicode-block + meaning-length gates).
- **Overlay now serves Gayatri from the repository** (`CurrentSadhana.MANTRA_ID = "gayatri"`); repository pre-warmed off-main-thread in NiyamApplication; PlaceholderMantra demoted to airbag-source, marked for deletion pending Pranav.
- **Tooling:** `tools/generate_scripts.py` (Aksharamukha 2.2.1, **Python 3.11 venv** — 3.12+ breaks aksharamukha; RomanColloquial scheme; Tamil+Grantha) + `--check` drift gate. Spot-check log committed: 0 errors, 7 named convention differences, Bengali/Gujarati calibration gap closed.
- **Review-driven fixes along the way:** gayatri meaning strengthened (default overlay line, `0824c71`); gita-3-35 death-clause restored (`834f847` + hi `मर मिटना` `7027cdb`); om mr danda fix; tool error-UX (`2547cce`); Lalita vocalic-ḷ Unicode defect caught & fixed at source.
- **Final whole-implementation review: SP-2 code-complete = YES.** Acceptance criteria 1-3, 6-7 PASS (independently re-run); **4-5 OPEN** (emulator Gayatri render + font sanity — no emulator was connected; needs a booted emulator or Pranav's phone).

### Open threads

1. **Acceptance 4+5:** install on emulator/phone, open a blocked app, confirm 3-line Gayatri + roman + en meaning, no tofu in Devanagari/Telugu/Tamil (flip CurrentSadhana.LANGUAGE temporarily for the latter two, or wait for SP-3's picker).
2. **PlaceholderMantra.kt deletion** — awaiting Pranav's explicit confirmation.
3. forlater.md now has 4 items (native review enriched with reviewer flags; OEM copy; Phase 2 device test; FALLBACK om alignment).
4. UI references still pending from Pranav.

---

## 2026-06-10 — Session 3 (subagent log): SP-2 Task 10 — Content Batch D (Dharma, 5 entries)

### Timeline

1. **Authored Dharma batch (5 entries)** → catalog 16 → 21. Entries: `gita-4-7-8` (Yada Yada Hi Dharmasya, both 4.7+4.8 shlokas as one unit), `gita-18-66` (Sarva-dharman Parityajya), `gita-3-35` (Shreyan Svadharmo), `purusha-suktam` (RV 10.90.1), `nasadiya-suktam` (RV 10.129.1). Two archaic-Vedic hymns = highest textual difficulty in project.
2. **Devanagari masters cross-verified ≥2 independent sources each** before generation. Three Gita verses: holy-bhagavad-gita.org + sanskritdocuments.org. Purusha: wisdomlib (RV 10.90.1, svara-marked) + indiaphilosophy + search corroboration of दशाङ्गुलम्. Nasadiya: Wikipedia (svara-marked) + sanskritdocuments.org.
3. **Two textual variants found & resolved (scholarly form chosen, noted):**
   - `gita-18-66`: `अहं त्वा` (sanskritdocuments / Shankara tradition — used) vs popular `अहं त्वां` (holy-bhagavad-gita, vivekavani, wisdomlib). Followed FINAL metadata + tie-breaker → `त्वा`.
   - `purusha-suktam`: joined `वृत्वात्यतिष्ठद्दशाङ्गुलम्` (continuous scholarly form, wisdomlib — used) vs spaced `वृत्वा अत्यतिष्ठत्` in some popular editions. Followed plan → joined. Caught an indiaphilosophy typo (`दशाङुलम्` missing ग); correct conjunct is ङ्गु → `दशाङ्गुलम्`.
4. **Generated 30 derived fields** via frozen `tools/generate_scripts.py` (aksharamukha). Eyeballed long Vedic compounds in all scripts — no mojibake/truncation; te/kn use anusvara-for-final-म्/ṅ, consistent with existing `mahamrityunjaya` convention.
5. **Gates:** ContentValidationTest 10/10 · full suite 43/43 (0 fail, 0 err) · `--check` exit 0.
6. **Committed + pushed to origin/main** (direct push authorized for this task).

### Notes
- Meanings: original writing, en→7 translations. Deity-gloss used for both Krishna promises (4.7-8 "Krishna promises to return"; 18.66 "Krishna's final word"). 18.66 handled as "let go of every other refuge," NOT abandoning duty. Vedic hymns kept third-person plain awe; nasadiya honors the open question ("The hymn only asks"). en lengths 150–163 chars, em-dash used as main pivot in only 2/5.
- Entry-count test still `assertTrue(isNotEmpty)` — flips to `assertEquals(26)` in Task 12 once all batches land. No change needed here.

---

## 2026-06-10 — Session 2: Phase 1 sign-off + Sub-project 2 design

### Timeline

1. **Phase 1 acceptance completed by Pranav on Pixel 9 emulator** (API 35, Google Play image — Pixel 6 lacked a Play Store image). 10 PASS, 1 N/A (Facebook not installed), 2 untested-presumed-pass (#11 debounce, #12 Recents survival — deferred to Phase 2 device testing). Report committed at `62a174e`. **Sub-project 1 signed off.**
2. **Sub-project 2 (content model + seed mantras) brainstormed and design approved.** Key decisions, all Pranav-confirmed 2026-06-10:
   - **Meanings in all 8 languages** (en, hi, te, ta, kn, mr, bn, gu) — not English-only. 26 entries × 8 = 208 meaning blocks.
   - **"Devanagari (Sanskrit)" picker option pairs with English meaning** (purist script + default explanation language).
   - **No human review gate for SP-2** — Pranav has no reviewers available; verification is mechanical (cross-sourced masters, deterministic generation, build-time validation). Native review queued in `forlater.md` as a pre-launch gate.
   - **Content pipeline:** one Devanagari master per entry (cross-checked vs ≥2 authoritative sources) → Aksharamukha generates 6 scripts + roman offline → Claude authors original meanings (single voice) → build-time validation + spot-checks vs vignanam.org/stotranidhi.com. No scraping, no copy-paste.
   - **Copyright:** Pranav assessed as non-issue; pipeline writes original meanings anyway.
   - 26th entry `om` added (skeleton's mantra, doubles as runtime fallback).
3. **`forlater.md` created** with 3 queued items: native-speaker review (pre-launch gate), OEM copy polish (UI pass), Phase 2 real-OEM test (phone arriving today).
4. **Spec written + committed:** `docs/superpowers/specs/2026-06-10-content-model-design.{md,html}`. Awaiting Pranav's spec review → then writing-plans.

### Open threads

- Pranav reviews SP-2 spec (gate to implementation plan).
- Phase 2 real-device test when phone arrives (forlater #3).
- UI references still pending from Pranav (blocks all visual work).
- Model note: session switched to Fable 5 via /model; Pranav's CLAUDE.md "never Haiku" rule still honored.

---

## 2026-06-09 — Session 1: design → plan → walking-skeleton code complete

### Timeline

1. **Spec dropped.** Pranav handed over the full product brief for a Hindu-themed Android distraction-blocker. Big multi-subsystem spec — flagged scope, decomposed into 7 sub-projects.
2. **Platform locked.** Android-first, native Kotlin. India focus → ~95% Android share + Apple Screen Time shield can't host the designed 15s-timer-with-ads unlock UX.
3. **Sub-project 1 chosen:** the blocking-engine walking skeleton — the de-risk piece. Until this works, the whole product is theoretical.
4. **Design spec written:** [docs/superpowers/specs/2026-06-09-blocking-engine-walking-skeleton-design.md](docs/superpowers/specs/2026-06-09-blocking-engine-walking-skeleton-design.md) (+ HTML companion per Pranav's HTML-first rule).
5. **Spec revisions after Pranav review:**
   - Block list expanded from Instagram-only → Instagram + Facebook + YouTube
   - Hardcoded `ॐ` confirmed for skeleton (will revisit when running on device)
   - No physical Android device available → acceptance split into Phase 1 (emulator-only) and Phase 2 (real OEM, deferred)
   - Test phone arriving 2026-06-10
6. **App named: Niyam** (नियम — Sanskrit "rule, discipline, observance"; literally one of Patanjali's Niyamas). Website: www.myniyam.com.
7. **Implementation plan written:** [docs/superpowers/plans/2026-06-09-niyam-blocking-engine-walking-skeleton.md](docs/superpowers/plans/2026-06-09-niyam-blocking-engine-walking-skeleton.md). 21 tasks. TDD where the Android testing model allows it.
8. **GitHub repo created** by Pranav at https://github.com/pranavadityaneti/Niyam-App, pushed initial commits with design + plan.
9. **Executed Plan Tasks 1-20 inline** under Pranav's "no permission needed for 30 min" authorization. Tasks 1-20 cover everything except the manual emulator acceptance test (Task 21).

### Key decisions made today

| Decision | Choice | Why |
|---|---|---|
| Platform | Android-first, native Kotlin | India = Android. iOS shield API likely can't host the designed UX. |
| Detection mechanism | AccessibilityService | Event-driven, intercepts before user sees Instagram. UsageStats polling has a ~200ms gap that defeats the product. |
| Overlay | `TYPE_APPLICATION_OVERLAY` via WindowManager + traditional View XML | Compose overlay outside an Activity is finicky. |
| Service | Foreground service with persistent notification | Survives background-kill longer than plain services. |
| OEM autostart | Opportunistic (not blocking), `AutoStarter` library, per-OEM walkthrough | Cannot programmatically verify grant. Hard-blocking = drop-off. |
| Min/target SDK | 26 / 34 | TYPE_APPLICATION_OVERLAY requires API 26. ~99% of Indian Android devices are 26+. |
| Compose | Yes for in-app UI; Views for overlay | Modern UI for in-app, fewer compose-overlay rough edges. |
| Acceptance | Two phases — Phase 1 emulator (current), Phase 2 real OEM (deferred to 2026-06-10+) | Honest about what emulator can/cannot prove. |

### Files modified today

All commits pushed to https://github.com/pranavadityaneti/Niyam-App (main).

**Design + plan (3,370 lines):**
- `docs/superpowers/specs/2026-06-09-blocking-engine-walking-skeleton-design.md`
- `docs/superpowers/specs/2026-06-09-blocking-engine-walking-skeleton-design.html`
- `docs/superpowers/plans/2026-06-09-niyam-blocking-engine-walking-skeleton.md`

**App scaffolding:**
- `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml`, `.gitignore`, `gradlew`, `gradlew.bat`, `gradle/wrapper/*`
- `app/build.gradle.kts`, `app/proguard-rules.pro`, `app/src/main/AndroidManifest.xml`
- `local.properties` (NOT committed — points at `~/Library/Android/sdk`)

**Source:**
- `app/src/main/java/com/myniyam/app/NiyamApplication.kt`
- `app/src/main/java/com/myniyam/app/MainActivity.kt`
- `app/src/main/java/com/myniyam/app/data/BlockList.kt` + test
- `app/src/main/java/com/myniyam/app/data/PlaceholderMantra.kt`
- `app/src/main/java/com/myniyam/app/permissions/PermissionChecker.kt`
- `app/src/main/java/com/myniyam/app/permissions/OemAutostartHelper.kt` + test
- `app/src/main/java/com/myniyam/app/service/AppLockForegroundService.kt`
- `app/src/main/java/com/myniyam/app/service/AppLockAccessibilityService.kt`
- `app/src/main/java/com/myniyam/app/overlay/OverlayManager.kt`
- `app/src/main/java/com/myniyam/app/ui/AppNavHost.kt`
- `app/src/main/java/com/myniyam/app/ui/theme/{Color,Type,Theme}.kt`
- `app/src/main/java/com/myniyam/app/ui/screens/{Welcome,Permission,OemAutostart,Home}Screen.kt`

**Resources:**
- `app/src/main/res/values/{strings,colors,themes}.xml`
- `app/src/main/res/xml/accessibility_service_config.xml`
- `app/src/main/res/layout/overlay_mantra.xml`

### System-level changes today

- Installed Homebrew packages `gradle` (9.5.1) and `openjdk@26` (transitive). Reversible via `brew uninstall`. Wrapper itself is pinned to **Gradle 8.10** for AGP 8.7 compatibility.
- Wrote `local.properties` pointing Gradle at `~/Library/Android/sdk`. Not committed (gitignored).

### Errors encountered + fix-once-and-for-all

- **AutoStarter dependency resolution failed** on first compile attempt. Library lives on JitPack (`com.github.judemanutd`), not Maven Central (`com.judemanutd`). Fixed by adding `https://jitpack.io` to `settings.gradle.kts` repositories and correcting the group id in `libs.versions.toml`. Both fixes pushed in commit `1621de0`.
- **AGP deprecation warning** on `kotlinOptions {}` (deprecated in favor of `compilerOptions {}` for Gradle 10 / AGP 9). NOT blocking now. Defer to a future version-bump task.

### Open threads — what's left

1. **Task 21 of the plan: Phase 1 acceptance test on the emulator.** Manual. Requires:
   - Boot a Pixel 6 / API 34 emulator
   - `./gradlew :app:installDebug`
   - Sign into Play Store on the emulator, install Instagram + Facebook + (YouTube comes preinstalled)
   - Walk through 5 permission screens, confirm green Home banner
   - Open each blocked app, confirm overlay covers within ~200ms, timer counts down, Continue dismisses
   - Open Chrome — confirm overlay does NOT trigger
   - Revoke AccessibilityService → confirm Home flips to "At Risk"
   - Write results to `docs/superpowers/test-reports/2026-06-09-niyam-skeleton-phase1-emulator.md`
2. **UI references** — Pranav will share visual mockups. Until then, all UI is bare system defaults.
3. **Phase 2 on real Android device** — phone arriving 2026-06-10. Sideload `app/build/outputs/apk/debug/app-debug.apk` and run Phase 2 acceptance.
4. **Sub-project 2** — content data model + 25 seed mantras. Starts only after sub-project 1 is signed off.

### Where to pick up

When Pranav returns: the walking-skeleton **code** is done and on GitHub. The remaining sub-project-1 work is the manual acceptance test (Task 21 in the plan). Pranav can either:
- Launch Android Studio, open this project, boot an emulator, and walk through the 14 acceptance criteria himself
- Or hand it back to me when he's ready and I'll drive the emulator via `mcp__computer-use__*` tools (token-heavy but possible)

No code change is blocked. The next code task (sub-project 2 content model) doesn't start until Phase 1 acceptance passes.

---

## 2026-06-11 — Catch-up + SP-5 EXECUTED

*(The 2026-06-10 sessions ran under interruptions and their log entries were never appended — recorded here from git ground truth.)*

### Catch-up: SP-2 → SP-4 (2026-06-09 → 06-10, all pushed to main)
- **SP-2 content model:** Mantra data model + DisplayLanguage + MantraRepository + Python generation tool (Aksharamukha, **Python 3.11 venv only**) + 26 mantras in 7 scripts / 8 meaning languages. Overlay renders live content. Spot-check report in `docs/superpowers/test-reports/`.
- **SP-3 onboarding + brand:** brand theme (Eggshell/BottleGreen/PumpkinOrange, Playfair+Inter), Welcome + 4 onboarding screens + permission flow restyle, selections persisted via UserPrefs DataStore.
- **SP-4 home + progress:** Room read-events DB, ProgressRepository, ProgressMath (day N of 14, streak), branded Home (mantra card/progress/streak/protection), Celebration + NextSadhana screens, overlay Continue records reads. Suite reached 69 tests. Report: `2026-06-10-sp4-acceptance.md`.

### SP-5 EXECUTED — library + filters + sadhana switching
- Commits `7dc6542 → efbaefc` (6 commits, all on origin/main). Suite **76/76**; `--check` exit 0; asset 163,590 B; **zero engine-file touches** (verified `git diff 4595149..HEAD`).
- T1 sourceCategory (model + 26 tags + validation) · T2 LibraryFilters pure function (TDD) · T3 LibraryScreen (4 chip rows, branded FilterChips) · T4 MantraDetailScreen + honest "start over" switch dialog (dayN-gated) · T5 routes + Home "Browse library" button · review fix `efbaefc` (detail chips: category/read-time/deity + green Completed).
- Two reviews (Opus): T1+T2 pass; T3-T5 pass with 2 flags — chips gap **fixed**, §6 stats-failure path **kept as direct switch** (more honest than a dialog missing its day count; flagged for Pranav).
- Report: `docs/superpowers/test-reports/2026-06-11-sp5-acceptance.md`. Manual emulator walkthrough + visual eyeball deferred to the standing session.

### Open threads
1. Standing manual emulator session: SP-2/3/4/5 walkthrough criteria + visual brand eyeball.
2. Pranav's unanswered 8-question monetization batch (Play Console, RevenueCat, ads, trial, privacy policy, Vercel/DNS, phone, definition of done).
3. PlaceholderMantra.kt deletion — awaiting explicit confirmation.
4. `forlater.md`: 4 queued items (native-speaker review, OEM copy, Phase 2 real-device test, FALLBACK-om alignment).

### Where to pick up
SP-5 is code-complete. Next: **SP-7** (settings / notifications / dark mode) design → plan → build, then SP-6 (paywall sandbox), then landing page.

---

## 2026-06-11 — SP-7 EXECUTED — settings, completion notification, dark mode

- Spec `f210c40` + plan `c1191f6` (7 tasks) authored same-day; commits `741e49b → 03ac51b` all on origin/main. Suite **83/83** (76 + 4 prefs round-trips + 3 notifier-guard); zero engine-file touches (verified diff vs `f210c40`; sole permitted exception: try/caught notifier hook in ProgressRepository).
- T1 ThemePref + notifyOnCompletion prefs (TDD) · T2 dark bottle-green scheme + live ThemeState mirror · T3 CompletionNotifier (channel/guard/PendingIntent→Celebration via pendingCelebration) · T4 SettingsScreen (rows, appearance segmented control, notify toggle + API-33 permission flow, version footer; buildConfig=true) · T5 language/apps/intention editors (reuse SelectableCard; empty-set guard) · T6 routes + Home gear.
- Two parallel Opus reviews: data/infra pass clean; UI pass flagged 2 → fixed `03ac51b`: centered segment labels + **NiyamExtraColors CompositionLocal** so pre-dark-mode screens (Library chips, detail InfoChips, shared SelectableCard) stop hardcoding light tokens. Light mode byte-identical.
- Dark overlay variant deliberately excluded → forlater.md item 5. Report: `docs/superpowers/test-reports/2026-06-11-sp7-acceptance.md`.

### Where to pick up
Free-tier app surface is complete (SP 1-5, 7). Next: **SP-6 paywall/trial in sandbox mode** (design → plan → build; live billing gated on Pranav's Play Console/RevenueCat answers), then the landing page.

---

## 2026-06-11 (later) — Screenshot walkthrough + two ENGINE FINDINGS

- Captured 21-screenshot gallery on Pixel 9 emulator (fresh install, full walkthrough incl. overlay over YouTube + dark mode): `docs/screenshots/index.html`, commit `31dbdd5`. Theme reset to Light after captures; emulator killed.
- Fixed during walkthrough: switch-dialog "1 days" → plurals resource (`detail_switch_body_plural`), suite still 83/83. Queued: duplicate roman line when language=English (forlater item 6).
- **ENGINE FINDING 1 (product decision needed):** Continue grants only the 2s debounce — the next TYPE_WINDOW_STATE_CHANGED inside the same blocked app re-triggers the overlay (AppLockAccessibilityService.kt: debounce check only `< DEBOUNCE_MS`). In real use, in-app navigation after Continue re-blocks almost immediately. Recommendation: per-package grace period (e.g. 5 min) after Continue. NOT touched — engine requires explicit approval.
- **ENGINE FINDING 2 (bug):** Nothing hides the overlay when the foreground app changes to a NON-blocked app. Empirically: overlay re-shown over YouTube persisted over launcher and over Niyam itself until Continue was tapped (screenshot 17). Recommendation: on window state change to a non-blocked package while overlay is showing → hide. NOT touched — same rule.
- Pranav's SP-6 answers received: freemium confirmed; sample/test ads OK (AdMob test units); trial per my rec; needs RevenueCat-in-plain-English + account list; landing page after app done.
