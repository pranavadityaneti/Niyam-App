# SP-5 Acceptance Report — Library + Filters + Sadhana Switching

**Date:** 2026-06-11
**Spec:** `docs/superpowers/specs/2026-06-10-library-design.md`
**Plan:** `docs/superpowers/plans/2026-06-10-niyam-library.md`
**Commits:** `7dc6542` (sourceCategory) · `fb4956b` (LibraryFilters) · `1d0a05e` (LibraryScreen) · `b0c726c` (MantraDetailScreen + switch dialog) · `d33337f` (routes + Home button) · `efbaefc` (detail chips spec-fidelity fix)

## Spec §7 acceptance criteria

| # | Criterion | Result |
|---|-----------|--------|
| 1 | Unit tests: LengthBucket boundaries, filter AND/All/order, category completeness; suite grows from 69, all green | ✅ Suite 69 → **76/76** from JUnit XMLs (`--rerun-tasks`). ContentValidationTest +2 (explicit-declaration scan + exact 26-id→category map); LibraryFiltersTest +5 (boundaries 29/30/60/61, passthrough order, gita=8, gita∩calm=[gita-2-14, gita-2-70], deity/length) |
| 2 | Build green; `--check` exit 0; asset ≤400KB | ✅ `assembleDebug` green at every task; `generate_scripts.py --check` exit 0 (re-run at HEAD); asset 163,590 bytes |
| 3 | Emulator walkthrough: browse → filter Gita (8 rows) → detail → switch with warning → Home shows new mantra | ⏳ **Deferred** — rides the standing manual emulator session with the SP-2/SP-3/SP-4 criteria (no emulator in this execution window) |
| 4 | Zero engine-file changes; pre-existing tests green | ✅ `git diff 4595149..HEAD --name-only` contains no service/, OverlayManager, or BlockList files; all 69 pre-existing tests green within the 76 |
| 5 | Visual brand match (async eyeball) | ⏳ Pending Pranav — FilterChip selection uses OrangeTint/BottleGreen override on every chip; markers PumpkinOrange (current) / SaladGreen (completed); info chips ChipFill/InkMuted |

## Reviews

- **T1+T2 combined review (data layer):** ✅ pass — category tags byte-for-byte vs spec §5 table; old-parser compat confirmed (`ignoreUnknownKeys`); default-STOTRA masking mitigated by the raw-JSON scan test.
- **T3-T5 combined review (UI):** ✅ pass with two flags, both resolved/dispositioned:
  1. Detail screen missing deity + Completed chips (spec §2) → **fixed** in `efbaefc` (category/read-time/deity InfoChips + SaladGreen Completed chip; deity chip skipped for UNIVERSAL).
  2. Spec §6 said homeStats failure → "dialog without day count"; implementation switches directly instead (failure folds into the dayN==0 "no countable progress" path). **Kept deliberately** — a warning dialog with missing data is less honest than treating unreadable progress as no progress. Flagged for Pranav; one-line revert possible if he prefers the spec behaviour.

## Notable implementation details

- Switch-dialog gating implements the plan's mandatory refinement: `homeStats().dayN` is loaded **before** the dialog decision; dialog only when dayN ≥ 1 and current sadhana is uncompleted/not pending celebration; otherwise direct switch.
- `label()` enum extensions return hardcoded English ("Vedic", "Shiva", …) per the accepted plan design — flagged for any future localization pass.
- No `runBlocking` on the main thread anywhere in the new screens; all persistence/stats work in coroutines.

## Verdict

**SP-5 code-complete.** Criteria 1, 2, 4 pass; 3 and 5 are manual and queued for the standing emulator/eyeball session.
