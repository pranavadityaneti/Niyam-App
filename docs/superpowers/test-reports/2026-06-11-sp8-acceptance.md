# SP-8 Acceptance Report — Visual Identity v2 ("Sunrise Sans")

**Date:** 2026-06-11
**Spec:** `docs/superpowers/specs/2026-06-11-ui-v2-design.md` (transcribed from the founder's interactive mockup approval session)
**Plan:** `docs/superpowers/plans/2026-06-11-niyam-ui-v2.md`
**Commits:** `734ee99` (spec + Outfit font) · `3a716eb` (plan) · `0791a39` (theme foundation) · `703c606` (onboarding/welcome/permissions) · `574d0e0` (home/celebration/nav) · `770afbe` (library/detail/settings) · `b0827e4` (overlay restyle) · `ae22d6e` (reads-today plural fix)

## Spec §4 acceptance criteria

| # | Criterion | Result |
|---|-----------|--------|
| 1 | 83 tests green; assembleDebug green | ✅ 83/83 from XMLs (`--rerun-tasks`) re-verified at HEAD after every task and after the plural fix; build green throughout |
| 2 | Engine logic untouched; overlay diff visual-only | ✅ Range diff has zero service/BlockList/ProgressRepository/data changes. Full OverlayManager diff inspected by the main agent: render-target swap only (countdown TextView → RingCountdownView), identical show/hide guards, WindowManager params, 15s/1000ms timer cadence, Continue→recordRead→hide flow, markDismissed debounce |
| 3 | Emulator core-loop re-check after overlay restyle | ✅ Performed live (Pixel 9, fresh install): YouTube launch → v2 overlay appears (gradient, Outfit, ring) → ring counts 15→0 → Continue enables → tap dismisses → read recorded (Home: Day 1 of 14, 1-day streak, 1 read today, animated fill) |
| 4 | v2 screenshot set committed mirroring approved mockups | ✅ 16 screens in `docs/screenshots/v2/` + self-contained gallery `docs/screenshots/v2/index.html` for the founder's screen-by-screen comparison |
| 5 | No Playfair/Inter usage; Indic fallback renders | ✅ Code references removed (review-verified); Devanagari renders via system Noto on the emulator (overlay + detail verified visually). Font FILES still in `res/font/` (~1.2MB dead weight) — removal queued as follow-up pending review confirmation |

## Live findings during emulator verification (fixed or queued)

- **"1 reads today"** on Home → fixed in `ae22d6e` (`home_today_plural`), same treatment as the SP-5 dialog plural.
- **Launcher icon is still the Android default robot** (visible on the cold-start splash). Queued — needs a real Niyam icon (forlater).
- Slight gradient banding on the emulator's software renderer — expected swiftshader artifact; check on the real device during Phase 2.

## Founder-approval trail

Direction (sunrise gradients + floating cards + serif heroes) → composition iterations (centered → upper-third → "immediately below the top bar" with protected CTA zones) → full 16-frame set approved ("Good to go") → Om option explored (bold/brush) and **dropped** ("let's not use Om anywhere") → typography finalized as Outfit ("Google Sans" requested; proprietary — Outfit disclosed and accepted as the licensed equivalent).

## Verdict

**SP-8 code-complete and emulator-verified.** Combined Opus review of the full range dispatched; any findings will be fixed as follow-up commits. The app now looks like the approved mockups on every screen, light and dark, including the unlock overlay.
