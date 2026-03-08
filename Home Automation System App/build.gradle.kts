//// Top-level build file where you can add configuration options common to all sub-projects/modules.
//plugins {
//    alias(libs.plugins.android.application) apply false
//    alias(libs.plugins.kotlin.android) apply false
//}






// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false
}
// Top-level build.gradle.kts
buildscript {
    // If Adding Firebase Depenedencies to project is giving an error, try or put the below said repositories.
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath ("com.google.gms:google-services:4.3.15")
//        classpath 'com.google.gms:google-services:4.4.2'
//
//        classpath("com.google.gms:google-services:4.4.0") // Apply the Google Services plugin
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.4") // Optional for Crashlytics
    }
}
