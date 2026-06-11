# Niyam — Paywall v2: Trial Timeline + Day-6 Reminder (SP-12)

**Date:** 2026-06-11 · Sub-project 12
**Status:** Founder-approved via reference image + mockup ("Good to go"). Background/design system
unchanged (his explicit instruction); this is a content-architecture restyle of the SP-6 paywall plus
one new feature (the trial-ending reminder the timeline promises).

## 1. Approved layout (from the founder's reference, adapted to our mechanics)

**Default view (state == TRIAL):** overline + close → 3-step timeline (icon circles + connector):
"Today — Everything is unlocked, your 7-day trial is already on" / "Day 6 — We remind you before it
ends" / "Day 7 — Trial ends, keep it all with Premium" → floating annual card (BEST VALUE chip,
"Annual — ₹399", "≈ ₹33 a month, billed yearly", orange check) → links row (Restore purchase ·
More plans ▾) → trust pill ("Sandbox — no real payment") → pinned orange CTA "Unlock Niyam".

**Expanded view (after "More plans", and the DEFAULT when state == FREE — the timeline's "trial is
on" line would be false post-trial):** hero "Every mantra. Every language." → three selectable plan
cards (Annual w/ badge + per-month line · "Monthly — ₹49, flexible, cancel anytime" · "Weekly — ₹15,
try it a week at a time") → links (… Fewer plans ▴) → same trust pill + CTA.

**Dropped from v1:** benefits checklist card, trial-days caption, bare "₹N / period" SelectableCards
(superseded by PlanCard with name + subline). Dead strings removed in EN and all 7 locales.

## 2. Day-6 trial reminder (new)

- **Founder asked for SMS/email automation.** Not buildable yet: the app collects no phone/email and
  has no backend. Queued as forlater item 9 (needs accounts + server + provider + privacy update).
  Built now instead: **local notification**, reliable, zero personal data.
- `billing/TrialReminder.shouldRemind(premiumActive, trialStartEpochDay, todayEpochDay, alreadyShown)`
  — pure, TDD: true iff state == TRIAL && daysLeft <= 1 && !alreadyShown. Premium purchase or no
  trial → never.
- `notifications/TrialReminderNotifier` — own channel, tap opens MainActivity; copy "Your trial ends
  tomorrow / Keep every mantra and language — unlock Niyam Premium." Device-locale strings (system
  surface, per SP-11 ruling) but translated in all locales.
- **Delivery: WorkManager** (`work-runtime-ktx 2.9.1`, new dependency) — unique daily periodic worker
  enqueued at Application start (KEEP). Survives reboots, runs without the app being opened.
  `UserPrefs.trialReminderShown` flag prevents repeats.

## 3. Scope

**In:** PaywallScreen rewrite, TrialReminder + worker + notifier, UserPrefs flag, strings (EN + 7
locales, incl. dead-string removal), WorkManager dep. **Out:** engine, billing gateway mechanics,
SMS/email (forlater 9), real-billing trust-pill copy ("Secured with Google Play" lands with Play
Console).

**Icon deviation (disclosed):** mockup showed padlock-open/bell/check-circle; material-icons-core has
no LockOpen, so Done/Notifications/CheckCircle are used. Adding icons-extended stays off the table.

## 4. Acceptance

1. TrialReminder TDD green; suite green; build green; engine diff zero.
2. Emulator: TRIAL state shows timeline+annual default; "More plans" expands/collapses; FREE state
   opens expanded; sandbox purchase still unlocks; Restore shows the sandbox toast.
3. Reminder logic: debug-verified by forcing daysLeft==1 (sandbox expire sets start = today-7 →
   daysLeft 0; verification uses a start of today-6 via unit tests + a manual worker trigger).
4. Localized paywall spot-check (Hindi) + screenshots for the founder.
