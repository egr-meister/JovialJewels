import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// ---- Release signing: read ONLY from environment variables, never fall back to debug keys ----
val releaseKeystorePath: String? = System.getenv("ANDROID_KEYSTORE_PATH")
val releaseKeystorePassword: String? = System.getenv("ANDROID_KEYSTORE_PASSWORD")
val releaseKeyAlias: String? = System.getenv("ANDROID_KEY_ALIAS")
val releaseKeyPassword: String? = System.getenv("ANDROID_KEY_PASSWORD")

val missingSigningVars: List<String> = listOf(
    "ANDROID_KEYSTORE_PATH" to releaseKeystorePath,
    "ANDROID_KEYSTORE_PASSWORD" to releaseKeystorePassword,
    "ANDROID_KEY_ALIAS" to releaseKeyAlias,
    "ANDROID_KEY_PASSWORD" to releaseKeyPassword,
).filter { it.second.isNullOrBlank() }.map { it.first }

val releaseSigningReady: Boolean = missingSigningVars.isEmpty()

android {
    namespace = "com.jovialjewels.game"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.jovialjewels.game"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    signingConfigs {
        create("release") {
            if (releaseSigningReady) {
                storeFile = file(releaseKeystorePath!!)
                storePassword = releaseKeystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
                storeType = "pkcs12"
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }

    buildTypes {
        release {
            // Staged R8: ship the first release non-minified, then enable both flags
            // after a verified non-minified release (see README "Staged R8").
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = false
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

// Fail clearly (before any work runs) if a signed release artifact is requested without signing env vars.
val releaseArtifactTask = Regex("^(assemble|bundle|package|sign|validateSigning).*Release.*$")
gradle.taskGraph.whenReady {
    val wantsRelease = allTasks.any { it.project == project && releaseArtifactTask.matches(it.name) }
    if (wantsRelease) {
        if (!releaseSigningReady) {
            throw GradleException(
                "Release signing is not configured. Missing environment variables: " +
                    missingSigningVars.joinToString() +
                    ". Release builds are never signed with the debug key — see README \"Local signing\".",
            )
        }
        val ks = file(releaseKeystorePath!!)
        if (!ks.isFile) {
            throw GradleException("ANDROID_KEYSTORE_PATH does not point to an existing keystore file.")
        }
    }
}

dependencies {
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.runtime.saveable)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.androidx.compose.ui.geometry)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.animation.core)
    implementation(libs.androidx.compose.material3)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.common)
    implementation(libs.androidx.navigation.runtime)

    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.preferences.core)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
}
