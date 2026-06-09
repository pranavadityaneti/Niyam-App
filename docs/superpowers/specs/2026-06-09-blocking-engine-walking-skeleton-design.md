# Niyam — Blocking Engine Walking Skeleton Design

**Date:** 2026-06-09
**Product:** Niyam (नियम) — www.myniyam.com
**Sub-project:** 1 of 7 — Blocking Engine Walking Skeleton
**Platform:** Android (native Kotlin)
**Status:** Design approved by Pranav 2026-06-09 — moving to implementation plan

---

## Table of Contents

1. [Purpose](#purpose)
2. [Scope and non-goals](#scope-and-non-goals)
3. [User-visible behavior](#user-visible-behavior)
4. [Architecture overview](#architecture-overview)
5. [Component design](#component-design)
6. [Permission flow](#permission-flow)
7. [OEM autostart handling](#oem-autostart-handling)
8. [Data and state](#data-and-state)
9. [Error modes and recovery](#error-modes-and-recovery)
10. [Test approach](#test-approach)
11. [Acceptance criteria](#acceptance-criteria)
12. [Known risks](#known-risks)
13. [Decisions deferred](#decisions-deferred)
14. [Open dependencies on Pranav](#open-dependencies-on-pranav)

---

## Purpose

Prove the core mechanic of Niyam on a real Indian Android phone: when the user opens a blocked app (Instagram), our app intercepts the launch and displays a mantra overlay with a 15-second timer before allowing access.

This is the de-risk sub-project. If this skeleton does not work on at least one MIUI device and one OneUI device, the product as designed cannot ship and we re-scope. If it does, every downstream sub-project (content library, onboarding, paywall, sadhana progression) attaches to a proven mechanism.

**In plain English:** the magic trick is "Instagram opens → mantra appears first." This document is the design for the bare-bones version of that trick. Once it works on real budget Android phones, we build the rest of the product around it.

---

## Scope and non-goals

### In scope

- Native Kotlin Android app, single Gradle module, debug-signed APK (sideload only).
- Detect when the user opens any of three hardcoded blocked apps: **Instagram** (`com.instagram.android`), **Facebook** (`com.facebook.katana`, main app only — Messenger and Lite are separate packages, not blocked), **YouTube** (`com.google.android.youtube`, main app only — YouTube Music is a separate package).
- Render a custom overlay on top of the blocked app showing a single hardcoded mantra (`ॐ`), an English meaning, a 15-second countdown timer, and an unlock button that becomes active when the timer completes.
- Walk the user through the five OS permissions required to make this work.
- Detect the device manufacturer and surface a per-OEM autostart / battery-optimization walkthrough.
- A home screen showing permission status (green/red per permission, re-grant buttons) and an overall "Protection: Active / At Risk" indicator.

### Explicit non-goals

- User-pickable block list. The three blocked apps are hardcoded; no settings UI for adding/removing apps. (Multi-app blocking *of these three* is in scope; *user-configurable* block list is sub-project 7.)
- Real content library or any mantra other than the hardcoded `ॐ`.
- Light/dark theme, brand colors, custom fonts. Bare system defaults until UI references arrive.
- Onboarding intention picker, mantra picker, language picker.
- Streak counter, sadhana progress tracking, completion celebration.
- Paywall, RevenueCat, AdMob, free-trial logic.
- Push notifications.
- Settings beyond the permission-status home screen.
- Play Store upload, release signing, ProGuard rules.
- iOS.

---

## User-visible behavior

### First-run flow

1. User installs APK via sideload, taps the app icon.
2. Welcome screen — single button, "Get started."
3. Permission screen 1 — "Usage access" — plain-language "why" copy, "Grant" button deep-links to Settings → Usage access → user toggles our app on → returns. We detect the grant and auto-advance.
4. Permission screens 2-5 — same pattern for: Display over other apps (`SYSTEM_ALERT_WINDOW`), AccessibilityService, Ignore battery optimization, OEM autostart.
5. Home screen — shows all five permissions green, plus a "Protection: Active" banner.

### Steady-state behavior

1. User locks phone, puts app in background.
2. Some time later, user opens one of the blocked apps (Instagram, Facebook, or YouTube) from the launcher.
3. Within ~200ms of the blocked app coming to the foreground, our overlay covers the screen with:
   - Devanagari character `ॐ` (large, centered)
   - Transliteration: "Om"
   - English meaning: "The primordial sound of the universe."
   - A countdown: "Unlocking in 15…14…13…" → "0"
   - At 0, a button appears: "Continue"
4. User taps "Continue" → overlay dismisses → the blocked app is visible underneath.
5. If user presses Home or Back while the overlay is up, the overlay **persists** — `TYPE_APPLICATION_OVERLAY` draws on top of the launcher and other apps too. The only way out is to wait the timer and tap "Continue." (For the skeleton, we accept this; in a later sub-project we can add "user navigated to a non-blocked app → auto-dismiss overlay".)

### Failure-state behavior

- If any required permission is revoked (e.g., user goes to Settings and disables our AccessibilityService), the home screen banner switches to "Protection: At Risk" with a list of which permissions need re-granting. Instagram launches in this state will NOT be intercepted — by design, since the mechanism literally cannot work without the permission.

---

## Architecture overview

```
┌─────────────────────────────────────────────────────────────┐
│                       Android OS                            │
│                                                             │
│  ┌─────────────────────┐   foreground   ┌────────────────┐  │
│  │ AppLockAccessibility│  app changes   │ ForegroundSvc  │  │
│  │ Service             │ ───────────────▶│ (notification) │  │
│  │  (Android Acc API)  │   intent       │                │  │
│  └─────────────────────┘                │  ┌──────────┐  │  │
│                                         │  │ Overlay  │  │  │
│                                         │  │ Manager  │  │  │
│                                         │  │          │  │  │
│                                         │  │ Window-  │  │  │
│                                         │  │ Manager  │  │  │
│                                         │  │ + View   │  │  │
│                                         │  └──────────┘  │  │
│                                         └────────────────┘  │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Compose UI Activity (in-app screens only)          │    │
│  │  ┌─────────┐ ┌────────────┐ ┌────────────────────┐  │    │
│  │  │ Welcome │ │ Permission │ │ Home / status      │  │    │
│  │  └─────────┘ │ screens 1-5│ └────────────────────┘  │    │
│  │              └────────────┘                          │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

Three runtime pieces:

1. **`AppLockAccessibilityService`** — Android `AccessibilityService` subclass. Listens for `TYPE_WINDOW_STATE_CHANGED` events, filters by package name, and dispatches an intent to the foreground service when Instagram comes to the front.
2. **`AppLockForegroundService`** — Android `Service` with `startForeground(...)`. Owns the overlay lifecycle and the timer state. Receives intents from the accessibility service and instructs the OverlayManager to show/hide.
3. **`OverlayManager`** — wraps `WindowManager` operations. Inflates the overlay layout, applies `TYPE_APPLICATION_OVERLAY` window flags, runs the 15-second timer, and removes the view on unlock.

The in-app Compose UI lives in a single `MainActivity` with a NavHost that walks the user through Welcome → Permissions → Home.

---

## Component design

### `AppLockAccessibilityService`

- Extends `AccessibilityService`.
- `serviceInfo` configured (via XML resource) to receive `TYPE_WINDOW_STATE_CHANGED`, `feedbackType = FEEDBACK_GENERIC`, no package filter at the service level (we filter in code so the block-list is easy to change later).
- On each event:
  ```kotlin
  override fun onAccessibilityEvent(event: AccessibilityEvent) {
      val pkg = event.packageName?.toString() ?: return
      if (pkg in BlockList.HARDCODED_PACKAGES) {
          startService(Intent(this, AppLockForegroundService::class.java).apply {
              action = AppLockForegroundService.ACTION_BLOCKED_APP_FOREGROUND
              putExtra(AppLockForegroundService.EXTRA_PACKAGE, pkg)
          })
      }
  }
  ```
- `BlockList.HARDCODED_PACKAGES = setOf("com.instagram.android", "com.facebook.katana", "com.google.android.youtube")` for the skeleton.
- Debounce: ignore events from a blocked package within 2 seconds of the last overlay dismissal for the same package (prevents re-trigger loop after user taps unlock).

### `AppLockForegroundService`

- Extends `Service`.
- `onStartCommand` handles two actions:
  - `ACTION_START` — called from `MainActivity` once all permissions are granted. Calls `startForeground(NOTIF_ID, buildNotification())`.
  - `ACTION_BLOCKED_APP_FOREGROUND` — triggers `OverlayManager.show(pkg)`.
- Notification channel: `LOW` importance, no sound, persistent. Text: `"Standing guard against doom-scroll"` (placeholder copy — replace when UI references arrive).
- `onDestroy` calls `OverlayManager.hide()`.

### `OverlayManager`

- Holds a single `View?` reference for the active overlay.
- `show(packageName)`:
  - If a view is already attached, no-op (prevents double-overlay).
  - Inflate `R.layout.overlay_mantra` (traditional View XML).
  - Build `WindowManager.LayoutParams` with `TYPE_APPLICATION_OVERLAY`, `FLAG_NOT_FOCUSABLE | FLAG_LAYOUT_NO_LIMITS`, `MATCH_PARENT` width and height.
  - Add view to `WindowManager`.
  - Start a 15-second `CountDownTimer`; update the countdown text every second.
  - Disable the "Continue" button (`isEnabled = false`, dimmed) until the timer completes.
- `hide()`:
  - Cancel the timer.
  - Remove the view from `WindowManager`.
  - Null out the reference.
- "Continue to Instagram" button click handler calls `hide()`.

### Layout: `overlay_mantra.xml`

Bare system defaults until UI references arrive:

- Solid background (semi-transparent black `#CC000000`) over full screen.
- `TextView` for `ॐ` — 96sp, centered horizontally, top third of screen.
- `TextView` for "Om" transliteration — 24sp, below.
- `TextView` for English meaning — 16sp, below, max-width 80% screen.
- `TextView` countdown — 20sp, below, monospace.
- `Button` "Continue" — bottom, full-width with margin, default Material button style.

This is engineering placeholder, NOT a design decision. Visual pass happens when Pranav provides references.

### In-app Compose UI (`MainActivity`)

- `MainActivity` hosts a `NavHost` with destinations:
  - `welcome`
  - `permission_usage_stats`
  - `permission_overlay`
  - `permission_accessibility`
  - `permission_battery`
  - `permission_oem_autostart`
  - `home`
- Each permission screen is a `@Composable` showing:
  - Plain-language "why we need this" copy (placeholder text — final copy with UI pass).
  - "Grant" button → launches the relevant `Intent` to Settings.
  - On resume, the screen calls a `PermissionChecker` to detect grant, and on success auto-navigates to the next screen.
- `home` shows a list of permissions with green check / red X, and a `Status: Active | At Risk` banner.

### `PermissionChecker` (single file, ~150 lines)

Static methods, one per permission:

- `hasUsageStatsAccess(ctx: Context): Boolean`
- `hasOverlayPermission(ctx: Context): Boolean`
- `isAccessibilityServiceEnabled(ctx: Context): Boolean`
- `isIgnoringBatteryOptimizations(ctx: Context): Boolean`
- `isOemAutostartHandled(ctx: Context): Boolean` — see OEM section; this one is opportunistic.

Plus a method to launch each Settings page:

- `openUsageAccessSettings(ctx: Context)`
- `openOverlayPermissionSettings(ctx: Context)`
- `openAccessibilitySettings(ctx: Context)`
- `openIgnoreBatteryOptimizationSettings(ctx: Context)`
- `openOemAutostartSettings(ctx: Context)` — uses manufacturer-specific intents.

---

## Permission flow

Five sequential screens. Each shows a "why" sentence, a "Grant" button, and a "Skip for now" link that warns "Skipping will prevent the app from working."

Order matters — the AccessibilityService can't be enabled meaningfully until the others are set up.

| # | Permission | Android API surface | Why we need it (user-facing copy) |
|---|---|---|---|
| 1 | Usage access | `AppOpsManager.MODE_ALLOWED` on `OPSTR_GET_USAGE_STATS` | "To see when you open Instagram, Facebook, or YouTube — so we can show your mantra first." |
| 2 | Display over other apps | `Settings.canDrawOverlays(ctx)` | "To show your mantra on top of the blocked app." |
| 3 | Accessibility service | check via enabled-services string | "To detect the exact moment Instagram, Facebook, or YouTube opens — instantly, with no delay." |
| 4 | Ignore battery optimization | `isIgnoringBatteryOptimizations` | "So your phone doesn't shut us off in the background." |
| 5 | OEM autostart | manufacturer-specific (opportunistic check) | "So your phone allows us to start when needed." |

For each: tapping "Grant" launches an Intent to the relevant Settings page. When the user returns to our app, `MainActivity.onResume` re-checks the permission and advances if granted.

---

## OEM autostart handling

Background-process killing is the single biggest reason apps in this category fail in the Indian market. Different OEMs have different settings UIs; there is no Android-standard API to handle this.

### Detection

`Build.MANUFACTURER` (lowercased). Routes to one of:

- `xiaomi`, `redmi`, `poco` → MIUI flow
- `oppo`, `realme` → ColorOS flow
- `vivo`, `iqoo` → FuntouchOS / OriginOS flow
- `oneplus` → OxygenOS flow
- `samsung` → OneUI flow
- anything else → generic flow ("Please ensure background activity is allowed for this app in your settings.")

### Walkthrough screen

For each known OEM, show:

- Heading: "Your phone is `<OEM brand>`. We need one more setting."
- Numbered steps (text only — screenshots come in a later pass when we have UI direction).
- A "Take me there" button → manufacturer-specific intent that deep-links to the closest Settings page we can reach. For MIUI, this is the `com.miui.securitycenter` "AutoStart" activity. For ColorOS, the "StartupManager" activity. Each manufacturer's intent is wrapped in a try/catch — if the activity doesn't exist (different ROM version), we fall back to the generic "App info" page for our app.
- A "Done" button that lets the user mark this step complete.

### Why opportunistic, not blocking

We cannot programmatically detect whether the OEM-specific autostart has actually been granted — there's no standard API. Forcing the user to "complete" the step would either gate them out forever (bad) or accept their "Done" tap as gospel (no different from opportunistic).

So: the user taps "Done" to advance past the screen. The home screen banner shows "Protection may be at risk on your device" if `Build.MANUFACTURER` is a known killer-OEM AND we ever observe our service being killed (heuristic: a flag stored when `onTaskRemoved` is called on the foreground service, cleared when the service restarts within a heartbeat window).

**In plain English:** there's no way to programmatically check that the user actually flipped the autostart switch on a Xiaomi phone. We trust them, and we show a persistent warning if we later notice we've been killed.

### Library choice

Use [`xxpermissions`](https://github.com/getActivity/XXPermissions) or [`AutoStarter`](https://github.com/judemanutd/AutoStarter) for the per-OEM intents. Both are MIT-licensed Kotlin/Java libraries with active maintenance. Pick `AutoStarter` for the skeleton — narrower scope, single-purpose. Re-evaluate if it stops being maintained.

---

## Data and state

The walking skeleton stores almost nothing.

- No user accounts, no remote sync.
- A small `SharedPreferences` file (`app_lock_prefs`) for:
  - `onboarding_complete: Boolean` — set true when the user reaches the home screen.
  - `oem_warning_shown: Boolean` — true once user has seen the OEM walkthrough at least once.
  - `service_kill_observed: Boolean` — heuristic from `onTaskRemoved`; clears on next successful start.

No database. No content store (skeleton's mantra is a string constant in `BuildConstants.kt`). The real content library lands in sub-project 2 with a Room database + JSON seed.

---

## Error modes and recovery

| Error mode | What happens | Recovery |
|---|---|---|
| Permission revoked at runtime (e.g., user disables AccessibilityService) | Detection stops. Instagram launches unintercepted. | Home screen banner switches to "Protection: At Risk." Each permission row shows red X with a re-grant button. |
| Foreground service killed by OEM ROM | Service dies; overlay never appears. | On next `MainActivity.onResume`, we restart the service. We also set `service_kill_observed = true` and surface the persistent warning. |
| Overlay fails to render (WindowManager throws) | Caught in try/catch. We log to logcat and let Instagram open normally. | No retry — failing silently is better UX than crash-looping. The bug will be visible to us during testing. |
| User taps back/home while overlay is up | Overlay is `FLAG_NOT_FOCUSABLE` and `TYPE_APPLICATION_OVERLAY`, so back goes to the blocked app (which handles it normally), home goes to launcher — but the overlay **persists on top** in either case. User has to wait the timer and tap "Continue" to dismiss. | None needed at skeleton scope. Auto-dismiss on navigation-away is a future enhancement. |
| AccessibilityService fails to start after enable | Rare, but happens on some ROMs. | On home screen `onResume`, we check `isAccessibilityServiceEnabled` AND test that our service instance is actually alive (using a static flag set in `onServiceConnected`). If enabled but not alive, banner says "Restart phone to activate." |

---

## Test approach

### Two-phase testing approach

Pranav has confirmed no physical Android device on hand. The walking skeleton therefore ships against **Phase 1 (emulator-only)** acceptance, with **Phase 2 (real OEM verification)** explicitly deferred until a test device is acquired.

**Phase 1 — Pixel emulator (current build target).**

- Android Studio AVD running a stock AOSP system image (Pixel 6 / API 34 recommended).
- Validates: detection mechanism, overlay rendering, permission-grant flow, timer correctness, foreground-service lifecycle.
- Does **NOT** validate: surviving real OEM battery-killers. Emulators run AOSP, not MIUI / ColorOS / OneUI; the killer behavior lives in those vendor OS layers and is not reproducible on emulator.

**Phase 2 — real OEM devices (deferred, gating any product launch decision).**

- At minimum: one Xiaomi/Redmi (MIUI) and one Samsung (OneUI) device.
- Stretch: one Oppo/Realme (ColorOS).
- Acquiring a budget Android device (~₹6-8k for a Redmi or Realme) is a real open dependency. Until Phase 2 runs, we cannot claim the engine works in market.

### Automated tests in the skeleton

Minimal. The pieces that benefit from unit tests:

- `BlockList.matches(packageName)` — trivial but cheap to test.
- `PermissionChecker` static methods — these wrap `AppOpsManager` / Settings — best tested with Robolectric or instrumentation tests, not pure JVM. Skip for the skeleton; add later when the checker grows.
- `OverlayManager` — UI-level, requires a running Android instance. Instrumented test is overkill for the skeleton.

The skeleton's correctness is validated by manual on-device testing, not test code. Test code lands in sub-project 2+ when business logic gets complex.

### Acceptance criteria for "skeleton complete"

See next section.

---

## Acceptance criteria

Split into two phases because the test environment changed (no physical device on hand).

### Phase 1 — emulator-only (current build target)

The skeleton ships Phase 1 when ALL of these are true on a Pixel emulator (Pixel 6 / API 34, stock AOSP system image):

1. APK installs and launches.
2. Welcome → 5 permission screens → home — completable end to end with green checks on every permission.
3. Phone goes to launcher. User opens **Instagram** — overlay covers the screen within ~200ms.
4. User opens **Facebook** — overlay covers the screen within ~200ms.
5. User opens **YouTube** — overlay covers the screen within ~200ms.
6. Overlay displays `ॐ`, "Om", "The primordial sound of the universe.", and a countdown starting at "15".
7. Countdown decrements each second.
8. "Continue" button is disabled until the countdown reaches 0.
9. At 0, the button enables.
10. Tapping the button dismisses the overlay; the blocked app is visible underneath.
11. Re-opening the same blocked app triggers the overlay again (after the 2-second debounce).
12. Closing the app via Recents does NOT prevent steps 3-11 (foreground service survives swipe-from-recents).
13. Revoking the AccessibilityService permission causes blocked-app launches to NOT be intercepted, and the home screen shows "Protection: At Risk" on next open.
14. Opening a NON-blocked app (e.g. Chrome, Gmail) does NOT trigger the overlay.

Phase 1 passing means: "the engine works on stock Android." It does NOT mean: "the engine works in market."

### Phase 2 — real OEM verification (deferred, gating product launch)

Phase 2 acceptance — does not block skeleton sign-off, but DOES block any decision to move to paid launch:

1. All Phase 1 criteria hold on at least one MIUI phone (Xiaomi/Redmi/Poco).
2. All Phase 1 criteria hold on at least one OneUI phone (Samsung).
3. On both, the foreground service survives at least 30 minutes of normal phone use including screen lock cycles. If it gets killed, the OEM autostart walkthrough was completed.
4. (Stretch) Same on one ColorOS phone (Oppo/Realme).

Phase 2 gates the call: "is the product mechanism real for the Indian market." If Phase 2 fails on MIUI specifically — that is information about the product, not a bug to power through. We discuss what to do.

---

## Known risks

1. **Google Play AccessibilityService policy review** — when we eventually upload to Play in sub-project 6, we will need to declare AccessibilityService use and justify it. Distraction-blocking is on Google's allowed-use list per my last reading, but I cannot verify the current state of policy without checking live docs at upload time. Mitigation: skeleton is sideload-only, so no review risk at this stage. Verify policy before Play upload.
2. **MIUI may kill the foreground service anyway** — even with all permissions granted and autostart enabled, some MIUI versions still kill foreground services aggressively. There's no fix from the app side — this is a vendor-OS bug. Mitigation: warn user clearly. If MIUI kills are universal, we may need to advise users to also enable "Don't restrict background activity" in MIUI's separate Battery saver settings.
3. **AccessibilityService scariness** — enabling AccessibilityService triggers a system warning ("This app can read everything on your screen…"). Some users will bail at this step. Mitigation: a clear "why we need this and what we actually do with it" screen BEFORE we send them to Settings.
4. **Hardcoded `ॐ` is intentional but feels minimal** — Pranav may want to see a real mantra in the skeleton. Acceptable to swap to Gayatri Mantra or similar from the 25-entry list in the original brief if you'd prefer.

---

## Decisions deferred

These are real product decisions but explicitly NOT part of the skeleton:

- Multi-app block-list management (UI for picking blocked apps)
- Real content store + Sanskrit + 9-script transliterations
- Theming, brand colors, custom fonts, Devanagari font (Noto Serif Devanagari)
- Language picker
- Onboarding intention flow
- Sadhana progression, streak counter, completion celebration
- Paywall, RevenueCat, AdMob
- Push notifications
- iOS port

Each owns its own sub-project and its own design spec.

---

## Open dependencies on Pranav

1. **UI references** — needed before any visual pass on the overlay and the in-app screens. Skeleton ships with bare system defaults; that is a stand-in, not a design.
2. **Real Android device — deferred but real.** Pranav confirmed no Android device on hand. Skeleton ships against Phase 1 (emulator). Phase 2 (real OEM) gates any product launch decision — see [Acceptance criteria](#acceptance-criteria). A budget Redmi or Realme at ~₹6-8k is the minimum we need before that gate can be cleared.
3. **Git repository.** Pranav is creating a git project and will share the link. The local working directory is currently *not* a git repo; spec is saved as files only. When the link arrives, we initialize the local repo, set the remote, and push the spec as the first commit.
4. **Review of this spec** — already received once; current revision is incorporating feedback (Facebook + YouTube added, emulator-only acceptance phased).
