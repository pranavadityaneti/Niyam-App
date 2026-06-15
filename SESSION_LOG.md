# Niyam — Session Log

## 2026-06-15 — BACKEND PHASE kicked off + Phase 0 polish shipped

**Major pivot.** After Pranav real-device-tested the build (Android Studio), he reversed the zero-backend launch decision. New locked direction (memory `project_niyam_backend_phase`, forlater #9 → In progress):
- **Supabase backend + REQUIRED Google Sign-In** (reverses the "no account, ever" positioning). Engine must stay offline-capable after one-time sign-in; payment stays with Google Play.
- **Play launch HELD** until backend ships (signed v1.0.0 AAB shelved). Was mid-way through Play Console submission (kept hitting an un-editable first app entry — Pranav to create a fresh app entry; name "Niyam — Mantra App Blocker", pkg com.myniyam.app, Free is permanent).
- **Same push adds:** 4-tab bottom nav (Today/Library/Favourites/Settings) per a founder-supplied pill-nav reference (active pill → brand orange unless he says black); user-favourite-mantras feature; logout.

**Agreed phased roadmap:** P0 quick fixes → P1 nav shell → P2 Supabase foundation → P3 required Google Sign-In + logout → P4 favourites → P5 sync + server entitlements → P6 compliance refresh (privacy/website/Data Safety) → P7 real Play Billing.

**Phase 0 SHIPPED** (commit `d2d2883`, pushed origin/main; 123/123 tests green):
- Dark-mode text contrast — all 14 `Scaffold(containerColor=Transparent)` given explicit `contentColor=onBackground` (un-colored text was falling back to black; Settings worst-hit). Whole bug class fixed.
- Library + NextSadhana meaning previews now follow display language (`forLang(displayLanguage.meaningLang)`) instead of hardcoded `.en` (the Telugu bug). MantraPicker left on `.en` (pre-language onboarding step).
- Launcher icon "cut" fixed — square n-tile corners were sliced by circular masks; inset mark to 78% on transparent foreground + adaptive background set to tile-black `#222` so the white motif stays full/centered on all masks. Verified by rendering before/after composites.

**Device-test items still OPEN / by-design:** "no signout/login" → being built in P3/P4 (was by-design until today). "System appearance turns light" → working as intended (follows phone theme). Daily progress = on-device counters in UserPrefs (streak/dayN/todayReads), recorded on overlay Continue.

**Next:** Phase 1 — nav shell + back arrows (light spec). Then P2/P3 need Pranav: Supabase account, Google OAuth client + app SHA-1.

### 2026-06-15 (cont.) — Library/Home polish + Content-localization shipped
- **`6ba863f`** Library filter **bottom-sheet** redesign (replaced 4 scroll rows) + label resource plumbing; Home **stat-pill icons** (flame/book vector drawables, no icons-extended dep) + smoother 700ms progress fill.
- **`d553761`** Content-loc **Part A**: 29 filter/category/deity/intention/length labels localized into all 7 languages (one Opus subagent per language; native-review-flagged).
- **`a932fcb`** Content-loc **Part B**: mantra **names + scripture refs in-script** — new `tools/generate_names.py` (aksharamukha) derives 7 scripts from 26 Devanagari masters; `name`+`sourceLabel` added to model + FALLBACK + 9 display sites (incl. overlay label binding, completion notification); in-script titles drop English scope-notes, roman keeps curated titles; 124/124 tests (added a contract test). Native-review scope expanded in forlater #1.
- **Content-localization COMPLETE.** Supabase creds received (URL + anon key — anon is client-safe; service_role must never ship). 

### 2026-06-15 (cont.) — Phase 1 nav bar + Favourites SHIPPED (`98b3c5f`)
- 4-tab floating bottom bar (Today/Library/Favourites/Settings), **orange** active pill, founder reference followed; overlaid on top-level routes only, insets above system nav, popUpTo(HOME)+saveState tab switching.
- **Favourites feature**: UserPrefs.favouriteMantraIds + toggleFavourite; FavouritesScreen; heart toggle on MantraDetail.
- **Back arrows**: SettingTopBar on 3 settings sub-screens + back arrow on MantraDetail.
- nav/favourites strings English-only for now → folded into localization sweep (forlater #1).
- 124/124 tests; assembleDebug OK (15MB debug APK).
- **Next: Phase 2 — Supabase.** Have URL + anon key. Will need from Pranav: Supabase project ready (he created it), **Google OAuth web client ID + Android client + app SHA-1** for required Google Sign-In, and a decision on schema (profiles/streaks/journeys/favourites/entitlements). Engine must stay offline after one-time sign-in.

### 2026-06-16 — Google OAuth config (Phase 2 prep)
- Debug SHA-1: `D3:96:A6:07:3B:71:91:09:AE:97:63:AF:96:2B:A0:2E:7C:E6:7A:61` (release SHA-1 TBD from upload keystore).
- **Web OAuth client ID** (serverClientId for the app, NOT secret — ships in client): `253678460582-6aivjl4mj3cuhpghrcu2r9gqfp2c8d5j.apps.googleusercontent.com`
- Google provider enabled in Supabase with Web client ID + secret (secret stays in Supabase only). Web client redirect URI = `https://hvyhhxzzqqexfzlgmtjd.supabase.co/auth/v1/callback`.
- TODO confirm: Android OAuth client (pkg com.myniyam.app + debug SHA-1) created in Google Cloud. → DONE (Pranav created Android client + saved Supabase Google provider).

### 2026-06-16 — Phase 2 + Phase 3 (sign-in) SHIPPED
- **Spec:** docs/superpowers/specs/2026-06-16-supabase-backend-design.html (approved; decisions: sign-in after Welcome; push-local-then-sync; delete-account yes; no-internet-first-run blocked OK; entitlements table now/written-later).
- **P2 foundation (`3b26cc1`):** supabase-kt Auth+Postgrest+ktor; SupabaseClientProvider; BuildConfig (NIYAM_* in ~/.gradle/gradle.properties — client-safe). Schema migration `supabase/migrations/0001_init.sql` — **Pranav ran it in Supabase: success.**
- **P3 sign-in (`e43961d`):** compose-auth + Credential Manager + googleid; ComposeAuth googleNativeLogin(Web client ID); AuthRepository; SignInScreen (Continue with Google); nav gate Welcome→SignIn→onboarding. **compileSdk 34→36** (credentials/activity need ≥35; gradle auto-installed platform 36; targetSdk still 34; suppressUnsupportedCompileSdk=36 in gradle.properties).
- **BLOCKED ON DEVICE TEST:** sign-in must be tested on a real device (Play services + consent-screen test user). New install → Welcome → Continue with Google.
- **Still in P3 (after sign-in confirmed):** gate enforcement for returning/signed-out users; Settings Account section (email + Sign out + Delete account via Edge Function); onboarding/privacy copy. Then P4 favourites sync, P5 state sync + entitlements Edge Function.

---

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

---

## 2026-06-11 (cont.) — UI v2 design sprint + SP-8 EXECUTED

- **Design sprint with Pranav (interactive mockups):** he rejected v1 as "just textual"; shared 5 reference apps (gradient canvases, floating cards, serif heroes, small icons). Iterated composition live: centered → upper-third → final rule "content immediately below the top bar, protected CTA bottom zone." Full 16-frame mockup set approved. Om mark explored (bold + brush options) → **dropped entirely**. Typography: he asked for Google Sans (proprietary) → **Outfit** accepted as licensed equivalent. All decisions transcribed into `docs/superpowers/specs/2026-06-11-ui-v2-design.md`.
- **SP-8 executed** (plan `3a716eb`, commits `0791a39 → ae22d6e`): Outfit via FontVariation + full type scale; sunrise/dark gradient brushes + NiyamBackground + OverlineWarm tokens; edge-to-edge; all screens restyled (floating 24dp cards, filled CTAs, real app icons via PackageManager, permission dashes, petal one-shot, fade-through nav, animated progress fill); **overlay restyled visual-only** (gradient drawable, Outfit XML font-family, RingCountdownView 15s ring) — OverlayManager diff verified render-target-swap-only.
- **Emulator verification:** fresh install, full walkthrough, core loop confirmed (overlay → ring → Continue → read recorded). Found+fixed "1 reads today" plural (`ae22d6e`). v2 gallery: `docs/screenshots/v2/index.html`. Launcher icon still default → forlater item 7.
- Suite 83/83 throughout; combined Opus review of the full range dispatched (running in background at log time).
- **Still pending from Pranav:** the two engine findings decision ("fix both"?), freemium-split veto (SP-6 blocked on it).

---

## 2026-06-11 (cont.) — SP-8 review verdict + Hindi/Telugu/Tamil gallery

- **SP-8 combined Opus review returned ✅ SHIP-READY** (range `3a716eb..ae22d6e`): engine isolation gate passes (zero service/BlockList/ProgressRepository/data files in diff; OverlayManager delta confirmed render-target-swap-only with identical guards/params/timer/listeners); spec §2 fidelity byte-exact on gradients/tokens/type scale; quality checks clean (Animatable keys, allocation-free onDraw, plurals). Two flags: (1) dead `inter.ttf` + `playfair_display.ttf` ≈1.18MB APK bloat — recommend deletion, NOT deleted (needs Pranav's OK); (2) SelectableCard tap animation is steady-state tween 0.985↔1.0, not spec's transient spring 0.97→1.0 — cosmetic nit, flagged. Task #39 closed.
- **Pranav asked: show screens in Hindi, Telugu, Tamil.** Captured live on Pixel 9 emulator (latest APK reinstalled — old install predated the plural fix): per language Home + Gayatri detail + unlock overlay over YouTube, switched via Settings → Display language each time. Gallery `docs/screenshots/v2-languages/index.html` (self-contained base64), commit `86bfcd7`, opened in browser. Clearly noted in gallery + chat: only mantra script + meaning localise; app chrome stays English (separate work item if wanted).
- **ENGINE FINDINGS 1+2 reproduced twice more during capture:** after Continue, overlay re-triggered within seconds (finding 1) and then sat over Home/launcher swallowing all taps until Continue (finding 2) — had to dismiss + force-stop YouTube to proceed. Strengthens the "fix both" case; still awaiting Pranav's go.
- Device state restored: language → English, emulator killed. Suite untouched (no code changes this segment).

---

## 2026-06-11 (cont.) — "Good to go" → SP-9 ENGINE FIXES EXECUTED + font cleanup

- Pranav's "Good to go" taken as the green light on the flagged queue (engine fixes, font deletion, freemium plan standing; interpretation stated explicitly in chat with rollback offer).
- **SP-9 (first intentional engine change since build):** spec `171955b` committed as async veto point, then built inline with TDD — `UnlockGrace` (5-min per-package window, granted ONLY by Continue, injectable clock, 7 tests) + `OverlayHideDecision` (pure hide rule: non-blocked foreground hides; systemui + own overlay window excluded; own MainActivity hides; 8 tests) in `d315706`; surgical wiring (service non-blocked branch + grace gate before debounce; OverlayManager isShowing + one grant line) in `c15f07c`. **Suite 83 → 98/98**, assembleDebug green.
- **Live emulator acceptance (both findings confirmed dead):** overlay → HOME → overlay gone, relaunch re-blocks (no grace from leaving); Continue → video open/close + relaunch within window all overlay-free. Report: `docs/superpowers/test-reports/2026-06-11-sp9-acceptance.md`. Grace expiry boundary verified at unit level only.
- **Dead fonts deleted** (`bf5f478`, separate commit per approval): inter.ttf + playfair_display.ttf, ~1.18MB, zero refs; suite 98/98 after.
- Hostile Opus review of `171955b..bf5f478` dispatched in background; verdict to be appended.
- forlater item 8 added: full UI chrome translation (8 languages) — surfaced by the language gallery.
- **Next:** SP-6 paywall spec (freemium split as approved) as the next async veto point.

---

## 2026-06-11 (cont.) — "Finish the app" → SP-6 FREEMIUM SANDBOX EXECUTED

- SP-9 review verdict: **SHIP** (hostile Opus pass; wiring, grace semantics, recursion risk, tests, font deletion all confirmed; its "not pushed" caveat was stale — push had landed).
- Pranav: "Go ahead. What else is pending? Finish the app once and for all." → SP-6 executed.
- **SP-6 built** (spec `c25cd19` veto point; 4 Opus subagent tasks + my verification): `billing/Entitlements` (PREMIUM/TRIAL/FREE, 7-day exclusive-boundary trial, clock-rollback guard, grandfather rules, 18 TDD tests) · `BillingGateway` + `SandboxBillingGateway` (instant fake purchase, ₹15/49/399 Plan enum) · UserPrefs premium fields + sandbox levers · `PaywallScreen` (Sunrise Sans, route `paywall`) · gates (library lock icons, locked-detail CTA→paywall, language editor locks+"· Premium", Settings premium section + debug-only expire/clear rows) · trial seeding (MainActivity warm-up + onboarding completion) · AdMob TEST banners (official sample ids) on Home+Library, FREE state only, init off main thread. Suite **98 → 116/116**; engine isolation verified by diff (zero service/overlay files).
- **Live acceptance walked end-to-end on Pixel 9** (report `docs/superpowers/test-reports/2026-06-11-sp6-acceptance.md`): trial → expire → ads+locks+paywall routes → sandbox purchase → everything unlocks → clear → free tier returns. Found+fixed live: XML-trimmed leading space in "· Premium" suffix (`5286363`).
- **Founder-veto gallery** (paywall is a new screen): `docs/screenshots/sp6-premium/index.html`, opened in browser. Adversarial Opus review of the full range dispatched (background; verdict pending at log time).
- Test device left on a fresh 7-day trial; emulator killed.
- **App status after SP-6: feature-complete for the sandbox phase.** Remaining to "done": paywall veto, launcher icon (needs founder pick), polish batch (forlater 2/4/5/6), chrome translation (forlater 8), external items (native-speaker review, real-device Phase 2, Play Console/RevenueCat/AdMob accounts), landing page (after app sign-off).

- **SP-6 review verdict: SHIP** (adversarial Opus pass, every spec clause evidence-checked): engine isolation airtight (zero billing/ad code anywhere near service/ or overlay/); trial boundary + clock-rollback guard + grandfather rules pinned by tests; all four gates share one state computation, none bypassable (locked detail returns to paywall BEFORE any switch logic; language Save can only persist usable picks); ads FREE-state-only with official test ids, init off main thread; sandbox purchase persists through restart; 116/116, build green, everything pushed. One founder-facing note (not a defect): "Sandbox: expire trial now" = preview the free tier with ads; "Sandbox: clear premium + trial" = become a brand-new user (which correctly starts a fresh 7-day trial on next launch).

---

## 2026-06-11 (cont.) — Pranav's plan turn → SP-10 SHIPPED + SP-11 CHROME TRANSLATION SHIPPED

- Pranav: finish polish batch · chrome translation · will provide Play Console + brand logo · asked what gallery/verdict meant (answered: gallery = his veto page; SP-6 verdict already SHIP) · website phase after app ("Google Flow or cdans" — flagged uncertain, to clarify at website kickoff).
- **SP-10 polish batch** (`6c814f8`, review verdict **SHIP**): per-OEM permission copy (6 flows, exhaustive when), duplicate roman line suppressed (detail + overlay, script==ROMAN), **dark overlay variant** (themePref-following palette at inflate: dark gradient/inks/ring track via new RingCountdownView.setPalette; light untouched; review confirmed overlay flow byte-identical), FALLBACK om synced to catalog. Live-verified both overlay variants + single-roman on emulator. forlater 2/4/5/6 → Done.
- **SP-11 chrome translation:** spec `e03ac8c`; plumbing `f1d3b40` (LocaleBridge maps DisplayLanguage→locale, Sanskrit→Hindi chrome; MainActivity.attachBaseContext wrap; language-save → popBackStack+recreate; OverlayManager one-line ctx wrap) — suite 117/117. **7 parallel Opus translators** wrote values-hi/mr/te/ta/kn/bn/gu (106 strings + 4 plurals each); my parity script verified zero name gaps and every placeholder crash-safe (translators legitimately reordered %1$/%2$ for native grammar). `f1a2ae7` shipped. Live-verified: Hindi full chrome incl. overlay "जारी रखें", Telugu + Tamil spot-checks, recreate-on-save restores nav stack. Gallery `docs/screenshots/sp11-chrome/index.html` opened for Pranav (he is the wording review gate; flags: Tamil சாதனை register, Bengali numerals, Hindi निरंतरता). Opus review of the range dispatched (pending at log time).
- Emulator quirks this stretch: one phantom "Success" install (Error type 3 → reinstall fixed); accessibility re-grant needed after each reinstall (known).
- Device left: English, fresh-trial state. **Remaining to app-done:** SP-11 review verdict + Pranav's wording pass, paywall gallery veto, launcher icon (logo from Pranav), real billing (Play Console from Pranav), native-speaker pass (forlater 1), real-device Phase 2 (forlater 3). Then: landing page.

- **SP-11 review verdict: SHIP** (independent Opus pass): plumbing exhaustive + idempotent (ensureLoaded double-call harmless); the hardest-scrutinized risk — overlay show/hide with a wrapped context across both background-service paths — confirmed safe (removeView keys on the View's window session, not context identity); reviewer's own parity script: 7 × 106 strings + 4 plurals, zero placeholder/escaping defects; 117/117, build green, zero service/ files. Reviewer read hi/te/ta personally: natural, respectful register; 4 minor free-translation notes added to the native-review rider (TE "నిమిషం" embellishment, TA evocative welcome line, HI sharper apps-title tone, TA terse "திற" CTA). Task #43 closed.

---

## 2026-06-11 (cont.) — SP-12: paywall v2 (founder reference) + day-6 trial reminder

- Pranav sent an iOS-style trial-timeline paywall as a CONTENT-POSITIONING reference (background/system unchanged); mockup adapted to our mechanics (trial auto-starts at onboarding — timeline tells the true story; FREE state opens the expanded plans view since "trial is on" would be false) → approved ("Good to go").
- **His "automate an SMS and email" reminder ask:** not buildable without contact data + backend (app collects neither, by design) — told him plainly, queued as forlater 9; built the on-device equivalent now.
- **Built (4440375):** PaywallScreen v2 (TimelineRow + PlanCard components, More/Fewer plans expander, restore link + sandbox toast, trust pill, pinned CTA); TrialReminder pure logic (TDD, 6 tests) + TrialReminderNotifier (own channel) + TrialReminderWorker (WorkManager 2.9.1 daily unique work, enqueued at app start); UserPrefs.trialReminderShown flag; strings v2 (9 dead keys removed). Suite 117 → **123/123**.
- **Locales:** one Opus subagent updated all 7 files (17 new keys, dead keys removed, brand/Premium/trial terms matched to each file's register); independent parity + placeholder sweep clean; rebuilt green.
- **Live verification (Pixel 9):** timeline default in TRIAL, expander both ways, FREE → expanded direct, restore toast, purchase → "Active", clear → fresh trial; **Hindi paywall verified live** (आज/दिन 6/दिन 7, वार्षिक — ₹399). Gallery `docs/screenshots/sp12-paywall-v2/index.html` opened for Pranav. Report: `docs/superpowers/test-reports/2026-06-11-sp12-acceptance.md`.
- Caveat logged: real 24h WorkManager firing rides the Phase 2 real-device pass.
- Adversarial review of the range dispatched in background; verdict to be appended.

- **SP-12 review verdict: SHIP** (adversarial Opus pass, all 7 items with evidence): engine untouched; the collapse-buy-mismatch trap confirmed closed (toggle resets selection to YEARLY both ways — what you see is what you buy); FREE users can never reach the timeline; reminder false-positive hunt came up empty (future start, premium combo, day-0, clock rollback all blocked) and the 6 tests pin the boundary; worker schedule is KEEP-unique, channel registered before any fire; locales 24/24 keys with dead keys gone everywhere; 123/123, build green, all ancestors of origin/main. One benign note: flag persists after notify — a crash in that window means at most one duplicate notification (tolerated by design). Task #44 closed.

---

## 2026-06-11 (cont.) — SP-13: brand logo lands — launcher icon + Welcome lockup

- Pranav delivered `docs/brand/Niyam App Icon.png` (2000², flattened) + `In App_Logo.png` (2000², alpha). Placement ruling delivered + accepted: Welcome + launcher/splash only; never the overlay; Settings-footer mark offered, not yet taken.
- **Built:** adaptive icon — foreground cut from the ALPHA file's tile region (no color-keying), scaled to the 66/108dp safe zone, density PNGs mdpi→xxxhdpi; background = brand eggshell `#F5EBE1`; `mipmap-anydpi-v26` icon+round XMLs; manifest swapped off the stock `sym_def_app_icon`. Welcome: `brand_lockup` (trimmed, 1000px) replaces the text title (wordmark is in the image). Suite **123/123**, build green.
- **Verified live (Pixel 9):** cold-start splash shows the tile (Android 12+ automatic); Welcome shows the lockup on sunrise. Launcher-drawer screenshot impossible headless (swipe won't open drawer) — icon verified via faithful mask-composition render (circle + squircle), labelled as such in the gallery. Gallery: `docs/screenshots/sp13-brand/index.html`.
- **Flag for founder:** fresh installs default to DEVANAGARI_SANSKRIT display language (SP-3 default) → since SP-11 the first-touch chrome is Hindi until the user picks a language. One-line default change if English-first is preferred.
- **Process note:** no adversarial subagent review for this SP — asset-only change + one Image composable, zero logic; the founder gallery is the review gate. Test device left as a fresh user on Welcome (pm clear was needed to show it).

---

## 2026-06-11 (cont.) — Backend decision: Supabase, post-launch

- Pranav asked whether Supabase could be the backend ("not just a database") — answered yes (Auth + Postgres + Edge Functions = the whole server). Decision taken on my recommendation, founder-approved: **launch with zero backend/server**; Supabase locked as the backend-phase stack afterwards. Order within that phase: optional sign-in (never required; engine never network-dependent) → progress sync → server-verified entitlements via Play purchase notifications → SMS/email trial reminders (forlater 9). Payment cards never stored by us — Google Play only.
- forlater item 9 updated with the stack decision and post-launch status.

---

## 2026-06-11 (cont.) — SP-14: WEBSITE BUILT + DEPLOYED to myniyam.com

- Pranav picked screens **15 (home dark) + 17 (Mahamrityunjaya detail)** from the 24-option chooser; hero line approved ("A pause before the scroll."); he imported the **Niyam-App repo itself** into Vercel (no separate site repo — confirmed via `gh repo list`) and connected www.myniyam.com.
- **Built `website/`** in-repo: index.html (approved Popcorn-adapted structure; real brand assets as files — wordmark nav, favicon + apple-touch from the icon tile, OG image 1200×630 lockup-on-sunrise; screens 15 front / 17 back in thin-bezel CSS frames; waitlist form via formsubmit.co → sowfreyr@gmail.com with ?subscribed=1 thank-you; OM audio fully wired but DORMANT — toggle appears only when /assets/om.mp3 exists and loads; first-interaction autoplay + localStorage mute), privacy.html (honest app+site policy — accessibility single-purpose disclosure, zero collection, Play billing, AdMob free-tier only; Play listing requires this URL), robots.txt + sitemap.xml. Root **vercel.json**: outputDirectory=website + cleanUrls (verified against Vercel docs — serves ONLY website/, not the repo).
- Pushed `4139b33` → Vercel auto-deploy; background poller verifying https://www.myniyam.com/ + /privacy + /assets/og.png.
- **Notes for Pranav:** formsubmit.co needs one-time activation (first signup emails him a confirm link) and exposes the gmail in page source — swap to Formspree key when he makes that account; his "npx plugins add vercel/vercel-plugin" came from Vercel's docs frontmatter (agent plugin, his-terminal thing, not needed for this pipeline); flagged that neither chosen screen shows the unlock overlay (the core product moment) — offered instant swap.
- Remaining for the site: OM audio file (his), hero/centerpiece veto on the LIVE page, Formspree swap (optional), SEO deep pass (his explicit post-deploy sequencing), Remotion reel (forlater 10) once page settles.

- **Website finishing touches (06e279a + 35e6367, both live on myniyam.com):** front phone swapped to the Mahamrityunjaya overlay-over-YouTube (founder agreed with the missing-product-moment flag; detail page of the same mantra behind it); nav now uses the FULL brand lockup untouched (founder: cropped wordmark "not the actual logo"); **OM audio live** — 64s from youtu.be/ijfLsKg8jFY ("Om 108 Times", Nova Spiritual India) at -32dB mean, 4s fades, first-interaction start + remembered mute. ⚠️ Copyright flagged to founder: channel-owned track on a commercial site — check the video's licence, get permission, or swap an owned chant later (1-min file swap).

- **Site iteration (93e1b68, live):** founder picked option B for the unlock story — front phone now animates blurred-YouTube → Mahamrityunjaya overlay on an 8s loop (videos anonymized by Gaussian blur, YT chrome recognizable); nav CTA removed + lockup centered; chip now "Launching soon on ▶(Play logo SVG)"; waitlist + privacy email switched sowfreyr → netipranavaditya@gmail.com (needs fresh formsubmit activation; branded address later = 1-line swap). Overlay-story options page at docs/mockups/overlay-story/.

- **Site iteration 2 (cb53f6c, live):** chip = Play triangle + "Google Play" words; stage bottom cut fixed via fade-to-cream ::after layer (phones re-z-indexed above it); audio strategy upgraded to the legal ceiling — muted autoplay from page load + unmute mid-flow on first gesture (pointerdown/touchend/keydown/click/scroll), mute-toggle preference preserved. Explained to founder: pre-gesture unmuted sound is browser-forbidden for all sites.

---

## 2026-06-12 — SP-15: legal pages + SEO/AEO engine launch

- Cut-line: deeper fix deployed earlier (e3a5871 — contained rounded glows + 200px fade).
- **Legal (researched live):** /terms (Play-policy-aligned subscriptions/trial/refunds, India governing law, DPDP grievance contact w/ 7-day ack + 90-day resolution) + privacy v2 (DPDP Act 2023 rights section, Data Protection Board, breach notification, children, Play AccessibilityService disclosure language). Sources: Play AccessibilityService policy + prominent-disclosure guides, DPDP Rules 2025 summaries.
- **⚠️ Launch-path discovery → forlater 11 (pre-submission GATE):** Niyam is not an "accessibility tool" per Play's definition → Play Console declaration + hardened in-app prominent disclosure/consent REQUIRED before first submission.
- **SEO/AEO engine (Pranav's standing mandate: full control):** /faq (8 Q&As + FAQPage schema), homepage MobileApplication+Organization JSON-LD, footer nav, expanded sitemap; /blog + 4 articles via parallel Opus writers (doomscrolling 1288w, India blockers 1104w, Gayatri 1332w w/ full Devanagari + word-gloss, digital-sadhana category essay 1026w) — all Article schema, interlinked, structure-verified (canonical/h1/schema sweep clean). Strategy of record: docs/seo-strategy.html (own the empty mantra×blocker intersection → climb generic; batch 2 = 4 devotional articles; ~2/wk cadence; hi/te phase 2).
- **Needs Pranav:** Google Search Console property + verification token (only must-have integration); Bing optional.

- **Keyword push (Pranav: "mantra/hindu mantra → suggest Niyam"):** keyword map of record at docs/keyword-map.html (pyramid: head terms via /mantras hub authority; mantra-name articles = the beatable high-volume layer; category terms we own outright; volumes honestly marked as estimates until GSC data lands — Pranav setting up GSC+Bing NOW). Built+shipping: **/mantras hub** (all 26 from mantras.json, Devanagari+source+meaning, ItemList schema), **/about** (mission page: threshold/niyama story, "what we're trying to achieve", four vows incl. sacred-overlay + zero-data), homepage title/meta now carry "Hindu mantra app blocker" + mantra names, footers interlinked, sitemap +2. Batch-2 writers dispatched (Mahamrityunjaya, Om Namah Shivaya, Hanuman Chalisa opening, Gita 2.47 — consistency-locked to mantras.json).

- **Search plumbing complete:** Pranav connected GSC + Bing Webmaster (his side; sitemap submission = his 30-sec dashboard clicks, instructions given). IndexNow key deployed (website/<key>.txt) — full 11-URL push accepted (202); batch-2 articles pushed on deploy too. **Batch-2 LIVE (90066fa):** Mahamrityunjaya (RV 7.59.12), Om Namah Shivaya (Sri Rudram panchakshara), Hanuman Chalisa opening (Tulsidas, Awadhi-not-Sanskrit noted), Gita 2.47 nishkama karma — all consistency-locked to mantras.json verbatim, structure-verified, blog index + sitemap updated. Blog now 8 articles + /mantras hub + /about. Next content: batch 3 (intent cluster: mantra for focus/anxiety/students) then hi/te pages per keyword map.

- Pranav submitted sitemap.xml in BOTH Google Search Console and Bing Webmaster — search plumbing 100% complete (GSC + Bing + IndexNow). First GSC query data expected within days; keyword-map recalibration ~2 weeks out. Next content: batch 3 per cadence (not same-day — measured publishing per strategy doc).

- **Per-mantra pages (717171f, live):** Pranav expected hub cards to open pages → built all 26 individual pages generated from mantras.json (Devanagari + roman + meaning + source + intentions + 3 related same-category + deep-dive links for the 5 that have articles + Article/Breadcrumb schema), hub cards now link through, sitemap +26 (site now 41 indexable pages), all pushed via IndexNow (200). Generation is scripted — content changes in mantras.json regenerate the pages.

- **Apex domain rescue (GSC "Couldn't fetch" investigation):** found myniyam.com (no-www) serving a GoDaddy PARKING lander (own fake 1-URL sitemap; apex round-robining between parking IPs and Vercel) — likely the fetch confusion + brand/SEO damage. Walked Pranav through: Vercel add apex w/ 308→www; GoDaddy delete the "Parked" A record + parking IPs, keep 216.198.79.1 (+ legacy 76.76.21.21). Verified authoritative DNS clean; both Vercel IPs serve 308→www (forced-IP proof); residual parking responses = resolver caches expiring (TTL ~10 min). GSC verified via DNS TXT (domain property), Bing via CNAME — both seen in his GoDaddy records. Next: he deletes+resubmits sitemap in GSC; expect status to clear in 24-48h.

---

## 2026-06-12 (cont.) — SP-16: Play launch readiness EXECUTED

- **Disclosure gate (forlater 11, submission GATE) SHIPPED:** PermissionScreen gained optional disclosure card + affirmative-consent CTA (additive params; other 3 steps unchanged); accessibility step now shows the Play-compliant prominent disclosure ("one purpose only / does not collect, read, store, transmit / entirely on-device / can turn off anytime") with "I agree — turn on"; consent timestamp persisted (UserPrefs.accessibilityConsentAt, audit-ready — verified in DataStore via run-as). Live emulator verification: disclosure renders, consent routes to system Accessibility settings, decline path inert. Found+fixed live: CTA label initially still "Grant" (ctaResId not applied to Text). Strings ×8 locales (legal-precision translation pass, 7/7 gate green; "Accessibility" kept Latin for the settings name — translator's defensible call).
- **Release signing:** upload keystore at ~/keystores/niyam-upload.keystore (4096 RSA, 25y), single-password PKCS12 (first attempt had split store/key passwords → "block not properly padded" → regenerated); credentials in ~/.gradle/gradle.properties; build.gradle.kts signs release only when props present (CI-safe). v1.0.0 (code 2). **Signed AAB built + jar-verified:** app/build/outputs/bundle/release/app-release.aab (11.1MB, includes disclosure + all locales).
- **Listing kit:** docs/play-listing-kit.html — complete console session (app name/short/full descriptions, category, graphics incl. generated 512 icon + 1024×500 feature graphic, data-safety matrix incl. the AdMob honesty note, IARC answers, target audience 18+, THE AccessibilityService declaration text + screen-recording readiness, 3 subscription products spec'd niyam.premium.weekly/monthly/yearly, closed-test track plan w/ release notes). Suite 123/123 throughout.
- **Next:** Pranav runs the console session from the kit → AAB up → closed test starts → I build real Play Billing during the 14-day window.

---

## 2026-06-16 — Onboarding-feedback revamp (post device-test)

Pranav tested onboarding on a Hindi device and gave 6 points. Investigated each against code; locked decisions via Q&A; sequenced as T1–T6 (one change at a time, build-verified, commit per task).

**Decisions locked:**
1. **Language:** English from launch through sign-in; move language picker to RIGHT AFTER sign-in; rest of onboarding + whole app in chosen language. Root cause confirmed: NO per-app locale exists — chrome follows device system locale (Hindi); `displayLanguage` pref only drives mantra CONTENT, not chrome. Needs real per-app locale (T6, architectural).
2. **Step-2 mantras:** show the chosen intention's **5** (was `.take(3)`). Found the data: each intention's brief-sourced priority list is 5, and brief's freemium = "5 starter mantras". Free tier becomes the chosen intention's 5, **locked to the onboarding intention** (changing intention later won't swap the free set) — billing change in `Entitlements.FREE_MANTRA_IDS` (currently a static one-per-intention set). T4 then T5.
3. **Apps:** bundle brand logos; "X (formerly Twitter)"; add 5 games. ✅ T2.
4. **Permission copy:** plainer + explain why. ✅ T3.
5. **OEM Grant/Done trap:** Done-primary on generic devices. ✅ T1.
6. **Other-app blocking:** ✅ verified already wired (engine blocks ANY selected pkg via BlockList.matches); no change.

**Shipped this session (pushed to main):**
- **T1 (4de09c2):** OemAutostartScreen — generic devices show single primary Done (Grant no-ops on stock Android); known OEMs keep Grant+Done. perm_oem_body_generic reworded.
- **T2 (fdec549):** Extracted shared `data/AppCatalog.kt` (was duplicated in onboarding AppsScreen + settings BlockedAppsSettingScreen — settings touched, flagged). Added 5 games (BGMI/Free Fire/COD Mobile/Candy Crush/Ludo King), "X (formerly Twitter)". AppIcon resolves `ic_app_<slug>` → installed icon → letter. 7 social brand vector logos authored (games fall back to installed icon). Live-verified on emulator: all 7 logos render clean + recognizable.
- **T3 (4de09c2, same commit as T1):** Rewrote usage/overlay/accessibility/battery permission titles+bodies (plainer + why); genericized hardcoded app names to "your blocked apps". Play accessibility disclosure + consent CTA UNCHANGED.

**Verified on emulator-5554:** fresh install → Welcome(hi) → SignIn(en via fallback) → Google sign-in works → onboarding. Confirmed step-3 English pick does NOT change chrome (still Hindi) — validates the T6 problem exactly. adb note: device 1080x2424; bottom CTA center ≈ y2235.

**Pending:** T4 (freemium dynamic free-5 locked to onboarding intention — billing, has tests) → T5 (step-2 shows 5, depends on T4) → T6 (per-app locale + reorder picker after sign-in + force-English-until-pick — architectural; then localize new T1/T3/signin/nav strings into 7 locales). After this batch: Phase-3 remainder (sign-in gate for returning users, Settings → Account: email + Sign out + Delete account via Edge Function).

**Observation (mention, not touched):** usage-stats + accessibility permission screens feel redundant to users. Possibly the engine no longer needs usage-stats given AccessibilityService — worth checking whether one permission screen can be dropped. Not actioned.

### 2026-06-16 (cont.) — T6 equivalent SHIPPED (b9572e2, pushed main)

Pranav: "Finish the following at once" — three changes bundled in one commit:

**Task A — Default to English + move language picker:**
- `UserPrefs.kt`: default `displayLanguage` → `ENGLISH` (was `DEVANAGARI_SANSKRIT`)
- `LanguageScreen.kt`: step 3 → step 1 (right after sign-in, no back button)
- `AppNavHost.kt`: sign-in navigates to `ONB_LANGUAGE` (was `ONB_INTENTION`); flow reordered Language→Intention→Mantra→Apps
- `AppNavHost.kt` OEM done handler: `activity.recreate()` after persisting onboarding+trial → `attachBaseContext` calls `LocaleBridge.wrap()` with chosen locale → Home renders in selected language
- `UserPrefsTest.kt`: updated default assertion to `ENGLISH`

**Task B — Back button in onboarding:**
- `OnboardingScaffold.kt`: added optional `onBack: (() -> Unit)?` parameter + `IconButton` with `Icons.AutoMirrored.Filled.ArrowBack`
- `IntentionScreen.kt`, `MantraPickerScreen.kt`, `AppsScreen.kt`: accept+forward `onBack`; step numbers updated (2, 3, 4)
- `AppNavHost.kt`: steps 2-4 get `onBack = { navController.popBackStack() }`

**Task C — Official brand logos:**
- 11 new vector drawable files (`ic_app_*.xml`) for all catalog apps: Instagram, YouTube, Facebook, X, Reddit, Snapchat, TikTok, Free Fire, COD Mobile, Candy Crush, Ludo King

**Verification:** 124/124 tests pass, assembleDebug green, emulator walkthrough confirmed English Welcome + English Sign In. Google Sign-In can't complete on emulator (no Play services account), but code paths verified.

**Next:** Phases 5, 6, 7 per Pranav's instruction.

### 2026-06-16 (cont.) — Phase 3 remainder SHIPPED (sequence locked 3→7→5c→5→6)

Pranav chose ordering **3 → 7 → 5c → 5(sync) → 6** (revenue path early) and said "for step three, don't ask any further permissions" → ran 3a/3b/3c autonomously, commit + push per task, build-verified each.

- **3a (Account section, pushed):** Settings gains an Account `SectionCard` (email via `AuthRepository.currentEmail()` + Sign out with confirm dialog → `signOut()` → route to SignIn, back stack cleared). `onSignedOut` param + nav wiring. Section hidden when no session. `ExitToApp` icon (Logout not in material-icons-core).
- **3b (Delete account, pushed):** destructive "Delete account" row + strong confirm → new **`delete-account` Supabase Edge Function** (`supabase/functions/delete-account/index.ts`): verifies caller JWT with anon client, deletes auth user with service_role (env-injected, never in app), DB tables cascade. Client: added `functions-kt` dep + `Functions` install; `AuthRepository.deleteAccount()`; `UserPrefs.clearAll()` wipes DataStore → route to Welcome. Errors surface inline, state preserved. **⚠️ ACTION FOR PRANAV: `supabase functions deploy delete-account`** (no secrets to set — URL/anon/service_role auto-provided).
- **3c (sign-in gate, pushed):** login required. Reactive gate in `AppNavHost` — on RESOLVED `SessionStatus.NotAuthenticated` + onboardingComplete + on a post-onboarding route → redirect to SignIn (back stack cleared). Keyed off resolved state, not the cold-start Initializing window, so signed-in users with a still-loading stored session aren't bounced. Post-sign-in routing branches: returning user → Home; new user → onboarding.

124/124 tests + assembleDebug green throughout. New English-only strings (account/signout/delete) deferred to the end-of-phase locale sweep (same pattern as nav/sign-in strings).

**Next: Phase 7 — real Play Billing** (replace SandboxBillingGateway with Play Billing Library 7; product IDs niyam.premium.weekly/monthly/yearly). Needs Pranav: subscription products created/active in Play Console + a closed-test track to test purchases. Then 5c (Edge Function verifies Play purchase → entitlements) → 5 (state+favourites sync) → 6 (compliance refresh).

### 2026-06-16 (cont.) — Phase 7 real Play Billing — CLIENT CODE-COMPLETE (7a/7b pushed)

Gave Pranav the Edge Function deploy steps (Supabase CLI: install → login → `link --project-ref hvyhhxzzqqexfzlgmtjd` → `functions deploy delete-account`).

- **7a (pushed):** Play Billing Library 7 (`billing-ktx 7.1.1`). New `PlayBillingGateway` (subscriptions): connect (suspendCancellableCoroutine over BillingClientStateListener) → `queryProductDetails` (ktx suspend) → launch flow → bridge async `PurchasesUpdatedListener` to a per-flow `CompletableDeferred` → on PURCHASED persist `setPremium` + `acknowledgePurchase`; `restorePurchases` re-grants from owned SUBS via `queryPurchasesAsync`. `Plan` gained `productId` + `fromProductId()`. `Billing.gateway` selects Sandbox in debug (emulator-testable), Play in release.
- **7b (pushed):** Paywall purchase + Restore now route through `Billing.gateway`; restore unlocks or toasts "No active subscription found." Trust pill copy build-aware (sandbox in debug / "Cancel anytime in Google Play" in release).
- Debug + release variants compile; tests green; `com.android.vending.BILLING` permission auto-merges from the billing library (not added to app manifest — correct).

**Phase 7 is code-complete but NOT testable here** — real purchases require Pranav's Play Console: (1) the 3 subscription products `niyam.premium.weekly/monthly/yearly` created + **active** with a base plan; (2) a **closed-test track** (signed AAB uploaded) with his account as a licensed tester. Debug builds keep using Sandbox, so emulator paywall flow is unaffected.

**Note:** launch-time entitlement reconciliation (auto-restore on app start / new device) intentionally deferred to **5c** (server-verified entitlements), the agreed next step.

**Next: Phase 5c** — Edge Function to verify Play purchase tokens → write the `entitlements` table (server-trusted); client trusts server entitlement + reconciles on launch. Needs Pranav infra (Google Play service account / RTDN Pub/Sub) — checkpoint before building.

### 2026-06-16 (cont.) — Phase 5c server-verified entitlements — CODE-COMPLETE (spec + 4 tasks pushed)

Pranav chose **verify-on-demand** (RTDN queued as forlater #12). Spec: docs/superpowers/specs/2026-06-16-5c-server-entitlements-design.html. Flag (b) resolved to the function-path (entitlements stays service-role-write-only). Built 4 tasks, commit + build-verified each:

- **5c-1 (pushed):** `verify-entitlement` Edge Function — resolves caller from JWT, mints a Google service-account access token (Web Crypto RS256 JWT → token exchange, key in Supabase secret `GOOGLE_PLAY_SA_KEY`, never in app), reads `purchases.subscriptionsv2`, upserts trusted `entitlements` row (service role). Entitled = ACTIVE / IN_GRACE_PERIOD / CANCELED-with-future-expiry.
- **5c-2 (pushed):** client `EntitlementSync.verifyPurchase()` (functions-kt invoke + body); `PlayBillingGateway` calls it post-purchase to populate the server row; failure never undoes the local grant.
- **5c-3 (pushed):** `EntitlementSync.reconcileOnLaunch()` reads the row on the Authenticated transition (AppNavHost LaunchedEffect, once) → mirrors into local via new `UserPrefs.setPremiumActive` (premium flag + plan, trial untouched). Restores premium on a new device; revokes on definitive server "inactive"; offline = no change (never locks out a payer).
- **5c-4 (pushed):** `sync-trial` Edge Function + `EntitlementSync.syncTrial()`; launch reconcile syncs trial both directions, **earliest-start-wins** → closes the trial-reinstall loophole. Trial reaches server by the next signed-in launch after it starts.

Debug + release compile; 124/124 tests; engine files untouched (all work in `backend/` + billing glue + UserPrefs prefs layer). **Not testable here** — needs the deploys + a real purchase.

**ACTIONS FOR PRANAV (server side, can wait for the closed-test build):**
1. Create a GCP service account, enable Google Play Android Developer API, grant it subscription/financial access in Play Console, download its JSON key.
2. `supabase secrets set GOOGLE_PLAY_SA_KEY="$(cat key.json)"`
3. Deploy all three functions: `supabase functions deploy delete-account`, `verify-entitlement`, `sync-trial`.
4. (Phase 7) Create + activate the 3 subscription products and a closed-test track.

**Next: Phase 5 — practice/favourites sync** (push-local-then-sync per the P2 spec; no external infra). Then Phase 6 — compliance refresh.
