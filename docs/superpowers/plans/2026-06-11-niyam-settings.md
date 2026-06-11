# Niyam — Settings, Completion Notification & Dark Mode — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let users re-edit every onboarding choice (current sadhana, display language, blocked apps, intention), get a completion notification when a 14-day journey finishes, and use the app in a bottle-green dark theme — per [the SP-7 spec](../specs/2026-06-11-settings-design.md). **Zero engine-file changes.**

**Architecture:** Two additive `UserPrefs.Snapshot` fields (`themePref`, `notifyOnCompletion`) with writers that also mirror into a tiny `ThemeState` `mutableStateOf` so `NiyamTheme` re-themes live; a `DarkNiyamColors` scheme derived by inverting brand tokens (orange untouched); a `CompletionNotifier` (app layer) with a pure, unit-tested guard, hooked into `ProgressRepository.maybeComplete` inside a try/catch so a notification failure can never break a read; a `SettingsScreen` plus three editor screens that reuse onboarding's public `SelectableCard`; the current-sadhana row navigates to the existing `LIBRARY` route (no second picker). New routes `settings`, `settings_language`, `settings_apps`, `settings_intention`; Home gains a gear `IconButton` and an `onOpenSettings` callback.

**Tech Stack:** existing stack only — no new dependencies. One build-config change: enable `buildConfig = true` so `BuildConfig.VERSION_NAME` is available for the settings version footer (currently only `compose = true` is set).

---

## Execution conventions

Continuous full-control mode, Opus subagents, combined or two-stage review per task, direct commit+push to `main`. Baseline: HEAD `f210c40`, **76 tests green**. Brand law (orange only for selection/CTA; dark tokens exactly as the spec table) + ui-ux-pro-max protocol for UI tasks (4, 5, 6). Every commit ends with the `Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>` trailer and is pushed to `origin main`.

**Engine isolation (hard gate):** no changes to anything under `service/`, no `OverlayManager`, no `BlockList`, no overlay XML. The only progress-layer change permitted is the notifier call inside `ProgressRepository.maybeComplete` (Task 3), which must be `try`/`catch`-wrapped.

## File structure

```
app/src/main/java/com/myniyam/app/data/UserPrefs.kt                          M  T1 (ThemePref + 2 fields + 2 writers + ThemeState mirror)
app/src/test/java/com/myniyam/app/data/UserPrefsTest.kt                       M  T1 (+4 tests)
app/src/main/java/com/myniyam/app/ui/theme/ThemeState.kt                      C  T2 (mutableStateOf mirror)
app/src/main/java/com/myniyam/app/ui/theme/Color.kt                          M  T2 (dark tokens)
app/src/main/java/com/myniyam/app/ui/theme/Theme.kt                          M  T2 (DarkNiyamColors + resolution)
app/src/main/java/com/myniyam/app/MainActivity.kt                            M  T2 (seed ThemeState after ensureLoaded)
app/src/main/java/com/myniyam/app/notifications/CompletionNotifier.kt         C  T3 (channel + builder + pure guard)
app/src/test/java/com/myniyam/app/notifications/CompletionNotifierTest.kt     C  T3 (+3 tests)
app/src/main/java/com/myniyam/app/progress/ProgressRepository.kt             M  T3 (notifier hook, try/caught)
app/src/main/java/com/myniyam/app/NiyamApplication.kt                        M  T3 (register completion channel)
app/src/main/java/com/myniyam/app/settings/SettingsScreen.kt                  C  T4
app/src/main/res/values/strings.xml                                          M  T4, T5, T6
app/build.gradle.kts                                                         M  T4 (buildConfig = true)
app/src/main/java/com/myniyam/app/settings/LanguageSettingScreen.kt           C  T5
app/src/main/java/com/myniyam/app/settings/BlockedAppsSettingScreen.kt        C  T5
app/src/main/java/com/myniyam/app/settings/IntentionSettingScreen.kt          C  T5
app/src/main/java/com/myniyam/app/ui/AppNavHost.kt                           M  T6 (4 routes + wire onOpenSettings)
app/src/main/java/com/myniyam/app/ui/screens/HomeScreen.kt                   M  T6 (gear IconButton + onOpenSettings)
docs/superpowers/test-reports/2026-06-11-sp7-acceptance.md                    C  T7
```

Expected test growth: 76 → **83** (UserPrefs +4 = T1, CompletionNotifier +3 = T3). UI tasks add no unit tests.

Per-task suite checkpoints: after T1 → **80**; T2 → 80 (no new tests); after T3 → **83**; T4/T5/T6 → 83; T7 verifies 83.

---

## Task 1: UserPrefs — ThemePref enum + two fields + writers (TDD)

**Files:** Modify `app/src/main/java/com/myniyam/app/data/UserPrefs.kt`, `app/src/test/java/com/myniyam/app/data/UserPrefsTest.kt`.

Spec §4: `themePref: ThemePref` (enum LIGHT/DARK/SYSTEM, **default LIGHT**) and `notifyOnCompletion: Boolean` (**default true**); writers `setThemePref`, `setNotifyOnCompletion`. Round-trip by enum `name`, exactly like `selectedIntention` already does. Unknown stored theme on downgrade → default LIGHT (handled by the same `firstOrNull { it.name == raw }` fallback pattern).

- [ ] **1.1** Failing tests — add inside `UserPrefsTest` (the class already imports `assertEquals`/`assertFalse`/`assertTrue`/`Test`):
```kotlin
    @Test
    fun `theme and notify defaults are LIGHT and on`() {
        val s = UserPrefs.Snapshot.DEFAULTS
        assertEquals(ThemePref.LIGHT, s.themePref)
        assertTrue(s.notifyOnCompletion)
    }

    @Test
    fun `themePref round-trips by name`() {
        ThemePref.entries.forEach { pref ->
            val s = UserPrefs.Snapshot.fromRaw(null, null, null, null, themePref = pref.name)
            assertEquals(pref, s.themePref)
        }
    }

    @Test
    fun `unknown themePref falls back to LIGHT`() {
        val s = UserPrefs.Snapshot.fromRaw(null, null, null, null, themePref = "NEON")
        assertEquals(ThemePref.LIGHT, s.themePref)
    }

    @Test
    fun `notifyOnCompletion round-trips and defaults true when null`() {
        assertFalse(
            UserPrefs.Snapshot.fromRaw(null, null, null, null, notifyOnCompletion = false).notifyOnCompletion
        )
        assertTrue(
            UserPrefs.Snapshot.fromRaw(null, null, null, null, notifyOnCompletion = null).notifyOnCompletion
        )
    }
```
- [ ] **1.2** Run the focused class → FAILS to compile (`ThemePref` unresolved, `fromRaw` has no `themePref`/`notifyOnCompletion` params). Command:
```bash
./gradlew test --tests "com.myniyam.app.data.UserPrefsTest"
```
Expected: compilation failure referencing `ThemePref` / unknown named arguments.
- [ ] **1.3** Add the enum at the bottom of `UserPrefs.kt` (after the `object UserPrefs { … }` closing brace), top-level in the same file:
```kotlin
/** Appearance preference (spec §2). Default LIGHT (Pranav's ruling). */
enum class ThemePref { LIGHT, DARK, SYSTEM }
```
- [ ] **1.4** Add the two DataStore keys alongside the existing keys (after `KEY_PENDING_CELEBRATION`):
```kotlin
    private val KEY_THEME_PREF = stringPreferencesKey("theme_pref")
    private val KEY_NOTIFY_ON_COMPLETION = booleanPreferencesKey("notify_on_completion")
```
- [ ] **1.5** Add the two fields to `data class Snapshot(...)` (after `pendingCelebration: Boolean`):
```kotlin
        val themePref: ThemePref,
        val notifyOnCompletion: Boolean
```
- [ ] **1.6** Add their defaults to `DEFAULTS` (after `pendingCelebration = false`):
```kotlin
                themePref = ThemePref.LIGHT,
                notifyOnCompletion = true
```
- [ ] **1.7** Extend `fromRaw` — add the two params at the end of its signature (after `pendingCelebration: Boolean? = null`):
```kotlin
                themePref: String? = null,
                notifyOnCompletion: Boolean? = null
```
and the two mappings inside its `Snapshot(...)` body (after the `pendingCelebration = …` line):
```kotlin
                themePref = themePref?.let { raw ->
                    ThemePref.entries.firstOrNull { it.name == raw }
                } ?: DEFAULTS.themePref,
                notifyOnCompletion = notifyOnCompletion ?: DEFAULTS.notifyOnCompletion
```
- [ ] **1.8** Read both keys in `ensureLoaded` — add to the `Snapshot.fromRaw(...)` call (after `pendingCelebration = p[KEY_PENDING_CELEBRATION]`):
```kotlin
                    themePref = p[KEY_THEME_PREF],
                    notifyOnCompletion = p[KEY_NOTIFY_ON_COMPLETION]
```
- [ ] **1.9** Add the two writers (place after `setIntention`, before `markCompleted`). `setThemePref` ALSO updates the live mirror — but `ThemeState` does not exist until Task 2. To keep T1 self-contained and green, write the persistence half now and add the mirror line in Task 2.5:
```kotlin
    suspend fun setThemePref(context: Context, pref: ThemePref) {
        context.niyamDataStore.edit { it[KEY_THEME_PREF] = pref.name }
        current = current.copy(themePref = pref)
    }

    suspend fun setNotifyOnCompletion(context: Context, enabled: Boolean) {
        context.niyamDataStore.edit { it[KEY_NOTIFY_ON_COMPLETION] = enabled }
        current = current.copy(notifyOnCompletion = enabled)
    }
```
(Implementer: `booleanPreferencesKey` and `stringPreferencesKey` are already imported at the top of the file — no new imports needed.)
- [ ] **1.10** Run focused class → **4/4 new pass** (UserPrefsTest = 11 total). Then full suite:
```bash
./gradlew test --rerun-tasks
```
Expected: **80 tests, 0 failures** (76 + 4). Confirm the count from `app/build/test-results/testDebugUnitTest/*.xml` (sum of `tests=` attributes) if the console summary is ambiguous.
- [ ] **1.11** Commit + push:
```bash
git add app/src/main/java/com/myniyam/app/data/UserPrefs.kt app/src/test/java/com/myniyam/app/data/UserPrefsTest.kt
git commit -m "feat(prefs): ThemePref + notifyOnCompletion — fields, writers, round-trips

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
git push origin main
```

## Task 2: Dark tokens + ThemeState + live theme resolution

**Files:** Create `app/src/main/java/com/myniyam/app/ui/theme/ThemeState.kt`; Modify `app/src/main/java/com/myniyam/app/ui/theme/Color.kt`, `app/src/main/java/com/myniyam/app/ui/theme/Theme.kt`, `app/src/main/java/com/myniyam/app/MainActivity.kt`.

Spec §2 dark palette (orange untouched): bg `#0A2A20`, card `#11352A`, hairline `#1E4438`, ink `#F5EBE1`, inkMuted `#B9C9C2`, labelMuted `#7E948B`, chipFill `#1A3F33`, dark orangeTint `#3A2415`. `PumpkinOrange`/`SaladGreen` unchanged. No unit tests (pure Compose theming verified by build + emulator).

- [ ] **2.1** `Color.kt` — append the dark tokens below the existing light tokens:
```kotlin

// Dark theme tokens (SP-7 spec §2) — bottle-green inversion, orange untouched.
val DarkBg = Color(0xFF0A2A20)
val DarkCard = Color(0xFF11352A)
val DarkHairline = Color(0xFF1E4438)
val DarkInk = Color(0xFFF5EBE1)
val DarkInkMuted = Color(0xFFB9C9C2)
val DarkLabelMuted = Color(0xFF7E948B)
val DarkChipFill = Color(0xFF1A3F33)
val DarkOrangeTint = Color(0xFF3A2415)
```
- [ ] **2.2** Create `ThemeState.kt` — a tiny snapshot-backed mirror MainActivity/NiyamTheme observe so `setThemePref` re-themes live:
```kotlin
package com.myniyam.app.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.myniyam.app.data.ThemePref

/**
 * Compose-observable mirror of UserPrefs.themePref (SP-7). The DataStore
 * write in UserPrefs.setThemePref is the source of truth; this mirror is
 * updated alongside it so NiyamTheme recomposes the instant the toggle flips.
 * MainActivity seeds it from the snapshot after ensureLoaded() on cold start.
 */
object ThemeState {
    var pref: ThemePref by mutableStateOf(ThemePref.LIGHT)
        private set

    fun set(value: ThemePref) { pref = value }
}
```
- [ ] **2.3** `Theme.kt` — add the dark scheme and resolve LIGHT/DARK/SYSTEM from `ThemeState`. Replace the whole file with:
```kotlin
package com.myniyam.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.myniyam.app.data.ThemePref

private val NiyamLightColors = lightColorScheme(
    primary = PumpkinOrange,
    onPrimary = Color.White,
    background = Eggshell,
    onBackground = BottleGreen,
    surface = CardWarm,
    onSurface = BottleGreen,
    surfaceVariant = ChipFill,
    onSurfaceVariant = InkMuted,
    secondary = SaladGreen,
    onSecondary = BottleGreen,
    outline = Hairline,
    outlineVariant = Hairline
)

private val NiyamDarkColors = darkColorScheme(
    primary = PumpkinOrange,
    onPrimary = Color.White,
    background = DarkBg,
    onBackground = DarkInk,
    surface = DarkCard,
    onSurface = DarkInk,
    surfaceVariant = DarkChipFill,
    onSurfaceVariant = DarkInkMuted,
    secondary = SaladGreen,
    onSecondary = BottleGreen,
    outline = DarkHairline,
    outlineVariant = DarkHairline
)

@Composable
fun NiyamTheme(content: @Composable () -> Unit) {
    val dark = when (ThemeState.pref) {
        ThemePref.LIGHT -> false
        ThemePref.DARK -> true
        ThemePref.SYSTEM -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (dark) NiyamDarkColors else NiyamLightColors,
        typography = NiyamTypography,
        content = content
    )
}
```
(Implementer notes: `DarkLabelMuted` and `DarkOrangeTint` are defined but not mapped into the M3 scheme — `DarkOrangeTint` is the dark variant of `OrangeTint` used directly by selected `SelectableCard`/`FilterChip` states in Tasks 4–5 via a theme-aware helper, and `DarkLabelMuted` is available for any caption that needs the dimmer ink. Both are reachable as top-level tokens; leaving them unmapped here is intentional, not a gap. `SelectableCard` currently hardcodes the light `OrangeTint` for its selected fill — that is a SHARED onboarding component; do NOT edit it in this task. If the eyeball veto later flags the onboarding card as too bright in dark mode, that is a separate, approval-gated change, not part of SP-7.)
- [ ] **2.4** `MainActivity.kt` — seed `ThemeState` from the loaded snapshot before `setContent`. Replace the body of `onCreate`:
```kotlin
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UserPrefs.ensureLoaded(this)
        ThemeState.set(UserPrefs.snapshot().themePref)
        val start = if (UserPrefs.snapshot().onboardingComplete) NiyamRoutes.HOME else NiyamRoutes.WELCOME
        setContent {
            NiyamTheme {
                AppNavHost(startDestination = start)
            }
        }
    }
```
and add the import `import com.myniyam.app.ui.theme.ThemeState`.
- [ ] **2.5** `UserPrefs.setThemePref` — add the live-mirror line so the toggle applies instantly. Edit the writer added in Task 1.9 to:
```kotlin
    suspend fun setThemePref(context: Context, pref: ThemePref) {
        context.niyamDataStore.edit { it[KEY_THEME_PREF] = pref.name }
        current = current.copy(themePref = pref)
        com.myniyam.app.ui.theme.ThemeState.set(pref)
    }
```
(Implementer note: fully-qualified to avoid an import that would couple the data layer to the UI package at the top of the file; this single reference is acceptable and keeps the diff minimal. `ThemeState.set` only touches a Compose `mutableStateOf` — safe to call from a coroutine on the main dispatcher, which is where settings writers run.)
- [ ] **2.6** Build + suite (no new tests, expect unchanged count):
```bash
./gradlew assembleDebug
./gradlew test --rerun-tasks
```
Expected: assembleDebug **BUILD SUCCESSFUL**; suite **80 tests, 0 failures**.
- [ ] **2.7** Commit + push:
```bash
git add app/src/main/java/com/myniyam/app/ui/theme/ThemeState.kt app/src/main/java/com/myniyam/app/ui/theme/Color.kt app/src/main/java/com/myniyam/app/ui/theme/Theme.kt app/src/main/java/com/myniyam/app/MainActivity.kt app/src/main/java/com/myniyam/app/data/UserPrefs.kt
git commit -m "feat(theme): dark bottle-green scheme + live ThemeState resolution

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
git push origin main
```

## Task 3: CompletionNotifier + maybeComplete hook (TDD)

**Files:** Create `app/src/main/java/com/myniyam/app/notifications/CompletionNotifier.kt`, `app/src/test/java/com/myniyam/app/notifications/CompletionNotifierTest.kt`; Modify `app/src/main/java/com/myniyam/app/progress/ProgressRepository.kt`, `app/src/main/java/com/myniyam/app/NiyamApplication.kt`.

Spec §2/§4/§5: channel `niyam_completion`; posted by `maybeComplete` right after `markCompleted`, only if `notifyOnCompletion` is ON **and** `POST_NOTIFICATIONS` is granted; tap → MainActivity → HOME → existing `pendingCelebration` LaunchedEffect routes to Celebration; one notification per completion (fired inside the once-only path — no dedup). The guard (`shouldPost`) is pure and unit-tested; the system post is wrapped in try/catch in `maybeComplete`.

- [ ] **3.1** Failing test — `CompletionNotifierTest.kt`:
```kotlin
package com.myniyam.app.notifications

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompletionNotifierTest {

    @Test
    fun `posts when toggle on and permission granted`() {
        assertTrue(CompletionNotifier.shouldPost(notifyOn = true, permissionGranted = true))
    }

    @Test
    fun `no post when toggle off`() {
        assertFalse(CompletionNotifier.shouldPost(notifyOn = false, permissionGranted = true))
    }

    @Test
    fun `no post when permission denied`() {
        assertFalse(CompletionNotifier.shouldPost(notifyOn = true, permissionGranted = false))
    }
}
```
- [ ] **3.2** Run → FAILS (`CompletionNotifier` unresolved):
```bash
./gradlew test --tests "com.myniyam.app.notifications.CompletionNotifierTest"
```
- [ ] **3.3** Implementation — `CompletionNotifier.kt`:
```kotlin
package com.myniyam.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.myniyam.app.MainActivity
import com.myniyam.app.R

/**
 * Posts a one-shot "journey complete" notification (SP-7 spec §2). The
 * decision is pure (shouldPost) and unit-tested; the Android post is isolated
 * in notifyCompletion and only ever called from ProgressRepository inside a
 * try/catch, so a notification failure can never break a recorded read.
 */
object CompletionNotifier {

    const val CHANNEL_ID = "niyam_completion"
    private const val NOTIFICATION_ID = 4201
    private const val TAG = "CompletionNotifier"

    /** Pure guard: post only when the user opted in AND the OS grant is present. */
    fun shouldPost(notifyOn: Boolean, permissionGranted: Boolean): Boolean =
        notifyOn && permissionGranted

    /** True on API < 33 (no runtime grant needed) or when POST_NOTIFICATIONS is granted. */
    fun hasPostPermission(context: Context): Boolean =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) true
        else ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

    fun registerChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Journey complete",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifies you when a sadhana journey reaches its goal."
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    /** Builds + posts the completion notification. Caller guards with shouldPost. */
    fun notifyCompletion(context: Context, mantraName: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.star_on)
            .setContentTitle(context.getString(R.string.notif_completion_title))
            .setContentText(context.getString(R.string.notif_completion_body_fmt, mantraName))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Permission revoked between the guard check and the post — swallow.
            Log.w(TAG, "notify failed (permission)", e)
        }
    }
}
```
(Implementer notes: `androidx.core:core-ktx` is already a dependency — `NotificationCompat`, `NotificationManagerCompat`, `ContextCompat` resolve without new gradle entries. `android.R.drawable.star_on` is a placeholder icon mirroring the app's existing `@android:drawable` icon choice in the manifest; a branded mono icon is a later cosmetic pass, out of SP-7 scope. The two strings `notif_completion_title` / `notif_completion_body_fmt` are added in Task 4.1 — if you build this task before Task 4, add those two strings here instead and remove the dup in 4.1.)
- [ ] **3.4** Run focused → **3/3 pass**.
- [ ] **3.5** `NiyamApplication.kt` — register the completion channel alongside the foreground channel. Add a call inside `onCreate` after `registerForegroundServiceChannel()`:
```kotlin
        CompletionNotifier.registerChannel(this)
```
and the import `import com.myniyam.app.notifications.CompletionNotifier`.
- [ ] **3.6** `ProgressRepository.maybeComplete` — fire the notification after `markCompleted`, guarded + try/caught. Replace the `if (ProgressMath.isComplete(...)) { … }` block:
```kotlin
        if (ProgressMath.isComplete(days, mantra.completionThresholdDays)) {
            UserPrefs.markCompleted(context, mantraId)
            try {
                val notifyOn = UserPrefs.snapshot().notifyOnCompletion
                if (CompletionNotifier.shouldPost(notifyOn, CompletionNotifier.hasPostPermission(context))) {
                    CompletionNotifier.notifyCompletion(
                        context,
                        MantraRepository.displayMantra(mantraId).canonicalName
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "completion notification failed", e)
            }
        }
```
and add the import `import com.myniyam.app.notifications.CompletionNotifier` (`MantraRepository`, `UserPrefs`, `Log` are already imported).
- [ ] **3.7** Build + suite:
```bash
./gradlew assembleDebug
./gradlew test --rerun-tasks
```
Expected: assembleDebug **BUILD SUCCESSFUL**; suite **83 tests, 0 failures** (80 + 3).

NOTE: `notif_completion_title` / `notif_completion_body_fmt` must exist for `assembleDebug` here. Add them to `strings.xml` now (they are also listed in 4.1 — add them once, in whichever task you reach first; do not duplicate):
```xml
<!-- Completion notification -->
<string name="notif_completion_title">Journey complete 🎉</string>
<string name="notif_completion_body_fmt">You finished your %1$s sadhana. Tap to celebrate.</string>
```
- [ ] **3.8** Commit + push:
```bash
git add app/src/main/java/com/myniyam/app/notifications/CompletionNotifier.kt app/src/test/java/com/myniyam/app/notifications/CompletionNotifierTest.kt app/src/main/java/com/myniyam/app/progress/ProgressRepository.kt app/src/main/java/com/myniyam/app/NiyamApplication.kt app/src/main/res/values/strings.xml
git commit -m "feat(notify): completion notification — channel, guard, maybeComplete hook

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
git push origin main
```

## Task 4: SettingsScreen + strings + buildConfig

**Files:** Create `app/src/main/java/com/myniyam/app/settings/SettingsScreen.kt`; Modify `app/build.gradle.kts`, `app/src/main/res/values/strings.xml`. (UI protocol: consult ui-ux-pro-max `--domain ux "settings list rows"` + `--stack jetpack-compose`; brand law — orange only on the selected appearance segment and the notification toggle's checked track.)

The version footer needs `BuildConfig.VERSION_NAME`. `buildConfig` is NOT currently enabled (only `compose = true`), so it must be turned on.

- [ ] **4.1** `app/build.gradle.kts` — enable buildConfig:
```kotlin
    buildFeatures {
        compose = true
        buildConfig = true
    }
```
- [ ] **4.2** Strings — add (skip `notif_completion_*` if already added in Task 3.7):
```xml
<!-- Settings -->
<string name="settings_title">Settings</string>
<string name="settings_section_sadhana">Your practice</string>
<string name="settings_row_current_sadhana">Current sadhana</string>
<string name="settings_row_language">Display language</string>
<string name="settings_row_apps">Blocked apps</string>
<string name="settings_row_intention">Intention</string>
<string name="settings_section_app">App</string>
<string name="settings_row_appearance">Appearance</string>
<string name="settings_appearance_light">Light</string>
<string name="settings_appearance_dark">Dark</string>
<string name="settings_appearance_system">System</string>
<string name="settings_row_notify">Completion notification</string>
<string name="settings_notify_caption">Get notified when a 14-day journey completes.</string>
<string name="settings_notify_blocked">Notifications are blocked in system settings.</string>
<string name="settings_apps_count_fmt">%1$d blocked</string>
<string name="settings_version_fmt">Niyam v%1$s</string>
```
- [ ] **4.3** `SettingsScreen.kt`:
```kotlin
package com.myniyam.app.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.myniyam.app.BuildConfig
import com.myniyam.app.R
import com.myniyam.app.data.ThemePref
import com.myniyam.app.data.UserPrefs
import com.myniyam.app.notifications.CompletionNotifier
import com.myniyam.app.ui.theme.OrangeTint
import com.myniyam.app.ui.theme.PumpkinOrange
import com.myniyam.app.ui.theme.ThemeState
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun SettingsScreen(
    onOpenCurrentSadhana: () -> Unit,
    onOpenLanguage: () -> Unit,
    onOpenApps: () -> Unit,
    onOpenIntention: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snap = UserPrefs.snapshot()

    var notifyOn by remember { mutableStateOf(snap.notifyOnCompletion) }
    var permissionGranted by remember { mutableStateOf(CompletionNotifier.hasPostPermission(ctx)) }
    val currentTheme = ThemeState.pref

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
    }

    Scaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(20.dp))

            SectionLabel(stringResource(R.string.settings_section_sadhana))
            NavRow(stringResource(R.string.settings_row_current_sadhana), onOpenCurrentSadhana)
            NavRow(stringResource(R.string.settings_row_language), onOpenLanguage)
            NavRow(
                stringResource(R.string.settings_row_apps),
                onOpenApps,
                trailing = stringResource(R.string.settings_apps_count_fmt, snap.blockedPackages.size)
            )
            NavRow(stringResource(R.string.settings_row_intention), onOpenIntention)

            Spacer(Modifier.height(20.dp))
            SectionLabel(stringResource(R.string.settings_section_app))

            Text(
                stringResource(R.string.settings_row_appearance),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppearanceSegment(
                    label = stringResource(R.string.settings_appearance_light),
                    selected = currentTheme == ThemePref.LIGHT,
                    onClick = { scope.launch { UserPrefs.setThemePref(ctx, ThemePref.LIGHT) } },
                    modifier = Modifier.weight(1f)
                )
                AppearanceSegment(
                    label = stringResource(R.string.settings_appearance_dark),
                    selected = currentTheme == ThemePref.DARK,
                    onClick = { scope.launch { UserPrefs.setThemePref(ctx, ThemePref.DARK) } },
                    modifier = Modifier.weight(1f)
                )
                AppearanceSegment(
                    label = stringResource(R.string.settings_appearance_system),
                    selected = currentTheme == ThemePref.SYSTEM,
                    onClick = { scope.launch { UserPrefs.setThemePref(ctx, ThemePref.SYSTEM) } },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.settings_row_notify), style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        if (!permissionGranted && notifyOn) {
                            stringResource(R.string.settings_notify_blocked)
                        } else {
                            stringResource(R.string.settings_notify_caption)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = notifyOn,
                    onCheckedChange = { wantOn ->
                        notifyOn = wantOn
                        scope.launch { UserPrefs.setNotifyOnCompletion(ctx, wantOn) }
                        if (wantOn &&
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            !CompletionNotifier.hasPostPermission(ctx)
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = PumpkinOrange
                    )
                )
            }

            Spacer(Modifier.height(28.dp))
            Text(
                stringResource(R.string.settings_version_fmt, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun NavRow(label: String, onClick: () -> Unit, trailing: String? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (trailing != null) {
            Text(
                trailing,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text("  ›", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AppearanceSegment(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = ThemeState.pref == ThemePref.DARK ||
        (ThemeState.pref == ThemePref.SYSTEM && androidx.compose.foundation.isSystemInDarkTheme())
    Text(
        label,
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .height(44.dp)
            .background(
                color = if (selected) {
                    if (dark) com.myniyam.app.ui.theme.DarkOrangeTint else OrangeTint
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp),
    )
}
```
(Implementer notes: the `AppearanceSegment` text is centered by wrapping in a Box if needed — if the label sits left-aligned, wrap the inner `Text` in `Box(modifier, contentAlignment = Alignment.Center) { Text(...) }` and move the background/clickable onto the Box. The selected segment uses `DarkOrangeTint` in dark mode and `OrangeTint` in light — brand law: orange wash only on the selected segment, the segment INK is `primary` orange. The notification permission caption shows only when the toggle is ON but the OS grant is missing — matches spec §5. `BuildConfig` is generated under the app's package `com.myniyam.app` once `buildConfig = true` lands in 4.1; a clean build may be needed for the IDE to resolve it but `assembleDebug` generates it regardless.)
- [ ] **4.4** Build + suite:
```bash
./gradlew assembleDebug
./gradlew test --rerun-tasks
```
Expected: **BUILD SUCCESSFUL**; **83 tests, 0 failures**.
- [ ] **4.5** Commit + push:
```bash
git add app/build.gradle.kts app/src/main/java/com/myniyam/app/settings/SettingsScreen.kt app/src/main/res/values/strings.xml
git commit -m "feat(settings): SettingsScreen — rows, appearance, notify toggle, version footer

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
git push origin main
```

## Task 5: Editor screens — language / blocked apps / intention

**Files:** Create `app/src/main/java/com/myniyam/app/settings/{LanguageSettingScreen,BlockedAppsSettingScreen,IntentionSettingScreen}.kt`; Modify `app/src/main/res/values/strings.xml`. (DRY: every editor reuses onboarding's public `SelectableCard`, which lives in `com.myniyam.app.onboarding` and is `fun SelectableCard(text, selected, onClick, supportingText?, trailingChip?)`. Reuse the onboarding catalogs — the language list and app catalog are private to their onboarding files, so re-declare the same data locally rather than touching onboarding code. Brand law: `SelectableCard` already paints selected state with orange border + `OrangeTint`.)

Each editor: pre-select from `UserPrefs.snapshot()`, write on save, then `popBackStack`. The apps editor enforces a non-empty selection (spec §2/§5 — save disabled at zero). Selections are local Compose state; the write happens only on the bottom Save button so a back-press cancels.

- [ ] **5.1** Strings:
```xml
<!-- Settings editors -->
<string name="settings_save">Save</string>
<string name="settings_language_title">Display language</string>
<string name="settings_apps_title">Blocked apps</string>
<string name="settings_apps_empty_hint">Keep at least one app blocked.</string>
<string name="settings_intention_title">Intention</string>
```
- [ ] **5.2** `LanguageSettingScreen.kt`:
```kotlin
package com.myniyam.app.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.myniyam.app.R
import com.myniyam.app.data.DisplayLanguage
import com.myniyam.app.data.UserPrefs
import com.myniyam.app.onboarding.SelectableCard
import kotlinx.coroutines.launch

private val LANGUAGE_LABELS: List<Triple<DisplayLanguage, String, String>> = listOf(
    Triple(DisplayLanguage.ENGLISH, "English", "Roman script"),
    Triple(DisplayLanguage.HINDI, "हिन्दी", "Hindi"),
    Triple(DisplayLanguage.DEVANAGARI_SANSKRIT, "संस्कृतम् — देवनागरी", "Sanskrit (Devanagari)"),
    Triple(DisplayLanguage.MARATHI, "मराठी", "Marathi"),
    Triple(DisplayLanguage.TELUGU, "తెలుగు", "Telugu"),
    Triple(DisplayLanguage.TAMIL, "தமிழ்", "Tamil"),
    Triple(DisplayLanguage.KANNADA, "ಕನ್ನಡ", "Kannada"),
    Triple(DisplayLanguage.BENGALI, "বাংলা", "Bengali"),
    Triple(DisplayLanguage.GUJARATI, "ગુજરાતી", "Gujarati")
)

@Composable
fun LanguageSettingScreen(onSaved: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var selected by remember { mutableStateOf(UserPrefs.snapshot().displayLanguage) }

    Scaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.settings_language_title), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(20.dp))
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                LANGUAGE_LABELS.forEach { (lang, native, caption) ->
                    SelectableCard(
                        text = native,
                        supportingText = caption,
                        selected = selected == lang,
                        onClick = { selected = lang }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    scope.launch {
                        UserPrefs.setDisplayLanguage(ctx, selected)
                        onSaved()
                    }
                },
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(stringResource(R.string.settings_save), style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
```
- [ ] **5.3** `BlockedAppsSettingScreen.kt` (non-empty guard — Save disabled at zero):
```kotlin
package com.myniyam.app.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.myniyam.app.R
import com.myniyam.app.data.UserPrefs
import com.myniyam.app.onboarding.SelectableCard
import kotlinx.coroutines.launch

private val APP_CATALOG: List<Pair<String, String>> = listOf(
    "Instagram" to "com.instagram.android",
    "YouTube" to "com.google.android.youtube",
    "Facebook" to "com.facebook.katana",
    "X" to "com.twitter.android",
    "Reddit" to "com.reddit.frontpage",
    "Snapchat" to "com.snapchat.android",
    "TikTok" to "com.zhiliaoapp.musically"
)

@Composable
fun BlockedAppsSettingScreen(onSaved: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var selected by remember { mutableStateOf(UserPrefs.snapshot().blockedPackages) }

    Scaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.settings_apps_title), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.settings_apps_empty_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                APP_CATALOG.forEach { (name, pkg) ->
                    SelectableCard(
                        text = name,
                        selected = pkg in selected,
                        onClick = {
                            selected = if (pkg in selected) selected - pkg else selected + pkg
                        }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    scope.launch {
                        UserPrefs.setBlockedPackages(ctx, selected)
                        onSaved()
                    }
                },
                enabled = selected.isNotEmpty(),
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(stringResource(R.string.settings_save), style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
```
- [ ] **5.4** `IntentionSettingScreen.kt`:
```kotlin
package com.myniyam.app.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.myniyam.app.R
import com.myniyam.app.data.Intention
import com.myniyam.app.data.UserPrefs
import com.myniyam.app.onboarding.SelectableCard
import kotlinx.coroutines.launch

@Composable
fun IntentionSettingScreen(onSaved: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var selected by remember { mutableStateOf(UserPrefs.snapshot().selectedIntention) }

    val options = listOf(
        Intention.FOCUS to stringResource(R.string.onb_intention_focus),
        Intention.CALM to stringResource(R.string.onb_intention_calm),
        Intention.SADHANA to stringResource(R.string.onb_intention_sadhana),
        Intention.DHARMA to stringResource(R.string.onb_intention_dharma),
        Intention.DEVOTION to stringResource(R.string.onb_intention_devotion)
    )

    Scaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.settings_intention_title), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(20.dp))
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                options.forEach { (intention, label) ->
                    SelectableCard(
                        text = label,
                        selected = selected == intention,
                        onClick = { selected = intention }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    scope.launch {
                        UserPrefs.setIntention(ctx, selected)
                        onSaved()
                    }
                },
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(stringResource(R.string.settings_save), style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
```
(Implementer note: the intention string resource ids `onb_intention_focus`/`_calm`/`_sadhana`/`_dharma`/`_devotion` already exist — reused from onboarding, no new strings needed for the intention labels.)
- [ ] **5.5** Build + suite:
```bash
./gradlew assembleDebug
./gradlew test --rerun-tasks
```
Expected: **BUILD SUCCESSFUL**; **83 tests, 0 failures**.
- [ ] **5.6** Commit + push:
```bash
git add app/src/main/java/com/myniyam/app/settings/ app/src/main/res/values/strings.xml
git commit -m "feat(settings): language, blocked-apps, intention editors — reuse SelectableCard

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
git push origin main
```

## Task 6: Routes + Home gear icon

**Files:** Modify `app/src/main/java/com/myniyam/app/ui/AppNavHost.kt`, `app/src/main/java/com/myniyam/app/ui/screens/HomeScreen.kt`, `app/src/main/res/values/strings.xml`.

The current-sadhana settings row navigates to the existing `LIBRARY` route (no second picker). The gear sits top-right of Home with `BottleGreen` tint (light) — use `MaterialTheme.colorScheme.onSurface` so it reads correctly in dark mode too. Keep the protection row bottom-anchored (the existing `Spacer(Modifier.weight(1f))` before it stays).

- [ ] **6.1** String: `<string name="settings_content_desc">Settings</string>`
- [ ] **6.2** `AppNavHost.kt` — add four route constants to `NiyamRoutes`:
```kotlin
    const val SETTINGS = "settings"
    const val SETTINGS_LANGUAGE = "settings_language"
    const val SETTINGS_APPS = "settings_apps"
    const val SETTINGS_INTENTION = "settings_intention"
```
Add `onOpenSettings` to the HOME composable's `HomeScreen(...)` call:
```kotlin
            HomeScreen(
                onFixProtection = { navController.navigate(NiyamRoutes.PERMISSION_USAGE) },
                onBrowseLibrary = { navController.navigate(NiyamRoutes.LIBRARY) },
                onOpenSettings = { navController.navigate(NiyamRoutes.SETTINGS) }
            )
```
Add the four composables (place after the `MANTRA_DETAIL` composable, before the NavHost closing brace):
```kotlin
        composable(NiyamRoutes.SETTINGS) {
            SettingsScreen(
                onOpenCurrentSadhana = { navController.navigate(NiyamRoutes.LIBRARY) },
                onOpenLanguage = { navController.navigate(NiyamRoutes.SETTINGS_LANGUAGE) },
                onOpenApps = { navController.navigate(NiyamRoutes.SETTINGS_APPS) },
                onOpenIntention = { navController.navigate(NiyamRoutes.SETTINGS_INTENTION) }
            )
        }
        composable(NiyamRoutes.SETTINGS_LANGUAGE) {
            LanguageSettingScreen(onSaved = { navController.popBackStack() })
        }
        composable(NiyamRoutes.SETTINGS_APPS) {
            BlockedAppsSettingScreen(onSaved = { navController.popBackStack() })
        }
        composable(NiyamRoutes.SETTINGS_INTENTION) {
            IntentionSettingScreen(onSaved = { navController.popBackStack() })
        }
```
Add the imports:
```kotlin
import com.myniyam.app.settings.SettingsScreen
import com.myniyam.app.settings.LanguageSettingScreen
import com.myniyam.app.settings.BlockedAppsSettingScreen
import com.myniyam.app.settings.IntentionSettingScreen
```
- [ ] **6.3** `HomeScreen.kt` — add `onOpenSettings: () -> Unit` to the signature:
```kotlin
fun HomeScreen(onFixProtection: () -> Unit, onBrowseLibrary: () -> Unit, onOpenSettings: () -> Unit) {
```
Add the gear as a top-right `IconButton` on the same row as the overline. Replace the existing overline `Text(...)` block (the `home_overline` Text) with a Row holding the overline + a spacer-weighted gear:
```kotlin
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.home_overline).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings_content_desc),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
```
Add the imports:
```kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
```
(Implementer notes: `Alignment` is already imported in HomeScreen; `Row` is already imported. The gear tint uses `onSurface` (= `BottleGreen` in light, `DarkInk` in dark) — spec says "BottleGreen tint" which is exactly `onSurface` in the light scheme, and stays legible in dark. The IconButton's 48dp touch target sits in the top region above the mantra card; the leading `Spacer(Modifier.height(24.dp))` may be reduced to `8.dp` if the header feels tall — cosmetic, implementer's call. Material icons-extended is NOT a separate dependency need: `Icons.Default.Settings` and `Icons.Default.Check` (already used in OnboardingScaffold) both ship in the core `material-icons-core` that `material3` pulls in — confirm `Icons.Default.Settings` resolves at build; if not, it lives in `androidx.compose.material:material-icons-extended` which would need adding to gradle — but `Settings` is a core icon, so this should resolve without change.)
- [ ] **6.4** Build + suite:
```bash
./gradlew assembleDebug
./gradlew test --rerun-tasks
```
Expected: **BUILD SUCCESSFUL**; **83 tests, 0 failures**.
- [ ] **6.5** Commit + push:
```bash
git add app/src/main/java/com/myniyam/app/ui/AppNavHost.kt app/src/main/java/com/myniyam/app/ui/screens/HomeScreen.kt app/src/main/res/values/strings.xml
git commit -m "feat(settings): routes + Home gear icon — full settings surface wired

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
git push origin main
```

## Task 7: Verification + SP-7 acceptance report

**Files:** Create `docs/superpowers/test-reports/2026-06-11-sp7-acceptance.md`; Modify `SESSION_LOG.md`.

- [ ] **7.1** Gates:
```bash
./gradlew test --rerun-tasks      # expect 83 tests, 0 failures
./gradlew assembleDebug           # expect BUILD SUCCESSFUL
git diff f210c40..HEAD --name-only | grep -E 'service/|OverlayManager|BlockList|res/layout|overlay' || echo "ZERO ENGINE FILES TOUCHED"
```
The grep MUST print `ZERO ENGINE FILES TOUCHED` (spec §6 acceptance #2). If it prints any path, the engine-isolation gate has failed — stop and investigate before reporting.
- [ ] **7.2** Confirm the test count by summing `tests=` across `app/build/test-results/testDebugUnitTest/*.xml`; record the exact number in the report.
- [ ] **7.3** Emulator walkthrough (best-effort, rides the standing session) per spec §6.3: gear → settings → change language → overlay shows new script; toggle dark → app re-themes live; un-block an app → it opens freely; complete a journey → notification arrives → tap → Celebration. Record pass/defer per step.
- [ ] **7.4** Write the acceptance report scoring spec §6 criteria (unit tests, build, zero engine diff, emulator steps, brand/dark eyeball status). Add a `SESSION_LOG.md` entry "SP-7 EXECUTED".
- [ ] **7.5** Commit + push:
```bash
git add docs/superpowers/test-reports/2026-06-11-sp7-acceptance.md SESSION_LOG.md
git commit -m "docs: SP-7 acceptance report — settings, notification, dark mode

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
git push origin main
```

---

## Self-review

**Spec coverage (§ → task):** §2 Settings entry (gear, BottleGreen) → T6.3; Editable items (sadhana→Library, language, apps, intention, appearance, notify, version footer) → T4 + T5 + T6.2 (sadhana row → LIBRARY route, no second picker ✓ DRY); Language change immediate → T5.2 writes `setDisplayLanguage` (overlay reads volatile snapshot, untouched); Blocked-apps non-empty guard → T5.3 (`enabled = selected.isNotEmpty()`); Intention future-only → T5.4 writes `setIntention` (no journey reset); Appearance LIGHT/DARK/SYSTEM default LIGHT → T1 (default) + T2 (resolution) + T4 (segments); Completion notification default ON, posted in `maybeComplete`, permission-gated, one-shot, tap→Celebration → T1 (field) + T3 (notifier + hook); Dark palette exact tokens → T2.1/T2.3; Overlay stays light → no overlay/engine files touched (T7.1 gate). §4 Architecture → T1 (prefs), T2 (Theme/ThemeState/Color/MainActivity), T3 (notifier + hook), T4/T5 (screens), T6 (routes). §5 Error handling → T3 (try/catch in maybeComplete + SecurityException swallow in notifier), T4 (permission-denied caption + RequestPermission launcher), T5.3 (empty-set save disabled), T1 (unknown themePref → LIGHT). §6 Acceptance → T1/T3 tests, T7 gates (build, zero engine diff, emulator). No gaps.

**Placeholder scan:** clean. All code blocks are complete and compilable. The two cross-task string notes (`notif_completion_*` listed in both T3.7 and T4.2) are explicitly de-duplicated with "add once" instructions — not a deferral. The `star_on` icon and `DarkLabelMuted`/`DarkOrangeTint` unmapped-token notes are bounded implementer guidance, not TBDs.

**Type consistency:** `ThemePref` (T1) consumed by `ThemeState` (T2), `Theme.kt` (T2), `SettingsScreen` (T4) — same `com.myniyam.app.data.ThemePref`. `fromRaw` new params `themePref: String?`/`notifyOnCompletion: Boolean?` match the test call sites in T1.1 (named args). `UserPrefs.setThemePref(ctx, ThemePref)` / `setNotifyOnCompletion(ctx, Boolean)` / `setDisplayLanguage` / `setBlockedPackages` / `setIntention` signatures match the actual source (verified). `CompletionNotifier.shouldPost(notifyOn, permissionGranted)` matches the test (T3.1) and the call site (T3.6). `SettingsScreen(onOpenCurrentSadhana, onOpenLanguage, onOpenApps, onOpenIntention)` matches the T6.2 call. `LanguageSettingScreen/BlockedAppsSettingScreen/IntentionSettingScreen(onSaved)` match T6.2. `HomeScreen(onFixProtection, onBrowseLibrary, onOpenSettings)` — T6 updates the only call site (AppNavHost HOME). `SelectableCard(text, selected, onClick, supportingText?, trailingChip?)` is public in `com.myniyam.app.onboarding` (verified) and called correctly from all three editors. `BuildConfig.VERSION_NAME` available after T4.1 (`buildConfig = true`). Test math: 76 + 4 (T1) + 3 (T3) = **83** ✓.
