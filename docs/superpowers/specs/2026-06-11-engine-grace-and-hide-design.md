# Niyam — Engine Fixes: Unlock Grace + Hide-on-Leave (SP-9)

**Date:** 2026-06-11 · Sub-project 9
**Status:** Founder-approved ("Good to go", 2026-06-11) — fixes the two engine findings reported from the
live walkthrough and reproduced three times during screenshot sessions.

## 1. Purpose

Two defects in the blocking engine's dismissal behaviour:

1. **Finding 1 — Continue grants only ~2 seconds.** The accessibility service debounces re-triggers for
   `DEBOUNCE_MS = 2_000` only. Any window-state change inside the same blocked app after that (in-app
   navigation, opening a video) re-shows the overlay. In real use the user is re-blocked mid-scroll
   seconds after honouring the mantra.
2. **Finding 2 — the overlay outlives the blocked app.** Nothing hides the overlay when the foreground
   moves to a NON-blocked package. Empirically the overlay persisted over the launcher, other apps, and
   Niyam itself, swallowing all touches until Continue was tapped.

**In plain English:** after you read your mantra and tap Continue, Niyam now leaves that app alone for
5 minutes. And if you change your mind mid-countdown and leave the app, the mantra screen gets out of your
way instead of taking the phone hostage.

## 2. Design

### 2.1 `UnlockGrace` (new, pure, unit-tested)

`service/UnlockGrace.kt` — object holding a per-package grant map (`ConcurrentHashMap<String, Long>`).

- `GRACE_MS = 5 * 60_000L` (5 minutes; single constant, tunable later, founder delegated the number)
- `grant(pkg: String, nowMs: Long)` — records the grant timestamp
- `isActive(pkg: String, nowMs: Long): Boolean` — true iff a grant exists and `nowMs - grantedAt < GRACE_MS`
- `clear()` — test hygiene
- All methods take `nowMs` explicitly (pure logic, no clock inside) — call sites pass
  `SystemClock.elapsedRealtime()` (monotonic, immune to wall-clock changes).

**Grace is granted ONLY by the Continue tap** (a completed read). Hide-on-leave does NOT grant grace —
leaving mid-countdown means the next launch re-blocks (only the existing 2s flicker-debounce applies).

### 2.2 `OverlayHideDecision` (new, pure, unit-tested)

`service/OverlayHideDecision.kt` — single pure function deciding whether a foreground change should hide
a showing overlay:

```
shouldHide(overlayShowing, foregroundPkg, foregroundClass, isBlocked, ownPkg): Boolean
```

Rules, in order:
- overlay not showing → `false`
- foreground package is blocked → `false` (still inside a blocked app)
- `com.android.systemui` → `false` (notification shade / status bar must not dismiss)
- our own package → `true` ONLY if `foregroundClass == "com.myniyam.app.MainActivity"`
  (the overlay window itself reports our package when it attaches — that event must not self-dismiss;
  the real app opening should)
- anything else (launcher, another app) → `true`

### 2.3 Wiring (the only edits to existing engine files)

`AppLockAccessibilityService.onAccessibilityEvent` becomes:

1. non-`TYPE_WINDOW_STATE_CHANGED` → return (unchanged)
2. null package → return (unchanged)
3. **NOT blocked** → if `OverlayManager.isShowing()` and `OverlayHideDecision.shouldHide(...)` →
   `OverlayManager.hide(applicationContext)`; return
4. blocked → **if `UnlockGrace.isActive(pkg, now)` → return** (new gate, before the debounce)
5. existing 2s dismissal debounce (unchanged)
6. start foreground service → show overlay (unchanged)

`OverlayManager`:
- new `fun isShowing(): Boolean = overlayView != null`
- Continue click listener gains one line: `UnlockGrace.grant(pkg, SystemClock.elapsedRealtime())`
  (uses the captured `pkg` param of `show`) before `hide(ctx)`
- `hide()` unchanged — it still marks the 2s dismissal debounce; harmless for the leave path,
  prevents flicker on re-entry.

Everything else — WindowManager params, 15s timer, recordRead flow, foreground service — untouched.

### 2.4 Accepted edge cases (deliberate)

- Switching from blocked app A directly to blocked app B while the overlay shows: overlay stays up with
  A's attribution. Same as today; rare; not worth complexity now.
- An IME window event while the overlay shows would count as "non-blocked foreground" → hide. The overlay
  is full-screen, touch-consuming and non-focusable, so a keyboard appearing under it is not a real flow;
  emulator verification covers the normal paths (home press, app switch, Niyam open).

## 3. Scope

**In:** `service/UnlockGrace.kt` (new), `service/OverlayHideDecision.kt` (new), surgical edits to
`AppLockAccessibilityService.kt` and `OverlayManager.kt`, unit tests for both new classes.

**Out:** any visual change; BlockList; ProgressRepository; the 15s timer; SP-6 paywall; making the grace
duration user-configurable.

## 4. Acceptance

1. New unit tests green (grace window boundaries, per-package independence, re-grant reset; all
   `shouldHide` branches); full suite green; `assembleDebug` green.
2. Emulator, live: blocked app → overlay → press HOME mid-countdown → **overlay disappears**; relaunch
   blocked app → overlay re-appears (no grace from leaving).
3. Emulator, live: overlay → wait 15s → Continue → navigate inside the app → **no re-block**; HOME →
   relaunch the same app within the window → **no overlay** (grace active).
4. Diff over the range shows only the four files above (+ tests); OverlayManager delta is the
   `isShowing` accessor + one grant line.
