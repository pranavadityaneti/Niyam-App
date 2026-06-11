# SP-7 Acceptance Report — Settings, Completion Notification & Dark Mode

**Date:** 2026-06-11
**Spec:** `docs/superpowers/specs/2026-06-11-settings-design.md`
**Plan:** `docs/superpowers/plans/2026-06-11-niyam-settings.md`
**Commits:** `741e49b` (prefs) · `188faad` (dark theme + ThemeState) · `7052a58` (CompletionNotifier + hook) · `43012b6` (SettingsScreen) · `9756008` (editor screens) · `d003ddb` (routes + gear) · `03ac51b` (theme-aware brand tokens fix)

## Spec §6 acceptance criteria

| # | Criterion | Result |
|---|-----------|--------|
| 1 | Unit tests: themePref/notify round-trips, notifier guard; suite grows from 76, all green | ✅ Suite 76 → **83/83** (UserPrefs +4 incl. unknown-raw→LIGHT fallback; CompletionNotifier shouldPost +3). Final tally re-run independently at HEAD: tests=83 failures=0 errors=0 |
| 2 | Build green; zero engine-file changes | ✅ `assembleDebug` green at every task; `git diff f210c40..HEAD` has zero hits on service/, OverlayManager, BlockList, overlay XML. Sole non-UI/non-prefs touch: the try/caught notifier hook in ProgressRepository (explicitly permitted by spec §4) |
| 3 | Emulator walkthrough: language change re-scripts overlay; dark toggle re-themes live; un-blocked app opens freely; completion notification → tap → Celebration | ⏳ **Deferred** — rides the standing manual emulator session |
| 4 | Visual brand match incl. dark palette (async eyeball) | ⏳ Pending Pranav — dark tokens byte-exact to spec table; light mode byte-identical to pre-SP7 (verified in review + the token-accessor fix preserved the exact light Color instances) |

## Reviews (two parallel Opus reviewers)

- **T1-T3 data/infra:** ✅ pass. Highlights independently verified: no seed-vs-first-composition race (MainActivity seeds ThemeState synchronously before setContent); notification fires exactly once per completion (inside the once-only completedMantraIds guard, markCompleted before notify); triple-guarded so a notification failure can never break a recorded read; thread-safety of the off-main hook confirmed (volatile snapshot reads, NotificationManagerCompat off-main safe); minSdk 26 ≥ channel API — no crash paths.
- **T4-T6 UI:** ✅ pass with two flags, both fixed in `03ac51b`:
  1. Appearance segment labels were left-aligned in their pills → centered.
  2. **Real defect:** screens built before dark mode (LibraryScreen FilterChips/markers, MantraDetailScreen InfoChips, onboarding's shared `SelectableCard` — visible in dark mode via the settings editors) hardcoded light-only tokens (`OrangeTint`, `ChipFill`, `InkMuted`) and would have rendered cream-colored on the dark background. Fixed with a `NiyamExtraColors` CompositionLocal (`LocalNiyamColors`) provided by `NiyamTheme` from its existing dark flag; all consumers now resolve theme-aware, including the FilterChip selected-label (BottleGreen in light → DarkInk in dark, which would otherwise be near-invisible on the dark orange wash). Light mode unchanged — the light instance holds the identical `Color` objects.
- Noted, not changed: selected appearance-segment label is orange `#FF6400` on the orange wash (~2.4:1 light / ~3.6:1 dark) — within the CTA carve-out per brand law, flagged for the eyeball veto.

## Deferred by design

Dark overlay variant → queued as `forlater.md` item 5 (engine-adjacent; SP-7 held the zero-engine-touch line).

## Verdict

**SP-7 code-complete.** Criteria 1-2 pass; 3-4 are manual and queued for the standing emulator/eyeball session. The app now has its full free-tier surface: blocking engine, 26-mantra content, onboarding, home/progress/streak, library/switching, settings/notification/dark mode.
