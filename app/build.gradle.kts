//
//plugins {
//    alias(libs.plugins.android.application)
//    alias(libs.plugins.kotlin.android)
//}
//
//android {
//    namespace = "com.example.sha_2"
//    compileSdk = 34
//
//    defaultConfig {
//        applicationId = "com.example.sha_2"
//        minSdk = 24
//        targetSdk = 34
//        versionCode = 1
//        versionName = "1.0"
//
//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//    }
//
//    buildTypes {
//        release {
//            isMinifyEnabled = false
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
//        }
//    }
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_11
//        targetCompatibility = JavaVersion.VERSION_11
//    }
//    kotlinOptions {
//        jvmTarget = "11"
//    }
//}
//
//dependencies {
//
//    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.appcompat)
//    implementation(libs.material)
//    implementation(libs.androidx.activity)
//    implementation(libs.androidx.constraintlayout)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
//}














//plugins {
//
//    alias(libs.plugins.android.application)
//    alias(libs.plugins.kotlin.android)
//    alias(libs.plugins.google.gms.google.services)
//    //  new plugins to link DB
//
//}
//
//android {
//    namespace = "com.example.sha_2"
//    compileSdk = 35
//
//    defaultConfig {
//        applicationId = "com.example.sha_2"
//        minSdk = 24
//        targetSdk = 34
//        versionCode = 1
//        versionName = "1.0"
//
//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//    }
//
//    buildTypes {
//        release {
//            isMinifyEnabled = false
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
//        }
//    }
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_11
//        targetCompatibility = JavaVersion.VERSION_11
//    }
//    kotlinOptions {
//        jvmTarget = "11"
//    }
//}
//
//dependencies {
//    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.appcompat)
//    implementation(libs.material)
//    implementation(libs.androidx.activity)
//    implementation(libs.androidx.constraintlayout)
//    implementation(libs.firebase.firestore)
////    implementation(libs.firebase.firestore.ktx)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
////    new dependencies to link DB
//    implementation(platform("com.google.firebase:firebase-bom:32.0.0")) // Firebase BOM
////    implementation("com.google.firebase:firebase-ktx:")
////    implementation(platform("com.google.firebase:firebase-bom:33.7.0")) // Firebase BOM
////    implementation("com.google.firebase:firebase-analytics") // Firestore with Kotlin extensions
//
//    implementation("com.google.firebase:firebase-auth-ktx")  // for authentication
//    implementation("com.google.firebase:firebase-analytics-ktx")  // for analytics
//
//
//
//    implementation ("com.google.android.material:material:1.3.0-alpha03")
//
//
//
//
//}















plugins {

    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    //alias(libs.plugins.google.gms.google.services)  // Ensure this line is present only here
}

android {
    namespace = "com.example.sha_2"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.sha_2"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database.ktx)
    implementation(libs.androidx.tools.core)
    implementation(libs.androidx.media3.common.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Firebase BOM to manage versions automatically
    implementation(platform("com.google.firebase:firebase-bom:32.0.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    // Material design dependency
    implementation("com.google.android.material:material:1.3.0-alpha03")

    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")


   implementation ("com.google.firebase:firebase-database-ktx:20.0.3'")
        implementation ("com.google.firebase:firebase-auth-ktx:21.0.1")
        implementation ("com.squareup.okhttp3:okhttp:4.9.1")
        implementation ("com.github.kittinunf.fuel:fuel:2.3.1")
        implementation ("com.github.kittinunf.fuel:fuel-coroutines:2.3.1")
        // Add your IR library here, for example:
        // implementation 'com.github.example:ir-library:version'

//    JavaMail Dependency: Add the JavaMail dependency to your build.gradle file:
    implementation ("com.sun.mail:android-mail:1.6.2")
    implementation ("com.sun.mail:android-activation:1.6.2")


    implementation  ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation  ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")


}

