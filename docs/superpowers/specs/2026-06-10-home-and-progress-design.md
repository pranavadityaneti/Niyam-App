# Niyam — Home & Sadhana Progress Design

**Date:** 2026-06-10
**Product:** Niyam (नियम) — www.myniyam.com
**Sub-project:** 4 of 7 — Home screen + sadhana progression + streak
**Status:** Designed under the founder's full-control grant (2026-06-10) — spec is the async review gate; execution proceeds

---

## 1. Purpose

Give Niyam its retention mechanic: every completed unlock-read counts toward the current mantra's 14-day journey, visible on a real home screen (mantra card, Day N of 14 progress, streak, today's reads). Completing a journey triggers a celebration and a curated next-sadhana picker.

**In plain English:** until now the app blocks and shows; after SP-4 it *remembers* — every mantra read builds a visible streak and a journey, and finishing a journey hands you the next one.

## 2. Definitions (the product rules, locked)

| Term | Rule |
|---|---|
| **Read** | The user tapped Continue after the 15s timer on the overlay. Dismissing any other way ≠ read. |
| **Counted day** | A device-local calendar day (LocalDate → epochDay) with ≥1 read of the current mantra. |
| **Progress** | "Day N of M": N = distinct counted days since the current sadhana started (incl. today if counted); M = the mantra's `completionThresholdDays` (14 for all v1 entries). The brief's "70 reads" framing is the rationale; days are the unit (matches schema + "Day 8 of 14" display). |
| **Streak** | Consecutive calendar days ending today (or yesterday, if today has no read yet) each having ≥1 read of ANY mantra. |
| **Today's reads** | Count of read events today (any mantra). |
| **Completion** | N ≥ M → mantra id appended to completed list; celebration screen; next-sadhana picker. |
| **Sadhana start** | The epochDay when the mantra became current (onboarding pick, post-completion pick, or future library switch). |

## 3. Scope and non-goals

**In scope:** Room database (read_events table) + DAO; `ProgressRepository` (thin DAO wrapper) + pure `ProgressMath` (unit-tested streak/day computations); read recording from the overlay Continue tap (fire-and-forget, engine never blocks); UserPrefs gains `selectedIntention` + `sadhanaStartEpochDay` (+ `completedMantraIds` string set); onboarding persists intention and stamps the start day; Home rebuilt to brand (mantra card + orange progress bar + streak/today chips + compact protection row); Celebration screen + next-sadhana picker (3 cards from `StarterMantras.forIntention`, excluding completed/current — backfilled from the full priority lists); completion detection on read-record.

**Non-goals:** push notifications (SP-7); Browse Library button + library + sadhana switching (SP-5); per-mantra threshold tuning UI; read-time-scaled timers (deferred tension stands — timer stays 15s); historical stats/charts; cloud sync.

## 4. Architecture

- **Room** (`androidx.room` runtime+compiler via KSP): `NiyamDatabase` with one entity `ReadEventEntity(id PK autogen, mantraId: String, epochDay: Long, timestampMs: Long)`. Index on (mantraId, epochDay) and (epochDay).
- **`ProgressRepository`** (object, same warm-up pattern): `recordRead(ctx, mantraId)` — inserts on a background executor, then runs completion check; query methods `distinctDaysFor(mantraId, sinceEpochDay)`, `allReadDays()`, `todayCount()` as suspend DAO calls; exposes a `HomeStats(dayN, dayM, streak, todayReads, completedNow)` loader.
- **`ProgressMath`** (pure, JVM-tested): `streak(readDays: Set<Long>, today: Long): Int`; `dayN(distinctDays: Int, capM: Int): Int`. All date logic uses epochDay longs; `LocalDate.now().toEpochDay()` only at the edges.
- **Engine touch (the one):** `OverlayManager`'s Continue click handler additionally calls `ProgressRepository.recordRead(ctx, mantraId-of-displayed-entry)` before `hide(ctx)`. Wrapped in try/catch; a recording failure can never break the unlock.
- **Completion flow:** `recordRead` returns whether this read completed the journey (distinct days crossed M for the CURRENT mantra). A completed journey: append id to `completedMantraIds`, set a `pendingCelebration` flag in UserPrefs. MainActivity (or Home composition) sees the flag → navigates to `celebration` route → CTA → `next_sadhana` route (3 cards, SelectableCard reuse) → pick persists new currentMantraId + new sadhanaStartEpochDay + clears flag → Home. Skip option ("keep this mantra") restarts the same mantra's journey.
- **Home (rebuilt `HomeScreen.kt`):** brand layout — overline "YOUR SADHANA", serif mantra name, script first-line preview, orange `LinearProgressIndicator` (Day N of M caption), Row of two chips (streak: "N-day streak" salad-green accent when ≥2; today: "N reads today"), compact protection row (green dot + "Protection active" / red dot + "Needs attention" + tap → existing permission flow start) replacing the old 4-row panel.

## 5. Error handling

DB insert failure → logged, unlock unaffected. Stats load failure → home shows zeros (never crashes). Clock skew/backwards-day → streak math tolerates duplicate epochDays (set semantics); negative gaps break streak naturally. Process death between completion and celebration → `pendingCelebration` flag persisted, shown on next launch. Mantra id missing from catalog at picker time → curation already drops unknowns.

## 6. Acceptance criteria

1. Unit tests: ProgressMath (streak across gaps/today-empty/single-day; dayN cap), UserPrefs new fields round-trip, next-picker exclusion logic. Suite grows from 55; all green.
2. Build green; Room schema compiles with KSP; no new permissions.
3. Emulator: tap Continue on the overlay → home shows 1 read today, Day 1 of 14, streak 1. (Manual; rides the standing emulator session.)
4. Completion path testable: temporarily setting a 1-day threshold mantra in a test build verifies celebration→picker→switch (verified via a unit test on the completion predicate + emulator spot-check with threshold-1 debug override removed before commit).
5. Engine regression: 55 pre-existing tests green; overlay timer/unlock untouched apart from the recordRead call.
6. Visual: matches brand (Pranav's async eyeball).

## 7. Engine touches (flagged per the scope rule)

| Touch | Why | Risk/mitigation |
|---|---|---|
| OverlayManager Continue handler += recordRead (try/catch, background) | Reads ARE the product mechanic | Unlock can never block/crash on it; recording is fire-and-forget |
| NiyamApplication warm-up += Room init (lazy) | First insert latency | Room builds lazily on executor; no main-thread I/O |
| Onboarding VM/screens: persist intention + stamp start day | Picker seeding + Day-N anchor | Additive; defaults (SADHANA intention, today) when absent |

## 8. Deferred

Notifications (SP-7) · library + switch warning (SP-5) · threshold tuning · stats history · timer scaling.
