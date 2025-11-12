plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt")
    id("kotlin-parcelize")
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.iie.thethreeburnouts.mineyourmoney"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.iie.thethreeburnouts.mineyourmoney"
        minSdk = 32
        targetSdk = 36
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

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // App dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.gridlayout)
    implementation(libs.mpandroidchart)
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // added for camera functionality
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.adapters)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)

    // Unit test dependencies
    testImplementation(libs.junit) // Basic unit testing
    testImplementation(libs.mockito) // Mocking data/repositories
    testImplementation(libs.androidxTestRules) // LiveData testing

    //Room Dependencies
    implementation(libs.androidx.room.runtime)   // Room runtime
    implementation(libs.androidx.room.ktx)       // Kotlin extensions
    kapt(libs.androidx.room.compiler)            // Annotation processor
    implementation(libs.bcrypt)

    // Android instrumented test dependencies
    androidTestImplementation(libs.androidx.junit) // JUnit for instrumentation
    androidTestImplementation(libs.androidx.espresso.core) // UI testing with Espresso
    androidTestImplementation(libs.androidxTestRules) // For handling activity rules
    androidTestImplementation(libs.androidxTestRunner) // Running tests on the emulator/device

    // RoomDB testing
    testImplementation(libs.roomTesting)
}