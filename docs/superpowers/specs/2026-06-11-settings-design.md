# Niyam — Settings, Completion Notification & Dark Mode Design

**Date:** 2026-06-11 · Sub-project 7 of 7 (built before SP-6 paywall, per sequencing)
**Status:** Full-control grant — this spec is the async review gate.

## 1. Purpose

Let users re-edit every choice they made in onboarding without reinstalling, get a notification when a 14-day journey completes, and use the app comfortably at night with a bottle-green dark theme.

**In plain English:** today, once you finish onboarding, your language, blocked apps, and intention are frozen — there's no door back to those screens. SP-7 adds that door (a Settings screen), a "your journey is complete 🎉" notification so completions don't go unnoticed, and a dark mode so the app isn't a white flashlight at night.

## 2. Product rules (locked)

| Rule | Decision |
|------|----------|
| Settings entry | Text-free gear `IconButton` top-right of Home, `BottleGreen` tint. Smallest possible Home touch. |
| Editable items | Current sadhana (links into the existing **Library** — no second picker, DRY), Display language (9 options, same native-script labels as onboarding), Blocked apps (same 7-app catalog), Intention (same 5 options), Appearance, Completion notification toggle. App version footer. |
| Language change | Writes `UserPrefs.setDisplayLanguage`; takes effect immediately everywhere (overlay reads the volatile snapshot on next show). |
| Blocked-apps change | Writes `setBlockedPackages`; engine picks it up on next launch-detection (snapshot read). Guard: cannot save an empty set — at least one app must stay blocked (the product is a blocker). |
| Intention change | Writes `setIntention`; affects only future NextSadhana seeding. No journey reset. |
| Appearance | `LIGHT` / `DARK` / `SYSTEM`; **default LIGHT** (Pranav's earlier ruling). Persisted as a new `themePref` field. |
| Completion notification | Toggle, **default ON**. Posted by `ProgressRepository.maybeComplete` right after `markCompleted`, only if toggle ON and `POST_NOTIFICATIONS` granted (permission already in the manifest for the foreground service; on API 33+ the runtime grant is requested when the user flips the toggle ON, or silently skipped if denied). Tapping it opens MainActivity → HOME → the existing `pendingCelebration` LaunchedEffect routes to Celebration. One notification per completion (fired inside the once-only `maybeComplete` path — no dedup needed). |
| Dark palette | Derived by inverting the existing brand tokens, orange untouched: bg `#0A2A20` (deep bottle green), card `#11352A`, hairline `#1E4438`, ink `#F5EBE1` (eggshell), inkMuted `#B9C9C2`, labelMuted `#7E948B`, chipFill `#1A3F33`, orangeTint → `#3A2415` (dark orange wash), `PumpkinOrange`/`SaladGreen` unchanged. Async eyeball veto applies — these are derivations, not new design. |
| Overlay | **Stays light in all themes.** It's the interruption surface and engine-adjacent (View XML, not Compose) — zero engine touches holds for SP-7. Dark overlay queued in `forlater.md`. |

## 3. Scope

**In:** SettingsScreen + 3 editor screens (language / apps / intention) reusing onboarding's `SelectableCard`; `themePref` + `notifyOnCompletion` UserPrefs fields; dark color scheme + theme switch in `NiyamTheme`; completion notification (channel, builder, post hook in ProgressRepository); gear icon on Home; routes.

**Out:** dark overlay (forlater) · daily-reminder notifications (YAGNI; completion push only) · account/profile (no accounts exist) · paywall hooks (SP-6) · re-running permission flow from settings (Home's protection row already handles broken permissions).

## 4. Architecture

- `UserPrefs`: `Snapshot` gains `themePref: ThemePref` (enum LIGHT/DARK/SYSTEM, default LIGHT) and `notifyOnCompletion: Boolean` (default true); writers `setThemePref`, `setNotifyOnCompletion`. Engine reads neither — cold-start behaviour unchanged.
- `ui/theme/Theme.kt`: add `DarkNiyamColors` scheme; `NiyamTheme` resolves LIGHT/DARK/SYSTEM from a snapshot-backed Compose state so the switch applies live. `Color.kt` gains the dark tokens.
- `notifications/CompletionNotifier.kt` (new, app layer): channel `niyam_completion`, builds + posts the notification (PendingIntent → MainActivity); `ProgressRepository.maybeComplete` calls it after `markCompleted`, guarded by toggle + permission check, wrapped in try/catch (a notification failure must never break a read).
- `settings/SettingsScreen.kt`: list of rows (current sadhana → LIBRARY route; language/apps/intention → editor routes; appearance segmented row; notification toggle; version footer from `BuildConfig`).
- `settings/{LanguageSetting,BlockedAppsSetting,IntentionSetting}Screen.kt`: pre-selected from snapshot, save → write → popBackStack. Apps screen enforces non-empty selection (save disabled at zero).
- Routes: `settings`, `settings_language`, `settings_apps`, `settings_intention`; Home gains `onOpenSettings`.
- MainActivity recomposes theme from the snapshot state holder (small `ThemeState` object with a `mutableStateOf` mirror updated by `setThemePref`).

## 5. Error handling

Notification post failure → caught + logged, read still recorded. Permission denied on 33+ → toggle stays ON in prefs but no notification posts (and the toggle row shows a "notifications blocked in system settings" caption when permission is missing). Empty blocked-apps set → save disabled. Unknown themePref on downgrade → default LIGHT.

## 6. Acceptance

1. Unit tests: ThemePref/notify round-trips through UserPrefs; CompletionNotifier guard logic (toggle off → no post; pure-logic extracted, system call faked). Suite grows from 76; all green.
2. Build green; **zero engine-file changes** (service/, OverlayManager, BlockList, overlay XML untouched).
3. Emulator walkthrough (manual, rides the standing session): gear → settings → change language → overlay shows new script; toggle dark → app re-themes live; un-block an app → it opens freely; complete a journey → notification arrives, tap → Celebration.
4. Visual brand match (async eyeball, incl. dark palette veto).
