# Niyam — Play Launch Readiness (SP-16)

**Date:** 2026-06-12 · Sub-project 16
**Trigger:** Play Console account created (founder, 2026-06-12). This spec is the async veto point.

## 1. Scope — three deliverables

### A. Accessibility prominent disclosure (forlater 11 — submission GATE)
Per Play's AccessibilityService policy, non-accessibility-tool apps must show an in-app prominent
disclosure and obtain affirmative consent BEFORE enabling the service. Implementation:
- `PermissionScreen` gains an optional `disclosure` block (used only by the accessibility step): a
  highlighted card stating verbatim-style: Niyam uses Android's **AccessibilityService API** solely to
  detect when an app you chose to block is opened, so the mantra can appear first; it does **not**
  collect, read, store, or transmit screen content or personal data; detection runs entirely on-device.
- The Grant button on that step is replaced by **"I agree — turn on"**: tapping it IS the affirmative
  consent (logged via a persisted `accessibilityConsentAt` pref for audit) and then routes to system
  settings as today. Without the tap, nothing routes.
- Strings EN + 7 locales (translator pass).
- NOT engine: this is the permission-flow UI; `service/` untouched.

### B. Release signing + AAB
- Upload keystore generated locally at `~/keystores/niyam-upload.keystore` (4096-bit RSA, 25-year
  validity). **Never committed**; credentials live in `~/.gradle/gradle.properties`
  (NIYAM_UPLOAD_STORE_PASSWORD etc.); `app/build.gradle.kts` reads them if present (CI-safe fallback
  to unsigned). Play App Signing enrolls this as the upload key — Google holds the real signing key.
- Version: `versionCode 2`, `versionName "1.0.0"`. Build `bundleRelease` AAB.
- Release build keeps minify OFF for v1 (avoid R8 surprises in review; size is acceptable).

### C. Play listing kit (founder's console session = paste-only)
`docs/play-listing-kit.html`: app name/short/full descriptions (EN + HI), category + tags, contact +
privacy URL, **data-safety questionnaire answers** (no collection; AdMob SDK disclosure for free tier),
**content-rating answers**, **AccessibilityService declaration text** (+ note that a short screen-recording
of the disclosure→consent→overlay flow may be requested), subscription products to create
(niyam.premium.weekly/monthly/yearly — ₹15/49/399, 7-day-trial note: app-side), closed-test plan
(12 testers × 14 days; waitlist as the source), and the step-by-step console checklist.
Assets: 512×512 icon + 1024×500 feature graphic generated from brand files; screenshot set picked
from existing galleries.

## 2. Out of scope (next SPs)
Real Play Billing gateway (runs as its own careful SP during the closed test — the test legitimately
starts on the sandbox gateway); AdMob real ad units; Remotion reel; backend.

## 3. Acceptance
1. Suite green + build green; emulator: accessibility step shows disclosure, consent tap routes to
   settings, decline path leaves nothing enabled; consent timestamp persisted; 8 locales render.
2. `bundleRelease` produces a signed AAB; keystore + credentials verifiably outside the repo.
3. Listing kit complete enough that the founder's console session needs zero composition.
