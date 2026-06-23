plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.myniyam.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.myniyam.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 8
        versionName = "1.0.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Supabase config (SP-P2). All three are CLIENT-SAFE — they ship in every APK
        // and data is protected by Row-Level Security. The service_role key must NEVER
        // appear here. Values come from ~/.gradle/gradle.properties (out of the repo);
        // empty fallback keeps CI builds compiling.
        buildConfigField("String", "SUPABASE_URL",
            "\"${providers.gradleProperty("NIYAM_SUPABASE_URL").getOrElse("")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY",
            "\"${providers.gradleProperty("NIYAM_SUPABASE_ANON_KEY").getOrElse("")}\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID",
            "\"${providers.gradleProperty("NIYAM_GOOGLE_WEB_CLIENT_ID").getOrElse("")}\"")
    }

    signingConfigs {
        // Upload key lives OUTSIDE the repo; credentials come from ~/.gradle/gradle.properties.
        // Builds stay unsigned-release-capable on machines without them (CI safety).
        create("upload") {
            val storeFileProp = providers.gradleProperty("NIYAM_UPLOAD_STORE_FILE").orNull
            if (storeFileProp != null) {
                storeFile = file(storeFileProp)
                storePassword = providers.gradleProperty("NIYAM_UPLOAD_STORE_PASSWORD").orNull
                keyAlias = providers.gradleProperty("NIYAM_UPLOAD_KEY_ALIAS").orNull
                keyPassword = providers.gradleProperty("NIYAM_UPLOAD_KEY_PASSWORD").orNull
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (providers.gradleProperty("NIYAM_UPLOAD_STORE_FILE").orNull != null) {
                signingConfig = signingConfigs.getByName("upload")
            }
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
        buildConfig = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.autostarter)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.play.services.ads)
    implementation(libs.androidx.work.runtime)

    // Supabase backend (SP-P2): Auth + Postgrest on a Ktor/OkHttp engine.
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.functions)
    implementation(libs.supabase.storage)
    implementation(libs.supabase.compose.auth)
    implementation(libs.billing.ktx)
    implementation(libs.ktor.client.okhttp)
    // Native Google sign-in (SP-P3): Credential Manager + Google ID.
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    debugImplementation(libs.androidx.ui.tooling)

    testImplementation(libs.junit)
}
