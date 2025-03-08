buildscript {
    repositories {
        google()  // ensure this repository is included
        mavenCentral()
    }
    dependencies {
        classpath("com.google.gms:google-services:4.3.15") // add the Google services plugin dependency
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}