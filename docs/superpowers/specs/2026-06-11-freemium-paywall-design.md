# Niyam — Freemium Paywall Sandbox (SP-6)

**Date:** 2026-06-11 · Sub-project 6
**Status:** Founder-approved direction (freemium confirmed; trial mechanics delegated "go with your
recommendations"; sample/test ads OK; sandbox-now-accounts-later accepted). This spec is the async veto
point; the paywall SCREEN visuals follow the locked SP-8 "Sunrise Sans" system and ship with screenshots
for founder veto — no new visual language is introduced.

## 1. The split (founder-confirmed)

| | Free | Premium |
|---|---|---|
| Blocking + mantra overlay + 15s unlock | ✅ always | ✅ |
| Mantras | 5 starters (one per intention) | all 26 |
| Display languages | English, Hindi | all 9 |
| Sadhana switching | within the free set | unlimited |
| Ads | banner on Home + Library | none |
| Journeys, streaks, celebration | ✅ full | ✅ |

- **Free mantra ids** (first priority entry per intention from `StarterMantras`): `gita-2-47` (Focus),
  `mahamrityunjaya` (Calm), `gayatri` (Sadhana), `gita-4-7-8` (Dharma), `hanuman-chalisa-opening`
  (Devotion).
- **Pricing (sandbox):** ₹15/week · ₹49/month · ₹399/year. Numbers live in one enum, repriceable.
- **Trial:** 7 days, app-side, starts at onboarding completion (existing installs: seeded on next
  app start). Trial = full premium experience, no ads.
- **The overlay NEVER carries an ad and is never gated.** The spiritual moment is not for sale.

## 2. Grandfather rules (no rug-pulls)

- **Current sadhana keeps working after trial expiry** even if it's a locked mantra — switching
  *to* another locked mantra is what requires premium. An active journey is never broken.
- **Current display language keeps working** after expiry — the language *editor* locks non-free
  options; we never silently flip a user's script mid-journey.
- Completed mantras: "Practice again" on a locked mantra → paywall (it's a new journey).

## 3. Architecture

### 3.1 `billing/Entitlements` (pure, TDD)

`PremiumState` enum: `PREMIUM` (sandbox-purchased) / `TRIAL` (within 7 days of trial start) / `FREE`.

- `state(premiumActive: Boolean, trialStartEpochDay: Long, todayEpochDay: Long): PremiumState`
  — trial window is `today - start < 7` (exclusive), `trialStart == 0L` counts as not-started (FREE).
- `isPremiumExperience(state)` = PREMIUM or TRIAL
- `canUseMantra(state, mantraId, currentMantraId)` = premium experience ‖ id in FREE_MANTRA_IDS ‖ id == current
- `canUseLanguage(state, lang, currentLang)` = premium experience ‖ lang in {ENGLISH, HINDI} ‖ lang == current
- `trialDaysLeft(trialStartEpochDay, todayEpochDay): Int` (0 when over/unset)
- `FREE_MANTRA_IDS`, `FREE_LANGUAGES` published constants

### 3.2 `billing/BillingGateway` + `SandboxBillingGateway`

`Plan` enum: `WEEKLY(₹15, "week")`, `MONTHLY(₹49, "month")`, `YEARLY(₹399, "year")` — label/price fields.
Interface: `suspend fun purchase(ctx, plan): Boolean`, `fun currentPlan(): Plan?`. Sandbox impl returns
instant success and persists via UserPrefs. Swapping in RevenueCat/Play Billing later = one new
implementation behind this interface + real product ids; no UI change.

### 3.3 `UserPrefs` additions

`trialStartEpochDay: Long` (0 = unset), `premiumActive: Boolean`, `premiumPlan: String?` + setters
(`startTrial`, `setPremium`, `clearPremiumForSandbox`). Seeding: MainActivity warm-up — if
`onboardingComplete && trialStartEpochDay == 0L` → set to today (covers both fresh installs at
onboarding completion and existing installs).

### 3.4 Paywall screen (`billing/PaywallScreen.kt`, route `paywall`)

Locked Sunrise Sans composition: overline `NIYAM PREMIUM`, 700 hero "Every mantra. Every language.",
floating benefits card (26 mantras · 9 languages · unlimited switching · no ads), three `SelectableCard`
plan rows (yearly pre-selected, "Best value" marker), filled-orange 52dp pill CTA "Unlock Niyam",
trial-status caption when in trial, footnote "Sandbox build — no real payment is charged." Purchase →
sandbox success → pop back with premium active.

### 3.5 Gates (entry points to the paywall)

- **Library rows:** lock glyph on mantras outside `canUseMantra` (state FREE only).
- **Mantra detail:** locked mantra → CTA becomes "Unlock with Premium" → paywall.
- **Language editor:** locked languages show lock + tap → paywall (Save only for usable picks).
- **Settings:** new "Niyam Premium" section — status row (Premium active / Trial, N days left /
  Free) → paywall. Debug builds add sandbox controls: *Expire trial now* and *Clear premium*
  (so the founder can see the free tier + ads without waiting 7 days).

### 3.6 Ads (test units only)

`play-services-ads` + Google's official sample app id in the manifest and official test banner unit id
(`ca-app-pub-3940256099942544/6300978111`) — **no AdMob account needed; real account/ids are a later
1-line swap.** Adaptive `AdView` via `AndroidView` at the bottom of Home and Library, rendered ONLY when
`PremiumState == FREE`. Never on the overlay, never during trial/premium.

## 4. Scope

**In:** files above + nav route + strings. **Out (unchanged):** the engine (`service/`, `OverlayManager`
logic, `BlockList`), overlay layout, onboarding flow (trial seeds invisibly), progress/streak logic,
real billing/RevenueCat wiring, AdMob account ids, server anything.

## 5. Acceptance

1. Entitlements TDD suite green (trial boundary day 6/7, premium override, free ids, grandfathered
   current mantra + language, days-left math); full suite green; `assembleDebug` green.
2. Engine untouched: `git diff` over the SP-6 range shows zero `service/`/`overlay/`/`BlockList` changes.
3. Emulator, live: fresh install → trial active (no locks, no ads) → debug-expire trial → Home/Library
   show test banner, library locks visible, locked detail routes to paywall, locked language routes to
   paywall, current sadhana + language still work → sandbox purchase → all locks + ads gone, Settings
   shows "Premium active".
4. Screenshot set of the paywall + locked states + ad banners committed for founder veto (the paywall
   is a new screen — founder sees it before it's considered final).
