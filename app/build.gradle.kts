import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.khatabook.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.khatabook.app"
        minSdk = 21          // Android 5.0 — covers 99%+ devices
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        // ═══════════════════════════════════════════════════════════
        // PERFORMANCE: Smaller DEX files
        // ═══════════════════════════════════════════════════════════
        multiDexEnabled = false  // Keep single DEX for < 64K methods
    }

    buildTypes {
        release {
            isMinifyEnabled = true            // R8 code shrinking (Rule 26)
            isShrinkResources = true          // Remove unused resources
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            // ═══════════════════════════════════════════════════════════
            // PERFORMANCE: Remove duplicate native libraries
            // ═══════════════════════════════════════════════════════════
            excludes += "/META-INF/DEPENDENCIES"
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // PERFORMANCE: Build optimizations
    // ═══════════════════════════════════════════════════════════════
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

dependencies {
    // ═══════════════════════════════════════════════════════════════
    // CORE — Keep minimal (Rule 25)
    // ═══════════════════════════════════════════════════════════════
    implementation("androidx.core:core-ktx:1.12.0")           // ~50KB
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")  // ~30KB
    implementation("androidx.activity:activity-compose:1.8.2")  // ~40KB

    // ═══════════════════════════════════════════════════════════════
    // COMPOSE — BOM for version alignment
    // ═══════════════════════════════════════════════════════════════
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")                    // Core
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")     // Material 3
    implementation("androidx.compose.material:material-icons-extended")  // Icons
    debugImplementation("androidx.compose.ui:ui-tooling")

    // ═══════════════════════════════════════════════════════════════
    // NAVIGATION — Compose Navigation
    // ═══════════════════════════════════════════════════════════════
    implementation("androidx.navigation:navigation-compose:2.7.6")  // ~80KB

    // ═══════════════════════════════════════════════════════════════
    // ROOM — Local database (Rule 22)
    // ═══════════════════════════════════════════════════════════════
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")        // ~200KB
    implementation("androidx.room:room-ktx:$roomVersion")           // ~50KB
    ksp("androidx.room:room-compiler:$roomVersion")                 // Annotation processor

    // ═══════════════════════════════════════════════════════════════
    // HILT — Dependency injection
    // ═══════════════════════════════════════════════════════════════
    val hiltVersion = "2.50"
    implementation("com.google.dagger:hilt-android:$hiltVersion")    // ~100KB
    ksp("com.google.dagger:hilt-android-compiler:$hiltVersion")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")   // ~20KB

    // ═══════════════════════════════════════════════════════════════
    // IMAGE LOADING — Coil (Rule 20, lighter than Glide)
    // ═══════════════════════════════════════════════════════════════
    implementation("io.coil-kt:coil-compose:2.5.0")             // ~300KB

    // ═══════════════════════════════════════════════════════════════
    // CAMERA — CameraX (on-demand only, Rule 8)
    // ═══════════════════════════════════════════════════════════════
    val cameraVersion = "1.3.1"
    implementation("androidx.camera:camera-camera2:$cameraVersion")   // ~150KB
    implementation("androidx.camera:camera-lifecycle:$cameraVersion")
    implementation("androidx.camera:camera-view:$cameraVersion")

    // ═══════════════════════════════════════════════════════════════
    // OCR — ML Kit (on-demand only, Rule 8)
    // Heavy: ~20MB, loaded only when Camera tab opens
    // ═══════════════════════════════════════════════════════════════
    implementation("com.google.mlkit:text-recognition:16.0.0")    // ~8MB
    implementation("com.google.mlkit:text-recognition-chinese:16.0.0")  // ~12MB

    // ═══════════════════════════════════════════════════════════════
    // EXIF — Image rotation handling
    // ═══════════════════════════════════════════════════════════════
    implementation("androidx.exifinterface:exifinterface:1.3.7")  // ~30KB

    // ═══════════════════════════════════════════════════════════════
    // IMAGE CROPPER — Circular avatar crop (uCrop fork, Maven Central)
    // ═══════════════════════════════════════════════════════════════
    implementation("com.vanniktech:android-image-cropper:4.7.0")

    // ═══════════════════════════════════════════════════════════════
    // BIOMETRIC — Fingerprint / Face unlock
    // ═══════════════════════════════════════════════════════════════
    implementation("androidx.biometric:biometric:1.1.0")

    // ═══════════════════════════════════════════════════════════════
    // PDF — Android built-in PdfDocument (Rule 25, no extra lib)
    // Excel — CSV only (no Apache POI, saves 8MB)
    // ═══════════════════════════════════════════════════════════════

    // ═══════════════════════════════════════════════════════════════
    // TESTING — Minimal
    // ═══════════════════════════════════════════════════════════════
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.8")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")  // Memory leak detection
}
