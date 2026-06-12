plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.myniyam.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.myniyam.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    debugImplementation(libs.androidx.ui.tooling)

    testImplementation(libs.junit)
}
