# Niyam — Session Log

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
