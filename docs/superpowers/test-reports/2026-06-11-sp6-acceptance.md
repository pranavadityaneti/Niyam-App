# SP-6 Acceptance Report — Freemium Paywall Sandbox

**Date:** 2026-06-11
**Spec:** `docs/superpowers/specs/2026-06-11-freemium-paywall-design.md` (commit `c25cd19`, async veto point)
**Commits:** `9236b98` (Entitlements + sandbox billing + prefs, TDD) · `00100a9` (paywall screen + route) · `a5a8660` (gates, settings section, trial seeding) · `13f4928` (AdMob test banners) · `5286363` (suffix spacing fix)

## Spec §5 acceptance criteria

| # | Criterion | Result |
|---|-----------|--------|
| 1 | Entitlements TDD green; full suite; assembleDebug | ✅ 18 new EntitlementsTest cases (trial day-6/7 exclusive boundary, premium override, clock rollback, free ids, grandfathered current mantra+language, days-left math); **suite 98 → 116/116** (`--rerun-tasks`, XML sum) re-verified at HEAD; build green |
| 2 | Engine untouched | ✅ `git diff c25cd19..HEAD --name-only` — zero `service/`/`overlay/`/`BlockList` files |
| 3 | Live emulator flow | ✅ Pixel 9: install → warm-up seeds trial → Settings "7 days of trial left" + sandbox rows → Library NO locks in trial → debug-expire → Home + Library show Google TEST banners, locks on premium mantras only (free starters clean) → locked Om detail CTA "Unlock with Premium" → paywall → locked Telugu language routes to paywall → sandbox purchase (yearly) → pops back, CTA flips to "Make this my sadhana", ads gone, Settings "Active" → debug-clear → free-tier gates return |
| 4 | Paywall + locked-state screenshots for founder veto | ✅ 8-shot gallery `docs/screenshots/sp6-premium/index.html` (self-contained) — the paywall is a NEW screen rendered in the locked Sunrise Sans system; founder veto pending |

## Found & fixed during verification

- **"Telugu· Premium" missing space** — Android XML trims unquoted leading whitespace in string resources; suffix quoted (`5286363`), re-verified live ("Telugu · Premium").

## Sandbox levers (debug builds only)

Settings → Niyam Premium: **Sandbox: expire trial now** (jump to the free tier instantly) and **Sandbox: clear premium + trial** (next app launch re-seeds a fresh 7-day trial — the "new user" reset). Test device left in fresh-trial state.

## Going live later (not in scope now)

Real money needs: Play Console account (₹/$25) + Play Billing products (or RevenueCat on top) → implement `BillingGateway` with the real SDK and swap `SandboxBillingGateway` at the single construction site; AdMob account → replace the two Google sample ids. No UI or entitlement-logic changes required.

## In plain English

The app now has a working business model you can hold in your hand: new users get everything free for 7 days; afterwards the 5 starter mantras and English/Hindi stay free with a small test ad on Home and the library, and everything else asks for Premium at ₹15/week, ₹49/month, or ₹399/year. Nobody's running journey or chosen language is ever broken by the lock. The "payment" is pretend for now (clearly labelled), the ads are Google's official test ads — both become real with account keys, not new code. The mantra overlay — the soul of the app — has no ads, no locks, and its code wasn't touched.
