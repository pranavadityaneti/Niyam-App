# SP-9 Acceptance Report — Engine Fixes: Unlock Grace + Hide-on-Leave

**Date:** 2026-06-11
**Spec:** `docs/superpowers/specs/2026-06-11-engine-grace-and-hide-design.md` (founder-approved "Good to go")
**Commits:** `171955b` (spec) · `d315706` (UnlockGrace + OverlayHideDecision, TDD, 15 tests) · `c15f07c` (service + OverlayManager wiring) · `bf5f478` (dead Playfair/Inter font deletion, separate approved change)

## Spec §4 acceptance criteria

| # | Criterion | Result |
|---|-----------|--------|
| 1 | New unit tests green; full suite green; assembleDebug green | ✅ 15 new tests (7 grace incl. exclusive boundary + per-package + re-grant; 8 hide-decision branches); **suite 98/98** from XMLs (`--rerun-tasks`) after wiring AND after font deletion; build green |
| 2 | Live: overlay → HOME mid-countdown → overlay disappears; relaunch → re-blocks | ✅ Pixel 9 emulator: YouTube → overlay (ring 10) → HOME → launcher fully clean → relaunch YouTube → overlay re-appears (leaving earns no grace) |
| 3 | Live: Continue → in-app navigation free; relaunch within window → no overlay | ✅ Continue → YouTube feed usable → opened video + back (the exact gesture that re-blocked in 2s pre-fix) → no overlay → HOME → relaunch YouTube → no overlay (grace active) |
| 4 | Diff shows only the four files + tests; OverlayManager delta minimal | ✅ Engine range touches exactly: 2 new pure classes, 2 test files, AppLockAccessibilityService (non-blocked branch + grace gate), OverlayManager (isShowing + one grant line in the Continue listener). Timer/params/show/hide guards untouched |

## Scope notes

- **Grace expiry at exactly 5:00** verified at unit level only (`nowMs - grantedAt < GRACE_MS`, exclusive boundary test) — not re-verified with a 5-minute wall-clock wait on the emulator.
- Grace is granted in exactly one place (Continue listener). Hide-on-leave keeps only the pre-existing 2s flicker debounce.
- Accepted edges per spec §2.4: blocked-app→blocked-app switch keeps the stale overlay; IME-window events count as "left the app."
- Independent hostile Opus review of the full range dispatched (running at report time); verdict appended to SESSION_LOG when in.

## In plain English

Before: reading your mantra bought you 2 seconds — then Niyam blocked you again mid-scroll; and if you changed your mind and left, the mantra screen stayed stuck over your whole phone. Now: a completed read means that app stays open for 5 minutes, and walking away simply dismisses the screen. Blocking behaviour for fresh launches is unchanged — verified live.
