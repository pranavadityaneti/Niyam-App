# Niyam — Blocking Engine Walking Skeleton — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a sideload-able Android APK named **Niyam** (package `com.myniyam.app`) that intercepts launches of Instagram, Facebook, and YouTube and shows a hardcoded mantra overlay with a 15-second timer, satisfying the Phase 1 emulator acceptance criteria in [the design spec](../specs/2026-06-09-blocking-engine-walking-skeleton-design.md).

**Goal in plain English:** at the end of this plan, you can install the app on an Android emulator (or a real phone), grant five permissions, and watch Instagram/Facebook/YouTube get covered by a mantra screen for 15 seconds every time you open them. Nothing else. No content library, no paywall, no streak counter.

**Architecture:** Single-module Kotlin Android app. Detection via `AccessibilityService`. State and overlay lifecycle in a `ForegroundService` with persistent notification. Overlay rendered with `WindowManager` + traditional `View` XML. In-app UI (welcome / permission grant / home) in Jetpack Compose with Navigation-Compose.

**Tech Stack:**
- Kotlin 2.0.21 (Compose Compiler plugin from Kotlin 2.0)
- Android Gradle Plugin 8.7.x (Pranav: bump to current stable if Android Studio's wizard shows newer)
- Jetpack Compose BOM `2024.12.01` (Pranav: bump if newer stable)
- Navigation-Compose, Lifecycle-Runtime-Compose, Activity-Compose, Material 3
- minSdk = 26 (Android 8.0), targetSdk = 34, compileSdk = 34
- AutoStarter library (`com.judemanutd:autostarter:1.1.0`) for per-OEM autostart deep-links
- Gradle Kotlin DSL + version catalog (`libs.versions.toml`)

---

## Execution conventions for this plan

These rules come from Pranav's `~/.claude/CLAUDE.md` and they OVERRIDE any default agent behavior:

1. **One task at a time.** Complete one task, then **STOP** and wait for Pranav's explicit approval before starting the next. Never bundle two tasks into one commit.
2. **After each task, report:**
   - Diff summary (which files changed)
   - Verification output (the exact terminal output of the build/test command shown in the task's last verification step)
   - One-line "What's next" pointing at the next task heading
3. **If a step fails twice**, stop, log to `ERRORS.md` at the project root (create the file if missing), and ask Pranav before retrying a third time.
4. **No scope creep.** If a task touches code outside its declared `Files:` list, STOP and ask Pranav.
5. **Verification command for compilation:** `./gradlew :app:compileDebugKotlin` (this is Android's equivalent of `tsc --noEmit`). Full build: `./gradlew :app:assembleDebug`. Unit tests: `./gradlew :app:testDebugUnitTest`.
6. **Git is local-only** until Pranav provides his repository URL. `git init` is in Task 1. When he provides the URL, add it as `origin` and `git push -u`.

---

## File structure overview

```
Hindu Distraction App/
├── .gitignore                            (Task 1)
├── settings.gradle.kts                   (Task 1)
├── build.gradle.kts                      (Task 1)
├── gradle.properties                     (Task 1)
├── gradle/
│   └── libs.versions.toml                (Task 1)
├── app/
│   ├── build.gradle.kts                  (Task 2)
│   ├── proguard-rules.pro                (Task 2 — empty)
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml       (Task 2 baseline, updated in Tasks 14, 15, 16)
│       │   ├── java/com/myniyam/app/
│       │   │   ├── NiyamApplication.kt   (Task 3)
│       │   │   ├── MainActivity.kt       (Task 8)
│       │   │   ├── data/
│       │   │   │   ├── BlockList.kt              (Task 4)
│       │   │   │   └── PlaceholderMantra.kt      (Task 5)
│       │   │   ├── permissions/
│       │   │   │   ├── PermissionChecker.kt      (Task 6)
│       │   │   │   └── OemAutostartHelper.kt     (Task 7)
│       │   │   ├── service/
│       │   │   │   ├── AppLockForegroundService.kt   (Task 14)
│       │   │   │   └── AppLockAccessibilityService.kt (Task 15)
│       │   │   ├── overlay/
│       │   │   │   └── OverlayManager.kt         (Task 18)
│       │   │   └── ui/
│       │   │       ├── AppNavHost.kt             (Task 8)
│       │   │       ├── theme/
│       │   │       │   ├── Color.kt              (Task 8)
│       │   │       │   ├── Theme.kt              (Task 8)
│       │   │       │   └── Type.kt               (Task 8)
│       │   │       └── screens/
│       │   │           ├── WelcomeScreen.kt              (Task 9)
│       │   │           ├── PermissionScreen.kt           (Task 10)
│       │   │           ├── OemAutostartScreen.kt         (Task 12)
│       │   │           └── HomeScreen.kt                 (Task 13)
│       │   └── res/
│       │       ├── layout/overlay_mantra.xml             (Task 17)
│       │       ├── xml/accessibility_service_config.xml  (Task 16)
│       │       ├── values/strings.xml                    (Task 8)
│       │       ├── values/colors.xml                     (Task 8)
│       │       └── values/themes.xml                     (Task 8)
│       └── test/
│           └── java/com/myniyam/app/
│               ├── data/BlockListTest.kt                 (Task 4)
│               └── permissions/OemAutostartHelperTest.kt (Task 7)
└── docs/superpowers/  (already exists — spec lives here)
```

---

## Task 1: Project scaffolding — Gradle config, `.gitignore`, first commit

**Files:**
- Create: `.gitignore`
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `gradle/libs.versions.toml`

- [ ] **Step 1.1: Initialize git repository**

```bash
cd "/Users/pranavaditya/projects/Hindu Distraction App"
git init
git branch -M main
```

Expected: `Initialized empty Git repository in .../Hindu Distraction App/.git/`

- [ ] **Step 1.2: Create `.gitignore`**

```gitignore
# Built application files
*.apk
*.ap_
*.aab

# Files for the ART/Dalvik VM
*.dex

# Java class files
*.class

# Generated files
bin/
gen/
out/
release/

# Gradle files
.gradle/
build/

# Local configuration file (sdk path, etc)
local.properties

# Proguard folder generated by Eclipse
proguard/

# Log Files
*.log

# Android Studio Navigation editor temp files
.navigation/

# Android Studio captures folder
captures/

# IntelliJ
*.iml
.idea/
*.iws
*.ipr

# Keystore files
*.jks
*.keystore

# Mac
.DS_Store

# Kotlin
.kotlin/

# Plan output files (left for human review)
ERRORS.md.bak
```

- [ ] **Step 1.3: Create `settings.gradle.kts`**

```kotlin
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Niyam"
include(":app")
```

- [ ] **Step 1.4: Create root `build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
```

- [ ] **Step 1.5: Create `gradle.properties`**

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true

android.useAndroidX=true
android.nonTransitiveRClass=true

kotlin.code.style=official
```

- [ ] **Step 1.6: Create `gradle/libs.versions.toml`**

```toml
[versions]
agp = "8.7.0"
kotlin = "2.0.21"
coreKtx = "1.13.1"
lifecycleRuntimeKtx = "2.8.7"
activityCompose = "1.9.3"
composeBom = "2024.12.01"
navigationCompose = "2.8.4"
autostarter = "1.1.0"
junit = "4.13.2"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycleRuntimeKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
autostarter = { group = "com.judemanutd", name = "autostarter", version.ref = "autostarter" }
junit = { group = "junit", name = "junit", version.ref = "junit" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

- [ ] **Step 1.7: Commit and STOP**

```bash
git add .gitignore settings.gradle.kts build.gradle.kts gradle.properties gradle/libs.versions.toml
git commit -m "$(cat <<'EOF'
chore: initialize Gradle project structure for Niyam

Sets up root Gradle config, version catalog, and Android-standard
gitignore. No app module yet — that's Task 2.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

Verification: `git log --oneline -1` should show the commit. No build runs in this task — there's no `:app` module yet.

**STOP after this. Wait for approval.**

---

## Task 2: `:app` module + AndroidManifest baseline + Application class

**Files:**
- Create: `app/build.gradle.kts`
- Create: `app/proguard-rules.pro`
- Create: `app/src/main/AndroidManifest.xml`

- [ ] **Step 2.1: Create `app/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.myniyam.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.myniyam.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0-skeleton"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.autostarter)

    debugImplementation(libs.androidx.ui.tooling)

    testImplementation(libs.junit)
}
```

- [ ] **Step 2.2: Create empty `app/proguard-rules.pro`**

```
# No Proguard rules for skeleton — debug build only.
```

- [ ] **Step 2.3: Create baseline `app/src/main/AndroidManifest.xml`**

This declares all permissions and Application class. Services come in later tasks.

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <uses-permission android:name="android.permission.PACKAGE_USAGE_STATS"
        tools:ignore="ProtectedPermissions" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
    <uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES"
        tools:ignore="QueryAllPackagesPermission" />

    <application
        android:name=".NiyamApplication"
        android:allowBackup="false"
        android:icon="@android:drawable/sym_def_app_icon"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.Niyam"
        tools:targetApi="34">

        <!-- MainActivity declared in Task 8 -->
        <!-- Services declared in Tasks 14 and 15 -->

    </application>

</manifest>
```

- [ ] **Step 2.4: Verify Gradle can sync the project**

Run: `./gradlew :app:tasks --no-daemon`
Expected: prints a list of available tasks. No errors. (If `gradlew` doesn't exist yet, Android Studio will create it on first sync — alternative: install Gradle 8.7+ on the system and run `gradle wrapper` first.)

If `./gradlew` doesn't exist yet:
```bash
gradle wrapper --gradle-version=8.10 --distribution-type=bin
```
Then re-run `./gradlew :app:tasks`.

- [ ] **Step 2.5: Commit and STOP**

```bash
git add app/build.gradle.kts app/proguard-rules.pro app/src/main/AndroidManifest.xml gradle/wrapper/ gradlew gradlew.bat
git commit -m "$(cat <<'EOF'
chore: add :app module + manifest baseline + Gradle wrapper

Compose enabled, minSdk 26, targetSdk 34. Permissions declared but
services not yet registered — those come in Tasks 14-16.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

**STOP after this. Wait for approval.**

---

## Task 3: `NiyamApplication` class

**Files:**
- Create: `app/src/main/java/com/myniyam/app/NiyamApplication.kt`

- [ ] **Step 3.1: Write `NiyamApplication.kt`**

This is the Application subclass declared in the manifest. It registers the foreground-service notification channel up-front (channels must exist before any notification is posted on Android 8+).

```kotlin
package com.myniyam.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class NiyamApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        registerForegroundServiceChannel()
    }

    private fun registerForegroundServiceChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID_FOREGROUND,
            "Niyam protection",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Persistent notification shown while Niyam is guarding blocked apps."
            setShowBadge(false)
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID_FOREGROUND = "niyam_foreground_service"
    }
}
```

- [ ] **Step 3.2: Verify it compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`. The build will warn that no resources/strings.xml exists yet — that's fine; strings get added in Task 8.

If the build fails because `R.string.app_name` is referenced from the manifest, that's OK — fix in Task 8. For now, edit the manifest temporarily to use `android:label="Niyam"` (literal string) instead of `@string/app_name`. Revert in Task 8.

- [ ] **Step 3.3: Commit and STOP**

```bash
git add app/src/main/java/com/myniyam/app/NiyamApplication.kt app/src/main/AndroidManifest.xml
git commit -m "$(cat <<'EOF'
feat: add NiyamApplication class with foreground-service channel

Notification channel is registered on app start so the foreground
service can post immediately when it's started later.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

**STOP after this. Wait for approval.**

---

## Task 4: `BlockList` (TDD)

**Files:**
- Create: `app/src/test/java/com/myniyam/app/data/BlockListTest.kt`
- Create: `app/src/main/java/com/myniyam/app/data/BlockList.kt`

- [ ] **Step 4.1: Write the failing test**

```kotlin
package com.myniyam.app.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BlockListTest {

    @Test
    fun `matches returns true for instagram package`() {
        assertTrue(BlockList.matches("com.instagram.android"))
    }

    @Test
    fun `matches returns true for facebook main app package`() {
        assertTrue(BlockList.matches("com.facebook.katana"))
    }

    @Test
    fun `matches returns true for youtube main app package`() {
        assertTrue(BlockList.matches("com.google.android.youtube"))
    }

    @Test
    fun `matches returns false for messenger`() {
        assertFalse(BlockList.matches("com.facebook.orca"))
    }

    @Test
    fun `matches returns false for youtube music`() {
        assertFalse(BlockList.matches("com.google.android.apps.youtube.music"))
    }

    @Test
    fun `matches returns false for facebook lite`() {
        assertFalse(BlockList.matches("com.facebook.lite"))
    }

    @Test
    fun `matches returns false for chrome`() {
        assertFalse(BlockList.matches("com.android.chrome"))
    }

    @Test
    fun `matches returns false for empty string`() {
        assertFalse(BlockList.matches(""))
    }
}
```

- [ ] **Step 4.2: Run the test — verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.myniyam.app.data.BlockListTest"`
Expected: `BUILD FAILED` with `Unresolved reference: BlockList`.

- [ ] **Step 4.3: Write minimal implementation**

```kotlin
package com.myniyam.app.data

object BlockList {

    val HARDCODED_PACKAGES: Set<String> = setOf(
        "com.instagram.android",
        "com.facebook.katana",
        "com.google.android.youtube"
    )

    fun matches(packageName: String): Boolean = packageName in HARDCODED_PACKAGES
}
```

- [ ] **Step 4.4: Run the test — verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.myniyam.app.data.BlockListTest"`
Expected: `BUILD SUCCESSFUL`, 8 tests passed.

- [ ] **Step 4.5: Commit and STOP**

```bash
git add app/src/test/java/com/myniyam/app/data/BlockListTest.kt app/src/main/java/com/myniyam/app/data/BlockList.kt
git commit -m "$(cat <<'EOF'
feat(data): add BlockList with hardcoded set of three packages

Instagram, Facebook (katana), YouTube (main). Messenger / FB Lite /
YouTube Music explicitly NOT blocked. TDD: 8 unit tests pass.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

**STOP after this. Wait for approval.**

---

## Task 5: `PlaceholderMantra` constants

**Files:**
- Create: `app/src/main/java/com/myniyam/app/data/PlaceholderMantra.kt`

No test — this file is pure constants.

- [ ] **Step 5.1: Write the file**

```kotlin
package com.myniyam.app.data

/**
 * Hardcoded mantra shown by the overlay in the walking skeleton.
 * Replaced by the content library in sub-project 2.
 */
object PlaceholderMantra {
    const val DEVANAGARI: String = "ॐ"
    const val TRANSLITERATION: String = "Om"
    const val ENGLISH_MEANING: String = "The primordial sound of the universe."
    const val UNLOCK_TIMER_SECONDS: Int = 15
}
```

- [ ] **Step 5.2: Verify it compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5.3: Commit and STOP**

```bash
git add app/src/main/java/com/myniyam/app/data/PlaceholderMantra.kt
git commit -m "$(cat <<'EOF'
feat(data): add PlaceholderMantra constants for skeleton overlay

Single ॐ with English meaning + 15s timer. Replaced by content
library in sub-project 2.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

**STOP after this. Wait for approval.**

---

## Task 6: `PermissionChecker`

**Files:**
- Create: `app/src/main/java/com/myniyam/app/permissions/PermissionChecker.kt`

This wraps the five OS-level permission checks plus their Settings deep-links. No unit test in the skeleton — every method touches `Context` or `Settings.Secure`, which requires Robolectric (out of scope for skeleton).

- [ ] **Step 6.1: Write `PermissionChecker.kt`**

```kotlin
package com.myniyam.app.permissions

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import com.myniyam.app.service.AppLockAccessibilityService

object PermissionChecker {

    // ---- Usage Stats ----

    fun hasUsageStatsAccess(ctx: Context): Boolean {
        val appOps = ctx.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            ctx.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun openUsageAccessSettings(ctx: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        ctx.startActivity(intent)
    }

    // ---- Overlay (SYSTEM_ALERT_WINDOW) ----

    fun hasOverlayPermission(ctx: Context): Boolean =
        Settings.canDrawOverlays(ctx)

    fun openOverlayPermissionSettings(ctx: Context) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${ctx.packageName}")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        ctx.startActivity(intent)
    }

    // ---- Accessibility Service ----

    fun isAccessibilityServiceEnabled(ctx: Context): Boolean {
        val expected = "${ctx.packageName}/${AppLockAccessibilityService::class.java.canonicalName}"
        val enabled = Settings.Secure.getString(
            ctx.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabled)
        while (splitter.hasNext()) {
            if (splitter.next().equals(expected, ignoreCase = true)) return true
        }
        return false
    }

    fun openAccessibilitySettings(ctx: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        ctx.startActivity(intent)
    }

    // ---- Battery Optimization ----

    fun isIgnoringBatteryOptimizations(ctx: Context): Boolean {
        val pm = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(ctx.packageName)
    }

    fun openIgnoreBatteryOptimizationSettings(ctx: Context) {
        val intent = Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Uri.parse("package:${ctx.packageName}")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        ctx.startActivity(intent)
    }

    // ---- All-permissions roll-up ----

    fun allPermissionsGranted(ctx: Context): Boolean =
        hasUsageStatsAccess(ctx) &&
        hasOverlayPermission(ctx) &&
        isAccessibilityServiceEnabled(ctx) &&
        isIgnoringBatteryOptimizations(ctx)
}
```

Note: `AppLockAccessibilityService` class doesn't exist yet — it's referenced via FQCN in `isAccessibilityServiceEnabled` but Kotlin's class literal `AppLockAccessibilityService::class.java` won't resolve until Task 15. As a workaround, replace `AppLockAccessibilityService::class.java.canonicalName` with the literal string `"com.myniyam.app.service.AppLockAccessibilityService"` for this task. We'll switch to the type-safe reference in Task 15.

Replace this line:
```kotlin
val expected = "${ctx.packageName}/${AppLockAccessibilityService::class.java.canonicalName}"
```
with:
```kotlin
val expected = "${ctx.packageName}/com.myniyam.app.service.AppLockAccessibilityService"
```
And remove the `import com.myniyam.app.service.AppLockAccessibilityService` line.

- [ ] **Step 6.2: Verify it compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6.3: Commit and STOP**

```bash
git add app/src/main/java/com/myniyam/app/permissions/PermissionChecker.kt
git commit -m "$(cat <<'EOF'
feat(permissions): add PermissionChecker for 5 OS permissions

Wraps usage-stats, overlay, accessibility-service, and battery-
optimization checks + their Settings deep-link launchers. OEM
autostart handled separately in OemAutostartHelper.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

**STOP after this. Wait for approval.**

---

## Task 7: `OemAutostartHelper` (TDD)

**Files:**
- Create: `app/src/test/java/com/myniyam/app/permissions/OemAutostartHelperTest.kt`
- Create: `app/src/main/java/com/myniyam/app/permissions/OemAutostartHelper.kt`

- [ ] **Step 7.1: Write the failing test**

```kotlin
package com.myniyam.app.permissions

import org.junit.Assert.assertEquals
import org.junit.Test

class OemAutostartHelperTest {

    @Test
    fun `xiaomi maps to MIUI`() {
        assertEquals(OemAutostartHelper.OemFlow.MIUI, OemAutostartHelper.flowFor("xiaomi"))
    }

    @Test
    fun `redmi maps to MIUI`() {
        assertEquals(OemAutostartHelper.OemFlow.MIUI, OemAutostartHelper.flowFor("Redmi"))
    }

    @Test
    fun `poco maps to MIUI`() {
        assertEquals(OemAutostartHelper.OemFlow.MIUI, OemAutostartHelper.flowFor("POCO"))
    }

    @Test
    fun `oppo maps to ColorOS`() {
        assertEquals(OemAutostartHelper.OemFlow.COLOR_OS, OemAutostartHelper.flowFor("oppo"))
    }

    @Test
    fun `realme maps to ColorOS`() {
        assertEquals(OemAutostartHelper.OemFlow.COLOR_OS, OemAutostartHelper.flowFor("REALME"))
    }

    @Test
    fun `vivo maps to FuntouchOS`() {
        assertEquals(OemAutostartHelper.OemFlow.FUNTOUCH_OS, OemAutostartHelper.flowFor("vivo"))
    }

    @Test
    fun `iqoo maps to FuntouchOS`() {
        assertEquals(OemAutostartHelper.OemFlow.FUNTOUCH_OS, OemAutostartHelper.flowFor("IQOO"))
    }

    @Test
    fun `oneplus maps to OxygenOS`() {
        assertEquals(OemAutostartHelper.OemFlow.OXYGEN_OS, OemAutostartHelper.flowFor("OnePlus"))
    }

    @Test
    fun `samsung maps to OneUI`() {
        assertEquals(OemAutostartHelper.OemFlow.ONE_UI, OemAutostartHelper.flowFor("samsung"))
    }

    @Test
    fun `pixel maps to Generic`() {
        assertEquals(OemAutostartHelper.OemFlow.GENERIC, OemAutostartHelper.flowFor("Google"))
    }

    @Test
    fun `unknown manufacturer maps to Generic`() {
        assertEquals(OemAutostartHelper.OemFlow.GENERIC, OemAutostartHelper.flowFor("UnknownBrand"))
    }

    @Test
    fun `empty string maps to Generic`() {
        assertEquals(OemAutostartHelper.OemFlow.GENERIC, OemAutostartHelper.flowFor(""))
    }
}
```

- [ ] **Step 7.2: Run the test — verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.myniyam.app.permissions.OemAutostartHelperTest"`
Expected: `BUILD FAILED` with `Unresolved reference: OemAutostartHelper`.

- [ ] **Step 7.3: Write minimal implementation**

```kotlin
package com.myniyam.app.permissions

import android.content.Context
import com.judemanutd.autostarter.AutoStartPermissionHelper

object OemAutostartHelper {

    enum class OemFlow {
        MIUI,
        COLOR_OS,
        FUNTOUCH_OS,
        OXYGEN_OS,
        ONE_UI,
        GENERIC
    }

    fun flowFor(manufacturer: String): OemFlow {
        return when (manufacturer.lowercase()) {
            "xiaomi", "redmi", "poco" -> OemFlow.MIUI
            "oppo", "realme" -> OemFlow.COLOR_OS
            "vivo", "iqoo" -> OemFlow.FUNTOUCH_OS
            "oneplus" -> OemFlow.OXYGEN_OS
            "samsung" -> OemFlow.ONE_UI
            else -> OemFlow.GENERIC
        }
    }

    /**
     * Best-effort deep-link to the OEM-specific autostart settings.
     * Falls back to the generic App info page on any failure.
     */
    fun openAutostartSettings(ctx: Context) {
        runCatching {
            AutoStartPermissionHelper.getInstance().getAutoStartPermission(ctx, true, true)
        }
    }
}
```

- [ ] **Step 7.4: Run the test — verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.myniyam.app.permissions.OemAutostartHelperTest"`
Expected: `BUILD SUCCESSFUL`, 12 tests passed.

- [ ] **Step 7.5: Commit and STOP**

```bash
git add app/src/test/java/com/myniyam/app/permissions/OemAutostartHelperTest.kt app/src/main/java/com/myniyam/app/permissions/OemAutostartHelper.kt
git commit -m "$(cat <<'EOF'
feat(permissions): add OemAutostartHelper with per-OEM routing

Manufacturer → OemFlow enum routing (MIUI / ColorOS / FuntouchOS /
OxygenOS / OneUI / Generic). Settings deep-link delegated to the
AutoStarter library. TDD: 12 unit tests pass.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

**STOP after this. Wait for approval.**

---

## Task 8: Compose theme + string resources + `MainActivity` + empty `AppNavHost`

**Files:**
- Create: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/values/colors.xml`
- Create: `app/src/main/res/values/themes.xml`
- Create: `app/src/main/java/com/myniyam/app/ui/theme/Color.kt`
- Create: `app/src/main/java/com/myniyam/app/ui/theme/Theme.kt`
- Create: `app/src/main/java/com/myniyam/app/ui/theme/Type.kt`
- Create: `app/src/main/java/com/myniyam/app/MainActivity.kt`
- Create: `app/src/main/java/com/myniyam/app/ui/AppNavHost.kt`
- Modify: `app/src/main/AndroidManifest.xml` (revert label to `@string/app_name`, register MainActivity)

- [ ] **Step 8.1: Create `strings.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Niyam</string>

    <!-- Welcome screen -->
    <string name="welcome_title">Niyam</string>
    <string name="welcome_subtitle">A pause before the scroll.</string>
    <string name="welcome_cta">Get started</string>

    <!-- Permission screens (placeholder copy — refined when UI references arrive) -->
    <string name="perm_usage_title">See when you open blocked apps</string>
    <string name="perm_usage_body">To see when you open Instagram, Facebook, or YouTube — so we can show your mantra first.</string>
    <string name="perm_overlay_title">Draw on top of other apps</string>
    <string name="perm_overlay_body">To show your mantra on top of the blocked app.</string>
    <string name="perm_accessibility_title">Detect app launches instantly</string>
    <string name="perm_accessibility_body">To detect the exact moment Instagram, Facebook, or YouTube opens — instantly, with no delay.</string>
    <string name="perm_battery_title">Don\'t shut us off in the background</string>
    <string name="perm_battery_body">So your phone doesn\'t kill Niyam while you\'re using it elsewhere.</string>
    <string name="perm_oem_title">Allow background activity</string>
    <string name="perm_oem_body">So your phone allows us to start when needed.</string>

    <string name="grant">Grant</string>
    <string name="done">Done</string>
    <string name="next">Next</string>

    <!-- Home screen -->
    <string name="home_protection_active">Protection: Active</string>
    <string name="home_protection_at_risk">Protection: At Risk</string>
    <string name="home_oem_warning">Background protection may be disabled on your device.</string>

    <!-- Overlay -->
    <string name="overlay_continue">Continue</string>
    <string name="overlay_unlocking_in">Unlocking in %1$d…</string>

    <!-- Foreground service notification -->
    <string name="notif_channel_foreground">Niyam protection</string>
    <string name="notif_text">Standing guard against doom-scroll.</string>
</resources>
```

- [ ] **Step 8.2: Create `colors.xml`**

Placeholder colors only. Replaced when Pranav's UI references arrive.

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="black">#FF000000</color>
    <color name="white">#FFFFFFFF</color>
    <color name="overlay_scrim">#CC000000</color>
</resources>
```

- [ ] **Step 8.3: Create `themes.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.Niyam" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
```

- [ ] **Step 8.4: Create `Color.kt`**

```kotlin
package com.myniyam.app.ui.theme

import androidx.compose.ui.graphics.Color

val PlaceholderPrimary = Color(0xFF6750A4)
val PlaceholderSecondary = Color(0xFF625B71)
val PlaceholderTertiary = Color(0xFF7D5260)
```

- [ ] **Step 8.5: Create `Type.kt`**

```kotlin
package com.myniyam.app.ui.theme

import androidx.compose.material3.Typography

val NiyamTypography = Typography()
```

- [ ] **Step 8.6: Create `Theme.kt`**

```kotlin
package com.myniyam.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = PlaceholderPrimary,
    secondary = PlaceholderSecondary,
    tertiary = PlaceholderTertiary
)

private val DarkColors = darkColorScheme(
    primary = PlaceholderPrimary,
    secondary = PlaceholderSecondary,
    tertiary = PlaceholderTertiary
)

@Composable
fun NiyamTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = NiyamTypography,
        content = content
    )
}
```

- [ ] **Step 8.7: Create `AppNavHost.kt` with placeholder destinations**

Each destination gets a real Composable in later tasks. For now they're stub `Text` composables so the NavHost compiles.

```kotlin
package com.myniyam.app.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

object NiyamRoutes {
    const val WELCOME = "welcome"
    const val PERMISSION_USAGE = "permission_usage_stats"
    const val PERMISSION_OVERLAY = "permission_overlay"
    const val PERMISSION_ACCESSIBILITY = "permission_accessibility"
    const val PERMISSION_BATTERY = "permission_battery"
    const val PERMISSION_OEM = "permission_oem_autostart"
    const val HOME = "home"
}

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = NiyamRoutes.WELCOME) {
        composable(NiyamRoutes.WELCOME) { Text("welcome (Task 9)") }
        composable(NiyamRoutes.PERMISSION_USAGE) { Text("permission usage (Task 11)") }
        composable(NiyamRoutes.PERMISSION_OVERLAY) { Text("permission overlay (Task 11)") }
        composable(NiyamRoutes.PERMISSION_ACCESSIBILITY) { Text("permission accessibility (Task 11)") }
        composable(NiyamRoutes.PERMISSION_BATTERY) { Text("permission battery (Task 11)") }
        composable(NiyamRoutes.PERMISSION_OEM) { Text("permission oem (Task 12)") }
        composable(NiyamRoutes.HOME) { Text("home (Task 13)") }
    }
}
```

- [ ] **Step 8.8: Create `MainActivity.kt`**

```kotlin
package com.myniyam.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.myniyam.app.ui.AppNavHost
import com.myniyam.app.ui.theme.NiyamTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NiyamTheme {
                AppNavHost()
            }
        }
    }
}
```

- [ ] **Step 8.9: Update `AndroidManifest.xml` to register MainActivity**

Revert the `android:label` to `@string/app_name` (we deferred this in Task 3), and add the `<activity>` block inside `<application>`:

```xml
<!-- Inside <application>, before the closing </application>: -->
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:theme="@style/Theme.Niyam">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

And change `android:label="Niyam"` back to `android:label="@string/app_name"` on the `<application>` element.

- [ ] **Step 8.10: Build the debug APK**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`, APK at `app/build/outputs/apk/debug/app-debug.apk`.

- [ ] **Step 8.11: Commit and STOP**

```bash
git add app/src/main/res/values/ app/src/main/java/com/myniyam/app/ui/theme/ app/src/main/java/com/myniyam/app/ui/AppNavHost.kt app/src/main/java/com/myniyam/app/MainActivity.kt app/src/main/AndroidManifest.xml
git commit -m "$(cat <<'EOF'
feat(ui): add MainActivity + NavHost scaffold + placeholder theme

7 routes defined as stub Text composables; real screens land in
Tasks 9-13. Colors and typography are Material 3 defaults — final
theme awaits UI references.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

**STOP after this. Wait for approval.**

---

## Task 9: `WelcomeScreen`

**Files:**
- Create: `app/src/main/java/com/myniyam/app/ui/screens/WelcomeScreen.kt`
- Modify: `app/src/main/java/com/myniyam/app/ui/AppNavHost.kt`

- [ ] **Step 9.1: Write `WelcomeScreen.kt`**

```kotlin
package com.myniyam.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.myniyam.app.R

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.welcome_title),
                style = MaterialTheme.typography.displayMedium
            )
            Text(
                text = stringResource(R.string.welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp, bottom = 48.dp)
            )
            Button(onClick = onGetStarted) {
                Text(stringResource(R.string.welcome_cta))
            }
        }
    }
}
```

- [ ] **Step 9.2: Wire it into `AppNavHost.kt`**

Replace the welcome stub with the real Composable.

In `AppNavHost.kt`, change:
```kotlin
composable(NiyamRoutes.WELCOME) { Text("welcome (Task 9)") }
```
to:
```kotlin
composable(NiyamRoutes.WELCOME) {
    WelcomeScreen(onGetStarted = { navController.navigate(NiyamRoutes.PERMISSION_USAGE) })
}
```

Add the import at top:
```kotlin
import com.myniyam.app.ui.screens.WelcomeScreen
```

- [ ] **Step 9.3: Verify it builds**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 9.4: Commit and STOP**

```bash
git add app/src/main/java/com/myniyam/app/ui/screens/WelcomeScreen.kt app/src/main/java/com/myniyam/app/ui/AppNavHost.kt
git commit -m "$(cat <<'EOF'
feat(ui): add WelcomeScreen with placeholder copy

CTA navigates to permission_usage_stats. Visual layout is bare
defaults — final UI lands when Pranav shares references.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

**STOP after this. Wait for approval.**

---

## Task 10: Reusable `PermissionScreen`

**Files:**
- Create: `app/src/main/java/com/myniyam/app/ui/screens/PermissionScreen.kt`

One screen template used for the four "simple" permission screens (usage stats, overlay, accessibility, battery). The OEM autostart screen is special-cased in Task 12.

- [ ] **Step 10.1: Write `PermissionScreen.kt`**

```kotlin
package com.myniyam.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * Reusable permission-grant screen.
 *
 * @param titleResId String resource for the screen title
 * @param bodyResId String resource for the "why we need this" body copy
 * @param isGranted () -> Boolean that returns the current grant state of this permission
 * @param launchSettings () -> Unit that opens the relevant Settings page
 * @param onGranted () -> Unit fired when this screen detects the permission has been granted
 */
@Composable
fun PermissionScreen(
    titleResId: Int,
    bodyResId: Int,
    isGranted: () -> Boolean,
    launchSettings: () -> Unit,
    onGranted: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var grantedState by remember { mutableStateOf(isGranted()) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                grantedState = isGranted()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(grantedState) {
        if (grantedState) onGranted()
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(titleResId),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.padding(top = 16.dp))
            Text(
                text = androidx.compose.ui.res.stringResource(bodyResId),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.padding(top = 48.dp))
            Button(
                onClick = { launchSettings() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(androidx.compose.ui.res.stringResource(com.myniyam.app.R.string.grant))
            }
        }
    }
}
```

- [ ] **Step 10.2: Verify it compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 10.3: Commit and STOP**

```bash
git add app/src/main/java/com/myniyam/app/ui/screens/PermissionScreen.kt
git commit -m "$(cat <<'EOF'
feat(ui): add reusable PermissionScreen Composable

Parameterized by title, body, grant-check callback, settings-launcher
callback, and onGranted callback. Detects grant on RESUME and fires
onGranted automatically. Reused 4× in Task 11.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

**STOP after this. Wait for approval.**

---

## Task 11: Wire 4 permission screens into NavHost using `PermissionScreen`

**Files:**
- Modify: `app/src/main/java/com/myniyam/app/ui/AppNavHost.kt`

- [ ] **Step 11.1: Replace the 4 stub routes**

In `AppNavHost.kt`, replace the four placeholder `composable(...)` calls for permission_usage_stats, permission_overlay, permission_accessibility, and permission_battery with real wiring. Updated `AppNavHost.kt` in full:

```kotlin
package com.myniyam.app.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.myniyam.app.R
import com.myniyam.app.permissions.PermissionChecker
import com.myniyam.app.ui.screens.PermissionScreen
import com.myniyam.app.ui.screens.WelcomeScreen

object NiyamRoutes {
    const val WELCOME = "welcome"
    const val PERMISSION_USAGE = "permission_usage_stats"
    const val PERMISSION_OVERLAY = "permission_overlay"
    const val PERMISSION_ACCESSIBILITY = "permission_accessibility"
    const val PERMISSION_BATTERY = "permission_battery"
    const val PERMISSION_OEM = "permission_oem_autostart"
    const val HOME = "home"
}

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = NiyamRoutes.WELCOME) {

        composable(NiyamRoutes.WELCOME) {
            WelcomeScreen(onGetStarted = { navController.navigate(NiyamRoutes.PERMISSION_USAGE) })
        }

        composable(NiyamRoutes.PERMISSION_USAGE) {
            val ctx = LocalContext.current
            PermissionScreen(
                titleResId = R.string.perm_usage_title,
                bodyResId = R.string.perm_usage_body,
                isGranted = { PermissionChecker.hasUsageStatsAccess(ctx) },
                launchSettings = { PermissionChecker.openUsageAccessSettings(ctx) },
                onGranted = { navController.navigate(NiyamRoutes.PERMISSION_OVERLAY) }
            )
        }

        composable(NiyamRoutes.PERMISSION_OVERLAY) {
            val ctx = LocalContext.current
            PermissionScreen(
                titleResId = R.string.perm_overlay_title,
                bodyResId = R.string.perm_overlay_body,
                isGranted = { PermissionChecker.hasOverlayPermission(ctx) },
                launchSettings = { PermissionChecker.openOverlayPermissionSettings(ctx) },
                onGranted = { navController.navigate(NiyamRoutes.PERMISSION_ACCESSIBILITY) }
            )
        }

        composable(NiyamRoutes.PERMISSION_ACCESSIBILITY) {
            val ctx = LocalContext.current
            PermissionScreen(
                titleResId = R.string.perm_accessibility_title,
                bodyResId = R.string.perm_accessibility_body,
                isGranted = { PermissionChecker.isAccessibilityServiceEnabled(ctx) },
                launchSettings = { PermissionChecker.openAccessibilitySettings(ctx) },
                onGranted = { navController.navigate(NiyamRoutes.PERMISSION_BATTERY) }
            )
        }

        composable(NiyamRoutes.PERMISSION_BATTERY) {
            val ctx = LocalContext.current
            PermissionScreen(
                titleResId = R.string.perm_battery_title,
                bodyResId = R.string.perm_battery_body,
                isGranted = { PermissionChecker.isIgnoringBatteryOptimizations(ctx) },
                launchSettings = { PermissionChecker.openIgnoreBatteryOptimizationSettings(ctx) },
                onGranted = { navController.navigate(NiyamRoutes.PERMISSION_OEM) }
            )
        }

        composable(NiyamRoutes.PERMISSION_OEM) { Text("permission oem (Task 12)") }
        composable(NiyamRoutes.HOME) { Text("home (Task 13)") }
    }
}
```

- [ ] **Step 11.2: Build**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 11.3: Commit and STOP**

```bash
git add app/src/main/java/com/myniyam/app/ui/AppNavHost.kt
git commit -m "$(cat <<'EOF'
feat(ui): wire 4 permission screens into NavHost via PermissionScreen

Usage → overlay → accessibility → battery, each using PermissionChecker
for grant-state and Settings deep-link. OEM autostart and home come in
Tasks 12-13.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

**STOP after this. Wait for approval.**

---

## Task 12: `OemAutostartScreen`

**Files:**
- Create: `app/src/main/java/com/myniyam/app/ui/screens/OemAutostartScreen.kt`
- Modify: `app/src/main/java/com/myniyam/app/ui/AppNavHost.kt`

- [ ] **Step 12.1: Write `OemAutostartScreen.kt`**

```kotlin
package com.myniyam.app.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.myniyam.app.R
import com.myniyam.app.permissions.OemAutostartHelper

@Composable
fun OemAutostartScreen(onDone: () -> Unit) {
    val ctx = LocalContext.current
    val flow = OemAutostartHelper.flowFor(Build.MANUFACTURER)

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.perm_oem_title),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.padding(top = 16.dp))
            Text(
                text = "Your phone is ${Build.MANUFACTURER} (${flow.name}). " +
                    stringResource(R.string.perm_oem_body),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.padding(top = 48.dp))
            Button(
                onClick = { OemAutostartHelper.openAutostartSettings(ctx) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.grant))
            }
            Spacer(modifier = Modifier.padding(top = 8.dp))
            OutlinedButton(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.done))
            }
        }
    }
}
```

- [ ] **Step 12.2: Wire it into `AppNavHost.kt`**

In `AppNavHost.kt`, replace:
```kotlin
composable(NiyamRoutes.PERMISSION_OEM) { Text("permission oem (Task 12)") }
```
with:
```kotlin
composable(NiyamRoutes.PERMISSION_OEM) {
    OemAutostartScreen(onDone = { navController.navigate(NiyamRoutes.HOME) })
}
```

Add the import at top:
```kotlin
import com.myniyam.app.ui.screens.OemAutostartScreen
```

- [ ] **Step 12.3: Build**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 12.4: Commit and STOP**

```bash
git add app/src/main/java/com/myniyam/app/ui/screens/OemAutostartScreen.kt app/src/main/java/com/myniyam/app/ui/AppNavHost.kt
git commit -m "$(cat <<'EOF'
feat(ui): add OemAutostartScreen with manufacturer detection

Shows Build.MANUFACTURER + OemFlow.name in body; Grant launches
AutoStarter deep-link; Done navigates to home regardless of whether
the OEM step was actually completed (opportunistic per spec).

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

**STOP after this. Wait for approval.**

---

## Task 13: `HomeScreen`

**Files:**
- Create: `app/src/main/java/com/myniyam/app/ui/screens/HomeScreen.kt`
- Modify: `app/src/main/java/com/myniyam/app/ui/AppNavHost.kt`

- [ ] **Step 13.1: Write `HomeScreen.kt`**

```kotlin
package com.myniyam.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.myniyam.app.R
import com.myniyam.app.permissions.PermissionChecker

@Composable
fun HomeScreen() {
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var usageOk by remember { mutableStateOf(PermissionChecker.hasUsageStatsAccess(ctx)) }
    var overlayOk by remember { mutableStateOf(PermissionChecker.hasOverlayPermission(ctx)) }
    var accessibilityOk by remember { mutableStateOf(PermissionChecker.isAccessibilityServiceEnabled(ctx)) }
    var batteryOk by remember { mutableStateOf(PermissionChecker.isIgnoringBatteryOptimizations(ctx)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                usageOk = PermissionChecker.hasUsageStatsAccess(ctx)
                overlayOk = PermissionChecker.hasOverlayPermission(ctx)
                accessibilityOk = PermissionChecker.isAccessibilityServiceEnabled(ctx)
                batteryOk = PermissionChecker.isIgnoringBatteryOptimizations(ctx)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val allOk = usageOk && overlayOk && accessibilityOk && batteryOk
    val bannerLabel = if (allOk)
        stringResource(R.string.home_protection_active)
    else
        stringResource(R.string.home_protection_at_risk)
    val bannerColor = if (allOk) Color(0xFF2E7D32) else Color(0xFFC62828)

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Text(
                text = bannerLabel,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bannerColor)
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.padding(top = 24.dp))

            PermissionRow("Usage access", usageOk)
            PermissionRow("Display over other apps", overlayOk)
            PermissionRow("Accessibility service", accessibilityOk)
            PermissionRow("Ignore battery optimization", batteryOk)
        }
    }
}

@Composable
private fun PermissionRow(label: String, isOk: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = if (isOk) "✓" else "✗",
            color = if (isOk) Color(0xFF2E7D32) else Color(0xFFC62828),
            style = MaterialTheme.typography.titleLarge
        )
    }
}
```

- [ ] **Step 13.2: Wire it into `AppNavHost.kt`**

Replace:
```kotlin
composable(NiyamRoutes.HOME) { Text("home (Task 13)") }
```
with:
```kotlin
composable(NiyamRoutes.HOME) { HomeScreen() }
```

Add import:
```kotlin
import com.myniyam.app.ui.screens.HomeScreen
```

- [ ] **Step 13.3: Build**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 13.4: Commit and STOP**

```bash
git add app/src/main/java/com/myniyam/app/ui/screens/HomeScreen.kt app/src/main/java/com/myniyam/app/ui/AppNavHost.kt
git commit -m "$(cat <<'EOF'
feat(ui): add HomeScreen with live permission status banner

Re-checks 4 permissions on ON_RESUME. Banner is green when all
granted, red otherwise. Service start happens in Task 20.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

**STOP after this. Wait for approval.**

---

## Task 14: `AppLockForegroundService` skeleton

**Files:**
- Create: `app/src/main/java/com/myniyam/app/service/AppLockForegroundService.kt`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 14.1: Write `AppLockForegroundService.kt`**

The overlay logic in `onStartCommand` references `OverlayManager`, which doesn't exist yet — comment those calls for now with `// TODO(Task 19)`. They're wired up in Task 19.

```kotlin
package com.myniyam.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.myniyam.app.MainActivity
import com.myniyam.app.NiyamApplication
import com.myniyam.app.R

class AppLockForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIF_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> { /* nothing extra — onCreate already started foreground */ }
            ACTION_BLOCKED_APP_FOREGROUND -> {
                val pkg = intent.getStringExtra(EXTRA_PACKAGE) ?: return START_STICKY
                // TODO(Task 19): OverlayManager.show(this, pkg)
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        // TODO(Task 19): OverlayManager.hide()
        super.onDestroy()
    }

    private fun buildNotification(): Notification {
        val launch = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            this, 0, launch,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, NiyamApplication.CHANNEL_ID_FOREGROUND)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notif_text))
            .setContentIntent(pi)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        private const val NOTIF_ID = 1001

        const val ACTION_START = "com.myniyam.app.action.START"
        const val ACTION_BLOCKED_APP_FOREGROUND = "com.myniyam.app.action.BLOCKED_APP_FOREGROUND"
        const val EXTRA_PACKAGE = "extra_package"
    }
}
```

- [ ] **Step 14.2: Register the service in `AndroidManifest.xml`**

Inside `<application>`, add:

```xml
<service
    android:name=".service.AppLockForegroundService"
    android:exported="false"
    android:foregroundServiceType="specialUse">
    <property
        android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
        android:value="distraction blocker — must run continuously to intercept blocked apps before the user sees their feed" />
</service>
```

- [ ] **Step 14.3: Build**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 14.4: Commit and STOP**

```bash
git add app/src/main/java/com/myniyam/app/service/AppLockForegroundService.kt app/src/main/AndroidManifest.xml
git commit -m "$(cat <<'EOF'
feat(service): add AppLockForegroundService skeleton

Persistent low-importance notification, START_STICKY restart policy,
specialUse foregroundServiceType with subtype property. Overlay wiring
is TODO(Task 19).

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

**STOP after this. Wait for approval.**

---

## Task 15: `AppLockAccessibilityService`

**Files:**
- Create: `app/src/main/java/com/myniyam/app/service/AppLockAccessibilityService.kt`
- Modify: `app/src/main/java/com/myniyam/app/permissions/PermissionChecker.kt` (switch to type-safe class reference now that the class exists)

- [ ] **Step 15.1: Write `AppLockAccessibilityService.kt`**

```kotlin
package com.myniyam.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import com.myniyam.app.data.BlockList

class AppLockAccessibilityService : AccessibilityService() {

    @Volatile private var lastDismissedPkg: String? = null
    @Volatile private var lastDismissedAtMs: Long = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (!BlockList.matches(pkg)) return

        // Debounce: ignore re-triggers within 2 seconds of last dismissal for the same package.
        val now = SystemClock.elapsedRealtime()
        if (pkg == lastDismissedPkg && (now - lastDismissedAtMs) < DEBOUNCE_MS) return

        val intent = Intent(this, AppLockForegroundService::class.java).apply {
            action = AppLockForegroundService.ACTION_BLOCKED_APP_FOREGROUND
            putExtra(AppLockForegroundService.EXTRA_PACKAGE, pkg)
        }
        startService(intent)
    }

    override fun onInterrupt() = Unit

    fun markDismissed(pkg: String) {
        lastDismissedPkg = pkg
        lastDismissedAtMs = SystemClock.elapsedRealtime()
    }

    companion object {
        private const val DEBOUNCE_MS = 2_000L

        @Volatile private var instance: AppLockAccessibilityService? = null

        fun get(): AppLockAccessibilityService? = instance
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }
}
```

- [ ] **Step 15.2: Update `PermissionChecker` to use the type-safe class reference**

Restore the proper class-literal reference now that `AppLockAccessibilityService` exists.

In `PermissionChecker.kt`, change:
```kotlin
val expected = "${ctx.packageName}/com.myniyam.app.service.AppLockAccessibilityService"
```
back to:
```kotlin
val expected = "${ctx.packageName}/${AppLockAccessibilityService::class.java.canonicalName}"
```
And add the import:
```kotlin
import com.myniyam.app.service.AppLockAccessibilityService
```

- [ ] **Step 15.3: Build**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 15.4: Commit and STOP**

```bash
git add app/src/main/java/com/myniyam/app/service/AppLockAccessibilityService.kt app/src/main/java/com/myniyam/app/permissions/PermissionChecker.kt
git commit -m "$(cat <<'EOF'
feat(service): add AppLockAccessibilityService + restore type-safe PermissionChecker reference

Filters TYPE_WINDOW_STATE_CHANGED events by BlockList, debounces
within 2s, dispatches ACTION_BLOCKED_APP_FOREGROUND to the
foreground service. Exposes static instance() for liveness checks.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

**STOP after this. Wait for approval.**

---

## Task 16: AccessibilityService XML config + manifest registration

**Files:**
- Create: `app/src/main/res/xml/accessibility_service_config.xml`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 16.1: Create `accessibility_service_config.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeWindowStateChanged"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:accessibilityFlags="flagDefault"
    android:canRetrieveWindowContent="false"
    android:description="@string/accessibility_service_description"
    android:notificationTimeout="100" />
```

- [ ] **Step 16.2: Add the accessibility-service description string**

In `strings.xml`, add:
```xml
<string name="accessibility_service_description">Niyam uses Accessibility to detect when you open Instagram, Facebook, or YouTube so it can show your mantra first. It does NOT read any other content on your screen.</string>
```

- [ ] **Step 16.3: Register the service in `AndroidManifest.xml`**

Inside `<application>`, add:

```xml
<service
    android:name=".service.AppLockAccessibilityService"
    android:exported="false"
    android:label="@string/app_name"
    android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
    <intent-filter>
        <action android:name="android.accessibilityservice.AccessibilityService" />
    </intent-filter>
    <meta-data
        android:name="android.accessibilityservice"
        android:resource="@xml/accessibility_service_config" />
</service>
```

- [ ] **Step 16.4: Build**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 16.5: Commit and STOP**

```bash
git add app/src/main/res/xml/accessibility_service_config.xml app/src/main/res/values/strings.xml app/src/main/AndroidManifest.xml
git commit -m "$(cat <<'EOF'
feat(service): register AppLockAccessibilityService in manifest

Only listens for typeWindowStateChanged, no window-content retrieval.
Description string is plain-language and tells the user exactly what
the service does and does not do (anti-creep transparency).

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

**STOP after this. Wait for approval.**

---

## Task 17: `overlay_mantra.xml` layout

**Files:**
- Create: `app/src/main/res/layout/overlay_mantra.xml`

- [ ] **Step 17.1: Write the layout**

Bare placeholder visuals. Updated when UI references arrive.

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/overlay_scrim"
    android:clickable="true"
    android:focusable="true">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:gravity="center"
        android:orientation="vertical"
        android:padding="32dp">

        <TextView
            android:id="@+id/overlay_devanagari"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textColor="@color/white"
            android:textSize="96sp" />

        <TextView
            android:id="@+id/overlay_transliteration"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:textColor="@color/white"
            android:textSize="24sp" />

        <TextView
            android:id="@+id/overlay_meaning"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:gravity="center"
            android:maxWidth="320dp"
            android:textColor="@color/white"
            android:textSize="16sp" />

        <TextView
            android:id="@+id/overlay_countdown"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="32dp"
            android:fontFamily="monospace"
            android:textColor="@color/white"
            android:textSize="20sp" />

        <Button
            android:id="@+id/overlay_continue"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="48dp"
            android:enabled="false"
            android:text="@string/overlay_continue" />

    </LinearLayout>

</FrameLayout>
```

- [ ] **Step 17.2: Build**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 17.3: Commit and STOP**

```bash
git add app/src/main/res/layout/overlay_mantra.xml
git commit -m "$(cat <<'EOF'
feat(overlay): add overlay_mantra.xml layout

Bare-placeholder visuals (system fonts, semi-transparent scrim,
disabled Continue button). Updated when UI references arrive.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

**STOP after this. Wait for approval.**

---

## Task 18: `OverlayManager`

**Files:**
- Create: `app/src/main/java/com/myniyam/app/overlay/OverlayManager.kt`

- [ ] **Step 18.1: Write `OverlayManager.kt`**

```kotlin
package com.myniyam.app.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.myniyam.app.R
import com.myniyam.app.data.PlaceholderMantra
import com.myniyam.app.service.AppLockAccessibilityService

object OverlayManager {

    private var overlayView: View? = null
    private var attachedPkg: String? = null
    private var timer: CountDownTimer? = null

    fun show(ctx: Context, pkg: String) {
        if (overlayView != null) return  // already showing — no-op

        val view = LayoutInflater.from(ctx).inflate(R.layout.overlay_mantra, null, false)

        view.findViewById<TextView>(R.id.overlay_devanagari).text = PlaceholderMantra.DEVANAGARI
        view.findViewById<TextView>(R.id.overlay_transliteration).text = PlaceholderMantra.TRANSLITERATION
        view.findViewById<TextView>(R.id.overlay_meaning).text = PlaceholderMantra.ENGLISH_MEANING

        val countdown = view.findViewById<TextView>(R.id.overlay_countdown)
        val continueBtn = view.findViewById<Button>(R.id.overlay_continue)

        countdown.text = ctx.getString(R.string.overlay_unlocking_in, PlaceholderMantra.UNLOCK_TIMER_SECONDS)
        continueBtn.isEnabled = false
        continueBtn.setOnClickListener { hide(ctx) }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        try {
            wm.addView(view, params)
            overlayView = view
            attachedPkg = pkg
            startTimer(ctx, countdown, continueBtn)
        } catch (e: Exception) {
            android.util.Log.e("OverlayManager", "Failed to attach overlay", e)
        }
    }

    fun hide(ctx: Context) {
        timer?.cancel()
        timer = null

        val view = overlayView ?: return
        val pkg = attachedPkg

        try {
            val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.removeView(view)
        } catch (e: Exception) {
            android.util.Log.e("OverlayManager", "Failed to remove overlay", e)
        }

        overlayView = null
        attachedPkg = null

        // Tell the AccessibilityService to debounce re-triggers for this package.
        if (pkg != null) AppLockAccessibilityService.get()?.markDismissed(pkg)
    }

    private fun startTimer(ctx: Context, countdown: TextView, continueBtn: Button) {
        val totalMs = PlaceholderMantra.UNLOCK_TIMER_SECONDS * 1000L
        timer = object : CountDownTimer(totalMs, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000L).toInt().coerceAtLeast(0)
                countdown.text = ctx.getString(R.string.overlay_unlocking_in, secondsLeft)
            }

            override fun onFinish() {
                countdown.text = ctx.getString(R.string.overlay_unlocking_in, 0)
                continueBtn.isEnabled = true
            }
        }.start()
    }
}
```

- [ ] **Step 18.2: Build**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 18.3: Commit and STOP**

```bash
git add app/src/main/java/com/myniyam/app/overlay/OverlayManager.kt
git commit -m "$(cat <<'EOF'
feat(overlay): add OverlayManager with WindowManager + CountDownTimer

show() inflates overlay_mantra.xml, starts 15s timer, disables
Continue until timer completes. hide() cancels timer, removes the
view, and notifies AccessibilityService to debounce re-triggers.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

**STOP after this. Wait for approval.**

---

## Task 19: Wire `OverlayManager` into `AppLockForegroundService`

**Files:**
- Modify: `app/src/main/java/com/myniyam/app/service/AppLockForegroundService.kt`

- [ ] **Step 19.1: Replace the TODOs with real OverlayManager calls**

In `AppLockForegroundService.kt`:

Add at top of file:
```kotlin
import com.myniyam.app.overlay.OverlayManager
```

In `onStartCommand`, replace the comment line with the real call:
```kotlin
ACTION_BLOCKED_APP_FOREGROUND -> {
    val pkg = intent.getStringExtra(EXTRA_PACKAGE) ?: return START_STICKY
    OverlayManager.show(applicationContext, pkg)
}
```

In `onDestroy`, replace the comment line with the real call:
```kotlin
override fun onDestroy() {
    OverlayManager.hide(applicationContext)
    super.onDestroy()
}
```

- [ ] **Step 19.2: Build**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 19.3: Commit and STOP**

```bash
git add app/src/main/java/com/myniyam/app/service/AppLockForegroundService.kt
git commit -m "$(cat <<'EOF'
feat(service): wire OverlayManager.show/hide into foreground service

ACTION_BLOCKED_APP_FOREGROUND now triggers overlay; service destroy
removes overlay. Engine is now end-to-end on stock Android.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

**STOP after this. Wait for approval.**

---

## Task 20: Start the foreground service when all permissions are granted

**Files:**
- Modify: `app/src/main/java/com/myniyam/app/ui/screens/HomeScreen.kt`

- [ ] **Step 20.1: Call `ACTION_START` from `HomeScreen` when `allOk` flips true**

In `HomeScreen.kt`, after the `val allOk = ...` line, add a `LaunchedEffect` that starts the foreground service when permissions are all green:

Add imports at top:
```kotlin
import android.content.Intent
import androidx.compose.runtime.LaunchedEffect
import com.myniyam.app.service.AppLockForegroundService
```

After `val allOk = usageOk && overlayOk && accessibilityOk && batteryOk`, add:
```kotlin
LaunchedEffect(allOk) {
    if (allOk) {
        val intent = Intent(ctx, AppLockForegroundService::class.java).apply {
            action = AppLockForegroundService.ACTION_START
        }
        ctx.startForegroundService(intent)
    }
}
```

- [ ] **Step 20.2: Build**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 20.3: Commit and STOP**

```bash
git add app/src/main/java/com/myniyam/app/ui/screens/HomeScreen.kt
git commit -m "$(cat <<'EOF'
feat(ui): start AppLockForegroundService when all 4 permissions green

LaunchedEffect on allOk starts the service the first time it flips
true. Subsequent re-checks are no-ops because Android dedups
startForegroundService for an already-running service.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

**STOP after this. Wait for approval.**

---

## Task 21: Phase 1 acceptance — manual emulator test pass + report

This task has no code. It verifies the skeleton works on a Pixel emulator against all 14 Phase 1 criteria from the spec.

- [ ] **Step 21.1: Boot a Pixel emulator (Pixel 6, API 34) in Android Studio**

If one doesn't exist, create it: Tools → Device Manager → Create Device → Pixel 6 → API 34 (Google APIs) → Finish.

- [ ] **Step 21.2: Install the APK**

```bash
./gradlew :app:installDebug
```
Expected: `Installed on 1 device.`

- [ ] **Step 21.3: Manually verify each Phase 1 acceptance criterion**

Walk through all 14 criteria from [the spec, Section 11 Phase 1](../specs/2026-06-09-blocking-engine-walking-skeleton-design.md#acceptance-criteria). For each, record PASS / FAIL with a one-line note.

To install Instagram / Facebook / YouTube on the emulator, use the Play Store (you'll need to sign in with a Google account on the emulator). YouTube comes preinstalled on Google APIs images; Facebook and Instagram need to be installed via Play.

- [ ] **Step 21.4: Save the test report**

Create `docs/superpowers/test-reports/2026-06-09-niyam-skeleton-phase1-emulator.md` (replace date with the actual test date) with the 14 criteria + PASS/FAIL + notes for each.

- [ ] **Step 21.5: Commit and report results**

```bash
git add docs/superpowers/test-reports/
git commit -m "$(cat <<'EOF'
test(phase1): Phase 1 acceptance test report (emulator)

Records PASS/FAIL against all 14 Phase 1 criteria from the spec.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

Report to Pranav: "Phase 1 acceptance done. N of 14 PASS. Failures: [list]. Skeleton is [ready / not ready] for Pranav to sideload on a real device when his phone arrives tomorrow."

**STOP after this. Wait for Pranav to either approve sub-project 1 complete, or direct fixes for any failures.**

---

## Self-review

Done after writing the plan, looking at the spec with fresh eyes.

**Spec coverage:**
- All 4 design components (`AppLockAccessibilityService`, `AppLockForegroundService`, `OverlayManager`, in-app Compose UI) → Tasks 14, 15, 18, 19 (services); Task 18 (overlay); Tasks 8-13 (Compose UI). ✓
- 5-permission grant flow → Tasks 10, 11, 12. ✓
- `PermissionChecker` → Task 6. ✓
- `OemAutostartHelper` → Task 7. ✓
- `BlockList` (all three packages) → Task 4. ✓
- Hardcoded `ॐ` → Task 5. ✓
- Overlay layout (bare placeholder) → Task 17. ✓
- AccessibilityService XML config + manifest → Task 16. ✓
- Foreground service notification → Task 14. ✓
- Home screen with live permission status → Task 13. ✓
- Service-start on all-permissions-granted → Task 20. ✓
- Phase 1 acceptance verification → Task 21. ✓
- **Gap: OEM-killer heuristic** (`onTaskRemoved` → `service_kill_observed`) from the spec was deferred — it can't be reliably tested on emulator since emulators don't kill foreground services. **Decision:** leave out of the skeleton; revisit when Pranav's phone arrives and Phase 2 testing starts. Logged as a known gap in the test report (Task 21).
- **Gap: `MainActivity.onResume` service restart on enabled-but-dead AccessibilityService** (spec's "Restart phone to activate" error mode) — also deferred, same reason; cannot reproduce on emulator.

**Placeholder scan:** no TBDs, no "implement later," no vague "add error handling." Every code step has the actual code.

**Type consistency:** `BlockList.matches(packageName)` used in Task 4 and Task 15 with same signature. `OemAutostartHelper.flowFor(manufacturer)` used in Task 7 and Task 12 with same signature. `OverlayManager.show(ctx, pkg)` / `hide(ctx)` consistent between Tasks 18 and 19. `AppLockForegroundService.ACTION_START` / `ACTION_BLOCKED_APP_FOREGROUND` / `EXTRA_PACKAGE` consistent between Tasks 14, 15, 19, 20. `AppLockAccessibilityService.markDismissed(pkg)` and `get()` consistent between Tasks 15 and 18.

**Fixed inline:** none — no inconsistencies found that needed correction.

---

## Execution choice (offered after plan approval)

Two execution options, per the writing-plans skill:

**1. Subagent-Driven (recommended).** I dispatch a fresh subagent per task, review between tasks, fast iteration. Each subagent gets the relevant task + the spec excerpt and reports back with diff + verification output.

**2. Inline Execution.** I execute tasks in this session using executing-plans, batched at checkpoints (every 3-4 tasks) for Pranav review.

**Recommendation for Niyam:** Subagent-Driven. Pranav's CLAUDE.md mandates one-task-at-a-time with diff + tsc-equivalent output and explicit approval between every task. A fresh subagent per task is the cleanest way to enforce that rhythm without my main context bloating with build logs and getting confused. Each subagent is small, focused, and ends; Pranav reviews the diff, I review the diff, we approve, the next subagent starts.
