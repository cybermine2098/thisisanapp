plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services") // add this for Firebase
}

android {
    namespace = "com.cyber98.thisisanapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.cyber98.thisisanapp"
        minSdk = 33
        targetSdk = 35
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    // Added compose options block to ensure correct Kotlin compiler extension version is used.
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3" // update as needed
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))
    implementation("com.google.firebase:firebase-analytics")
    // Replaced unresolved alias for Compose UI dependencies with explicit coordinates.
    implementation("androidx.compose.ui:ui:1.5.1")
    implementation("androidx.compose.ui:ui-graphics:1.5.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.1")
    implementation(libs.androidx.material3)

    implementation("androidx.compose.ui:ui-text:1.5.1")
    implementation("io.coil-kt:coil-compose:2.3.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.0")
    implementation("com.google.firebase:firebase-messaging:24.1.0")
    implementation("com.android.volley:volley:1.2.1") // <-- added Volley dependency

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.1")
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.1")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.1")
}
