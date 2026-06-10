# Niyam — Library Design

**Date:** 2026-06-10
**Product:** Niyam (नियम) — www.myniyam.com
**Sub-project:** 5 of 7 — Library + filters + sadhana switching
**Status:** Designed under the founder's full-control grant — spec is the async review gate; execution proceeds

---

## 1. Purpose

Let users browse all 26 mantras, filter by source/length/intention/deity, read any entry in full, and switch their current sadhana — with an honest warning about what switching does to progress. The Browse Library button lands on Home.

**In plain English:** until now users live with the mantra they picked at setup. After SP-5 they can wander the whole shelf, read anything in their language, and change their practice — with a clear heads-up that a journey restarts if abandoned mid-way.

## 2. Product rules (locked)

| Rule | Decision |
|---|---|
| **Source filter backing** | New `sourceCategory` enum field on every catalog entry — `vedic` \| `upanishad` \| `gita` \| `stotra` — hand-tagged (table in §5), validation-enforced. Schema-additive with a Kotlin default (`STOTRA`) so old parsers survive; `contentVersion` bumps to `2026-06-10.2`. The brief's "Mahavakyas" category has zero catalog entries — omitted (YAGNI; filter chips render only categories present). |
| **Length filter** | Derived at runtime from `estimatedReadSeconds`: under 30s / 30-60s / over 1 min. No schema change. |
| **Filter semantics** | One selection per dimension (or All), dimensions combine with AND. Pure function, unit-tested. |
| **Switch warning (honest copy)** | The brief says switching "pauses" progress; our model re-stamps `sadhanaStartEpochDay`, so a journey **starts over** on return. Dialog: *"You're N days into {name}. If you switch, this journey will start over when you return to it."* [Switch] / [Keep current]. Shown only when current has ≥1 counted day and isn't completed; otherwise switch is immediate. |
| **Detail screen** | Full text in the user's chosen script (scrollable), roman line, meaning in their language, source attribution, chips (category, read time, deity). CTA states: "Make this my sadhana" / disabled "Your current sadhana" / completed entries show a "Completed" chip (salad green) and CTA "Practice again". |
| **Switching mechanics** | `UserPrefs.setCurrentMantra(ctx, id)` — already re-stamps start + clears celebration flag. No new persistence. |

## 3. Scope and non-goals

**In scope:** `SourceCategory` enum + field (model, content, validation); `LibraryFilters` pure filter function (TDD); LibraryScreen (filter chip rows + LazyColumn of entry rows); MantraDetailScreen (+ switch dialog with live day-count); routes `library` and `mantra_detail/{id}`; Home gains secondary "Browse library" OutlinedButton.

**Non-goals:** search box (26 entries don't need it) · favorites · multi-select filters · per-category art · audio · notifications (SP-7) · paywall gating of the library (SP-6 decides) · re-celebration semantics (SP-4 decision stands).

## 4. Architecture

- **Model:** `enum class SourceCategory { VEDIC, UPANISHAD, GITA, STOTRA }` with lowercase `@SerialName`s; `Mantra.sourceCategory: SourceCategory = SourceCategory.STOTRA` (default keeps old JSON parseable). `ContentValidationTest` gains an explicit-category check (the asset must declare it on every entry — the default is a parser safety net, not a content license).
- **Content:** all 26 entries gain `"sourceCategory"` per the §5 table; `contentVersion` → `2026-06-10.2`; `generate_scripts.py --check` unaffected (field is hand-authored, not derived).
- **`LibraryFilters`** (pure, TDD): `data class Selection(category: SourceCategory?, length: LengthBucket?, intention: Intention?, deity: Deity?)`; `LengthBucket.of(seconds)`; `apply(all: List<Mantra>, sel: Selection): List<Mantra>` — AND across non-null dimensions, catalog order preserved.
- **LibraryScreen:** overline "LIBRARY", serif title "All mantras"; four horizontal chip rows (scrollable, single-select with All); LazyColumn rows — serif name, gist (reuse `mantraGist`), trailing read-time chip, small category/deity caption; current entry marked with a small orange dot; completed entries a salad-green check. Tap → detail.
- **MantraDetailScreen(id):** resolves via `MantraRepository.byId` (unknown → pops back); renders per §2; loads current-progress day count via `ProgressRepository.homeStats` for the dialog copy; switch executes `setCurrentMantra` then pops to Home.
- **Navigation:** routes added to NiyamRoutes; detail uses a `{mantraId}` argument. Home button navigates to library.
- **Brand law** as established; ui-ux-pro-max consultation for the UI tasks (chips, list rows).

## 5. Source-category tagging (hand-authored, validation-enforced)

| Category | Entries |
|---|---|
| `vedic` | gayatri, mahamrityunjaya, om-namah-shivaya, purusha-suktam, nasadiya-suktam |
| `upanishad` | om, asato-ma, om-sahanavavatu, hare-krishna |
| `gita` | gita-2-47, gita-6-5, gita-2-14, gita-6-6, gita-2-70, gita-4-7-8, gita-18-66, gita-3-35 |
| `stotra` | twameva-mata, vakratunda, saraswati-vandana, guru-brahma, hanuman-chalisa-opening, vishnu-sahasranama-opening, lalita-sahasranama-opening, krishna-ashtakam, ram-raksha-opening |

(Rationale notes: om-namah-shivaya sourced from Sri Rudram/Yajur Veda → vedic; hare-krishna from Kali-Santarana Upanishad → upanishad; Vishnu Sahasranama is Mahabharata text but functions as a stotra in practice → stotra; Hanuman Chalisa (Awadhi devotional) → stotra. 5+4+8+9 = 26 ✓.)

## 6. Error handling

Unknown detail id → pop back silently. Filter selection yielding zero results → friendly empty state ("Nothing matches — clear a filter"). homeStats failure in the dialog → warning shows without the day count ("You're partway into…"). Switch persistence failure → logged; UI returns Home regardless (snapshot already updated in-memory by the writer's design).

## 7. Acceptance criteria

1. Unit tests: LengthBucket boundaries, LibraryFilters AND-combination + All-passthrough + order preservation, category-tag completeness (validation). Suite grows from 69; all green.
2. Build green; `--check` exit 0 (derived fields untouched); content asset ≤400KB.
3. Emulator: Home → Browse library → filter by Gita → 8 rows; open one → full text in chosen language; switch → warning dialog with day count → confirm → Home shows new mantra. (Manual; rides the standing session.)
4. Engine regression: zero engine-file changes this sub-project; 69 pre-existing tests green.
5. Visual: brand match (async eyeball).

## 8. Engine touches

**None.** SP-5 touches no service, overlay, or BlockList code. The only shared-state write is the existing `UserPrefs.setCurrentMantra`.
