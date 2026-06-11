# Niyam — UI Chrome Translation (SP-11)

**Date:** 2026-06-11 · Sub-project 11
**Status:** Founder-approved ("Let's finish Chrome translation"). This spec is the async veto point.

## 1. Purpose

Today the language picker localises the mantra text + meaning only; every button, label, dialog and
settings row stays English. SP-11 makes the whole app speak the user's language: pick తెలుగు and
"Browse library" becomes Telugu too.

## 2. Decisions

| Decision | Ruling |
|---|---|
| Languages | 7 new chrome locales — `hi, mr, te, ta, kn, bn, gu` — via standard Android `values-<lang>/strings.xml`. English remains the default (`values/`). |
| Sanskrit chrome | `DEVANAGARI_SANSKRIT` users get **Hindi chrome** (mantra text stays Sanskrit/Devanagari). Writing app chrome in Sanskrit would be artificial and error-prone; Hindi is the natural Devanagari UI language. Documented, revisitable. |
| Driver | The IN-APP language choice (UserPrefs.displayLanguage), NOT the device locale. `LocaleBridge` maps DisplayLanguage → `Locale` and wraps Contexts via `createConfigurationContext`. |
| Activity plumbing | `MainActivity.attachBaseContext` → `UserPrefs.ensureLoaded(newBase)` (idempotent, already-blocking pattern) → `super.attachBaseContext(LocaleBridge.wrap(newBase))`. Compose `stringResource`/`pluralStringResource` then resolve localized automatically. |
| Language switch at runtime | Saving a new language recreates the activity (`activity.recreate()` after popBackStack in the nav callback). Navigation state restores per navigation-compose defaults; verified live. |
| Overlay chrome | `OverlayManager.show` wraps its context once with `LocaleBridge.wrap(ctx)` for inflate + getString — the overlay's overline and Continue button localise too. Visual-binding-only change, same authorized class as SP-8/SP-10 overlay touches. |
| System surfaces | The foreground-service notification and the accessibility-service description keep following the DEVICE locale (they are system-owned surfaces; also keeps engine service files untouched). Their strings still ship translated so device-locale users benefit. |
| What stays English | `app_name` ("Niyam" brand, launcher label), the debug-only sandbox rows, version format. Everything else translates, including permission explanations and the paywall. |
| Placeholders & plurals | `%1$d`/`%1$s` positions preserved exactly; every `<plurals>` provides `one` + `other` (valid across all 8 CLDR rule sets here). Prices stay "₹N". |
| Translation authorship | Claude-authored (same provenance as the shipped mantra meanings), devotional-appropriate register, natural register over literal word-for-word. Native-speaker review rides the existing forlater item 1 before public launch. |

## 3. Architecture

- **`ui/LocaleBridge.kt`** (new, pure mapping + one wrap fn) — `localeFor(DisplayLanguage): Locale`
  (exhaustive when), `wrap(Context): Context`.
- **`values-hi|mr|te|ta|kn|bn|gu/strings.xml`** (7 new files) — full translations of every
  translatable string + plurals in `values/strings.xml`.
- **Touched:** `MainActivity` (attachBaseContext), `AppNavHost` (recreate after language save),
  `OverlayManager` (one wrap line). Nothing else.

## 4. Scope

**Out:** engine logic, mantra content (already multilingual), RTL (none of these languages are RTL),
per-app-locale system setting (API 33 `LocaleManager`) — can be added later without redesign.

## 5. Acceptance

1. `LocaleBridge` unit-tested (9 DisplayLanguage → locale mappings); full suite green; build green.
2. All 7 locale files parse (resource compile is the gate) with placeholder/plural parity vs English —
   verified by an automated comparison script run during review (same string names, same format args).
3. Emulator, live: switch to Hindi → ENTIRE app chrome in Hindi (Home, Library, Settings, paywall,
   overlay overline/Continue); same spot-check for Telugu + Tamil; English unchanged; switch back works
   (activity recreates cleanly).
4. Screenshot gallery (Hindi/Telugu/Tamil chrome) committed for founder read-through — Pranav reads
   these languages and is the first review gate.
