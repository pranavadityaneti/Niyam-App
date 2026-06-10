# Niyam — Onboarding & Brand Theme Design

**Date:** 2026-06-10
**Product:** Niyam (नियम) — www.myniyam.com
**Sub-project:** 3 of 7 — Onboarding flow + brand theme
**Status:** Design approved by Pranav 2026-06-10 ("Orange is a must") — pending spec review

---

## Table of Contents

1. [Purpose](#purpose)
2. [Scope and non-goals](#scope-and-non-goals)
3. [Decisions locked during design](#decisions-locked-during-design)
4. [Brand theme](#brand-theme)
5. [Onboarding flow](#onboarding-flow)
6. [Mantra curation rule](#mantra-curation-rule)
7. [Persistence architecture](#persistence-architecture)
8. [Engine touches (founder-approved at design stage)](#engine-touches-founder-approved-at-design-stage)
9. [Error handling](#error-handling)
10. [Acceptance criteria](#acceptance-criteria)
11. [Known risks](#known-risks)
12. [Decisions deferred](#decisions-deferred)
13. [Dependencies on Pranav](#dependencies-on-pranav)

---

## Purpose

Turn the engineering prototype into the product's first real experience: apply the founder-supplied brand direction app-wide, and build the four-step onboarding that captures intention → starter mantra → language → blocked apps, persists those choices, and feeds them to the blocking engine. After SP-3, a fresh install's first five minutes work end-to-end and look like the reference.

**In plain English:** today the app works but looks like a lab bench, and it ignores who the user is. After this sub-project it looks like the calm reading app Pranav showed, and the overlay shows *your* mantra in *your* language over *your* chosen apps.

---

## Scope and non-goals

### In scope

- `NiyamTheme` rebuilt from the brand palette + reference aesthetic (light theme only); applied to all Compose screens.
- Bundled fonts: Playfair Display (display serif) + Inter (UI sans).
- Overlay (`overlay_mantra.xml`) restyled to the new theme — colors/typography/spacing only; identical view ids, structure, timer, and unlock logic.
- Onboarding flow: Welcome (restyled) → Intention → Starter-mantra picker → Language → Apps-to-block → existing 5 permission screens (inherit theme) → Home.
- Persistence via Jetpack DataStore: `onboardingComplete`, `currentMantraId`, `displayLanguage`, `blockedPackages`; in-memory snapshot loaded at app start.
- `CurrentSadhana` and `BlockList` become snapshot-backed (defaults = today's hardcoded values).
- Returning users (onboardingComplete) route straight to Home.

### Explicit non-goals

- Home screen redesign, mantra card, progress, streak (SP-4). Home keeps its current permission-status content, restyled only by the theme.
- Library browse/filters, sadhana switching (SP-5).
- Paywall, trial, ads (SP-6).
- Dark mode, settings screen, notifications, re-onboarding/edit flows (SP-7 — settings will reuse the same DataStore keys).
- Illustrations, iconography sets, deity imagery (none; "no AI-generated deity imagery" rule stands).
- Audio, haptics, animations beyond default Material motion.

---

## Decisions locked during design

| Decision | Choice | Why / context |
|---|---|---|
| Visual direction | Founder references (editorial reading-app aesthetic) + 4-color brand palette | Supersedes the original brief's maroon/saffron/gold. Reference images described in project memory; Pranav to drop files in `docs/design-refs/`. |
| **Orange** | **Non-negotiable hero accent** — every primary CTA, selection state, active progress | Pranav verbatim: "Orange is a must" (2026-06-10). Discipline: fills/large elements only, never small body text (contrast). |
| Theme default | Light (eggshell) | Pranav-confirmed; refs are light-first. Dark mode deferred to SP-7. |
| Type | Playfair Display (display) + Inter (UI), bundled; Indic scripts via system Noto | Closest free match to the refs' high-contrast serif; Inter was already the brief's body font. Bundling 7-script serif families = megabytes; deferred. |
| Mockup | Approved 2026-06-10 (intention screen + overlay, two-phone widget) | The committed visual translation of the refs. |
| Curation | Deterministic per-intention priority lists (the brief's own groupings), first 3 shown | No fake algorithm; product-true; testable. |
| Persistence | Jetpack DataStore (Preferences) + app-start warm-up snapshot | Engine needs synchronous reads; same warm-up pattern as MantraRepository. |
| Block-list catalog | Instagram, YouTube, Facebook, X, Reddit, Snapchat, TikTok; defaults = today's 3 | TikTok banned in India, kept for diaspora installs (noted in-screen copy not required). |
| Engine touches | Approved at design stage (see §8) | CurrentSadhana + BlockList become snapshot-backed; services untouched. |

---

## Brand theme

### Color tokens

| Token | Hex | Role |
|---|---|---|
| `Eggshell` | `#F5EBE1` | App background; overlay background |
| `Card` | `#FFFDF8` | Card/surface fills |
| `Hairline` | `#E8DCCD` | Card borders, dividers |
| `BottleGreen` | `#003223` | Primary ink: titles, body on light |
| `InkMuted` | `#33524A` | Secondary text (meanings, captions) |
| `LabelMuted` | `#8A7F72` | Small-caps labels, hints |
| `PumpkinOrange` | `#FF6400` | **Hero accent**: CTA pills, selected borders/checks, active progress |
| `OrangeTint` | `#FFF3EA` | Selected-card fill |
| `SaladGreen` | `#8CC850` | Success/completion states only |
| `ChipFill` | `#ECE0D2` | Understated chips (countdown, read-time) |

Material3 mapping: `primary=PumpkinOrange, onPrimary=White, background=Eggshell, onBackground=BottleGreen, surface=Card, onSurface=BottleGreen, surfaceVariant=ChipFill, outline=Hairline, secondary=SaladGreen`. Hardcoded brand hexes live ONLY in `ui/theme/Color.kt`; screens consume `MaterialTheme.colorScheme`.

**Orange contrast rule:** `#FF6400` on `#F5EBE1` passes contrast only at large/bold sizes — orange is used as fills (white text on orange) and thick borders/icons ≥ 18dp, never as body-text color.

### Typography

| Style | Font | Use |
|---|---|---|
| Display/headline | Playfair Display (SemiBold/Medium) | Screen titles ("Why are you here?"), Welcome wordmark |
| Body/label | Inter (Regular/Medium) | Everything else UI |
| Overline | Inter Medium, 11sp, letter-spacing 1.5sp, all-caps | "STEP 1 OF 4", "YOUR SADHANA" labels |
| Mantra text (overlay) | `serif` family (system Noto Serif per script; Devanagari serif guaranteed, some South Indian scripts render sans) | Honest v1 limitation, recorded in §11 |

Fonts ship as `res/font/` resources (Playfair Display ~2 weights + Inter ~2 weights, OFL-licensed, ≈600KB total — see §11).

### Shape & components

- Cards: 14-16dp radius, `Card` fill, 1.5dp `Hairline` border; selected: 2dp `PumpkinOrange` border + `OrangeTint` fill + orange check icon.
- Buttons: full-width pill (999dp), orange fill, white Inter Medium label; disabled = 40% alpha orange.
- Chips: `ChipFill` pill, 11.5sp `LabelMuted` text.
- Bottom-anchored CTAs; generous whitespace; no shadows beyond Material defaults.

### Overlay restyle (`overlay_mantra.xml` — same ids, logic untouched)

Full-screen `Eggshell` background (replaces black scrim). Top overline: "YOUR SADHANA · {canonicalName uppercased}". Mantra text in `serif`, 19-24sp (auto-size between, for long texts), `BottleGreen`, line-height ~1.7, top-third start. Roman line: Inter italic 12sp `InkMuted`-green. Meaning: Inter 12.5sp `InkMuted`. Countdown chip centered above the CTA. CTA: orange pill "Continue", disabled-alpha until timer ends. The `overlay_scrim` color resource is repurposed to Eggshell (opaque — also resolves the SP-1 "content bleeds through" observation).

---

## Onboarding flow

Linear NavHost flow; back allowed; each step persists its choice immediately on "Continue."

| # | Route | Title (Playfair) | Content | CTA |
|---|---|---|---|---|
| 0 | `welcome` | "Niyam" wordmark | Subtitle "A pause before the scroll." | "Get started" |
| 1 | `onboarding_intention` | "Why are you here?" | 5 single-select cards: Focus better, scroll less / Calm a busy mind / Start a daily sadhana / Feel more connected to dharma / Deepen my devotion. Overline "STEP 1 OF 4". | "Continue" (disabled until selection) |
| 2 | `onboarding_mantra` | "Pick your starter mantra" | 3 cards from curation rule (§6): canonicalName (serif), one-line gist (first sentence of `meaning.en`), read-time chip ("~30 sec"). Overline "STEP 2 OF 4". | "Continue" (disabled until selection) |
| 3 | `onboarding_language` | "Your language" | 9 single-select rows: native-script label + English caption — हिन्दी (Hindi), English, संस्कृत — देवनागरी (Sanskrit), मराठी (Marathi), తెలుగు (Telugu), தமிழ் (Tamil), ಕನ್ನಡ (Kannada), বাংলা (Bengali), ગુજરાતી (Gujarati). Default pre-selected: English. Overline "STEP 3 OF 4". | "Continue" |
| 4 | `onboarding_apps` | "What pulls you in?" | Checklist: Instagram, YouTube, Facebook, X, Reddit, Snapchat, TikTok — rows with app name + checkbox; pre-checked: Instagram, YouTube, Facebook. Overline "STEP 4 OF 4". | "Continue" (disabled if none checked) |
| 5+ | existing `permission_*` routes | (existing copy) | Inherit new theme automatically (Compose/Material3). | existing |
| end | `home` | — | `onboardingComplete=true` written; subsequent launches route Welcome→skip to Home. | — |

Welcome screen on a fresh install starts the flow; on `onboardingComplete`, `MainActivity` sets NavHost start destination to `home`.

---

## Mantra curation rule

Per-intention ordered priority lists — exactly the brief's groupings; the screen shows the first 3:

| Intention | Priority order (ids) |
|---|---|
| focus | gita-2-47, gita-6-5, gita-2-14, asato-ma, gita-6-6 |
| calm | mahamrityunjaya, om-sahanavavatu, om-namah-shivaya, gita-2-70, twameva-mata |
| sadhana | gayatri, vakratunda, saraswati-vandana, guru-brahma, hare-krishna |
| dharma | gita-4-7-8, gita-18-66, gita-3-35, purusha-suktam, nasadiya-suktam |
| devotion | hanuman-chalisa-opening, vishnu-sahasranama-opening, lalita-sahasranama-opening, krishna-ashtakam, ram-raksha-opening |

Implemented as a unit-tested pure function `StarterMantras.forIntention(intention): List<Mantra>` (resolves ids via `MantraRepository`, drops any id missing from the catalog — defensive, test-enforced). This same table seeds SP-4's "next sadhana" recommendations later.

---

## Persistence architecture

- **New dependency:** `androidx.datastore:datastore-preferences` — pin the latest stable 1.1.x at implementation time (1.1.1 known-good); record the pin in the version catalog.
- **`UserPrefs`** (new, `data/` package): suspend writers + a `@Volatile` immutable `Snapshot(onboardingComplete: Boolean, currentMantraId: String, displayLanguage: DisplayLanguage, blockedPackages: Set<String>)` loaded at app start and refreshed on every write. Defaults: `false / "gayatri" / DisplayLanguage.DEVANAGARI_SANSKRIT / {instagram, facebook, youtube packages}`.
- **`NiyamApplication.onCreate`** warm-up thread extends to: `MantraRepository.ensureLoaded(this); UserPrefs.ensureLoaded(this)`.
- **`CurrentSadhana`** keeps its API (`MANTRA_ID`, `LANGUAGE`) but becomes computed properties reading `UserPrefs.snapshot()` — call sites (OverlayManager) unchanged in shape.
- **`BlockList.matches(pkg)`** reads `UserPrefs.snapshot().blockedPackages`; `HARDCODED_PACKAGES` renamed `DEFAULT_PACKAGES` (the fallback + first-run default). Existing BlockList tests updated to cover both default and overridden snapshots.
- Onboarding screens write via a small `OnboardingViewModel` (the project's first ViewModel — justified by flow state across 5 screens).

---

## Engine touches (founder-approved at design stage)

Per the global scope rule, every engine-adjacent change, its why, and its blast radius:

| Touch | Why | What could break / mitigation |
|---|---|---|
| `CurrentSadhana` constants → snapshot-backed properties | Overlay must honor onboarding choices | Same API shape; defaults = today's values; if prefs unread at call time, defaults apply (warm-up makes this a first-millisecond-of-first-launch edge) |
| `BlockList` hardcoded set → snapshot-backed | Apps-to-block screen must take effect | Same `matches()` contract; AccessibilityService untouched; a blocked-app launch racing the very first prefs load falls back to the 3 defaults once |
| `NiyamApplication` warm-up +1 line | Prefs must be readable synchronously by engine paths | None beyond the above |
| `overlay_mantra.xml` restyle | Brand | Same ids/structure; timer/unlock logic untouched; emulator-verified |

`AppLockAccessibilityService`, `AppLockForegroundService`, manifest, permissions: **zero changes.**

---

## Error handling

| Failure | Behavior |
|---|---|
| DataStore read fails at warm-up | Snapshot = defaults; app fully functional as today; error logged |
| Persisted `currentMantraId` missing from catalog (future content change) | `MantraRepository.displayMantra` already falls back (om); curation function drops unknown ids |
| Process death mid-onboarding | Steps already confirmed are persisted; relaunch starts at Welcome with prior selections pre-filled; `onboardingComplete` only written at flow end |
| User unchecks every app | CTA disabled — cannot proceed with empty block list |
| Font resource failure | Impossible at runtime (bundled resources are compile-checked) |

---

## Acceptance criteria

1. **Fresh-install walkthrough (emulator):** Welcome → 4 onboarding steps → 5 permission screens → Home, every screen in the new theme, every CTA orange, no regressions in the permission deep-links.
2. **Choices take effect:** pick "Calm a busy mind" → Mahamrityunjaya/Sahanavavatu/Om-Namah-Shivaya offered; pick one + pick தமிழ் (Tamil) + check only Instagram → overlay on Instagram launch shows that mantra in Tamil script with Tamil meaning; YouTube launches clean (unblocked).
3. **Persistence:** force-stop + relaunch → straight to Home; overlay choices intact across process death.
4. **Returning-user routing:** `onboardingComplete` skips onboarding.
5. **Unit tests:** curation rule (5 intentions × ordering + unknown-id drop), UserPrefs snapshot mapping + defaults, BlockList default/overridden behavior, DisplayLanguage label mapping. Suite grows from 43; all green.
6. **Engine regression:** detection→overlay→timer→unlock unchanged on emulator; 15s timer intact; debounce intact.
7. **Visual sign-off (Pranav):** the built screens match the approved mockup's feel — his eyeball is the gate, same as the Phase-1 pattern.
8. APK size growth ≤ 1MB (fonts ≈ 600KB budget).

---

## Known risks

1. **Indic serif inconsistency (v1 accepted):** Devanagari renders in system serif; some scripts (Tamil/Telugu/Kannada) may fall back to sans in the overlay. Bundling 7 serif families (~several MB) deferred; revisit at UI polish with subsetted fonts.
2. **First-launch race:** a blocked-app launch in the milliseconds before the prefs warm-up completes uses default values once. Accepted; warm-up starts in `Application.onCreate`.
3. **Orange contrast:** enforced by the fills-only rule (§4); a future violation would fail acceptance 7.
4. **Playfair Display has no Devanagari** — wordmark "Niyam" renders in Latin; any future Devanagari wordmark (नियम) needs a Devanagari display font (deferred).
5. **OnboardingViewModel is the first ViewModel** — adds `lifecycle-viewmodel-compose` dependency; conventional, low risk.

---

## Decisions deferred

Dark mode + theme toggle (SP-7) · settings re-edit of all onboarding choices (SP-7) · per-app icons in the block-list screen (needs icon strategy; text rows in v1) · "Skip for now" on permission screens (existing behavior kept) · Devanagari wordmark · custom motion/transitions · Telugu/Tamil/Kannada serif bundling.

---

## Dependencies on Pranav

1. **Spec review** (this document) — gate to the implementation plan.
2. **Drop the two reference images** into `docs/design-refs/` (chat-only today; repo permanence).
3. **Visual sign-off** at acceptance (criterion 7) — and the still-pending SP-2 emulator check rides along in the same session.
