# SP-4 Acceptance Report — Home & Sadhana Progress

- **Date:** 2026-06-10
- **Commit range:** `0f4da3f..b734bf6` (plan + 9 implementation/test commits)
- **Final whole-implementation review:** code-complete = YES, no Critical/Important issues; two optional test gaps it flagged were closed in `b734bf6`.

## What SP-4 shipped

Room database (`read_events`) + `ProgressRepository` (fire-and-forget read recording from the overlay Continue tap; completion detection) + pure `ProgressMath` (streak/dayN/isComplete, fully unit-tested). UserPrefs gained intention / sadhana-start / completed-list / pendingCelebration (with `setCurrentMantra` re-stamping journeys atomically). Onboarding persists intention. Home rebuilt to brand: mantra card with orange Day-N-of-14 progress bar, streak + today chips, compact protection row. Celebration screen → next-sadhana picker (curated, excludes completed/current, backfills) wired via the persisted `pendingCelebration` flag.

## Acceptance scorecard (spec §6)

| # | Criterion | Result | Evidence |
|---|---|---|---|
| 1 | Unit tests (ProgressMath, UserPrefs, picker exclusion); suite grows from 55, all green | **PASS** | **69/69** (ProgressMath 7, UserPrefs 7, OnboardingViewModel 5, NextSadhana 3 + 47 pre-existing). Counted from JUnit XMLs after `--rerun-tasks`. |
| 2 | Build green; Room/KSP compiles; no new permissions | **PASS** | `assembleDebug` SUCCESSFUL; `ReadEventDao_Impl`/`NiyamDatabase_Impl` generated; AndroidManifest unchanged. |
| 3 | Emulator: Continue tap → 1 read today, Day 1 of 14, streak 1 on Home | **OPEN** | Manual — rides the standing emulator session with the SP-2/SP-3 open criteria. |
| 4 | Completion path testable | **PASS (unit) / OPEN (emulator spot-check)** | `ProgressMath.isComplete` boundary tested at 13/14/15 vs threshold 14; `maybeComplete` guards (current-only, once-only, threshold from catalog, start-bounded) verified by the final review. Celebration→picker→switch flow spot-check is manual. |
| 5 | Engine regression: pre-existing tests green; overlay timer/unlock untouched apart from recordRead | **PASS** | OverlayManager diff = 1 import + 2-line listener (final review verified); 69/69 incl. all pre-existing suites. |
| 6 | Visual brand match | **OPEN** | Pranav's async eyeball. |

## Notes from the final review

- The engine touch is provably non-blocking: recordRead enqueues on a single-thread executor and returns; all DB I/O + completion logic is try/caught off the main thread.
- "Keep going" with a completed mantra restarts the journey but never re-celebrates (completed-set guard) — reviewed and accepted as deliberate v1 product behavior.
- Session-interruption recovery: Tasks 4-9 committed during an interrupted stretch were re-reviewed in one integrated pass; Task 10 was recovered from the working tree, verified, and committed (`a1b78bc`).

## Open items (riding the standing manual session)

Criteria 3, 4-spot-check, 6 — plus the SP-2 (Gayatri render, fonts) and SP-3 (onboarding walkthrough, persistence, regression) open criteria. One emulator session closes all of them.
