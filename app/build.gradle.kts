plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
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

    // Unit test dependencies
    testImplementation(libs.junit) // Basic unit testing
    testImplementation(libs.mockito) // Mocking data/repositories
    testImplementation(libs.androidxTestRules) // LiveData testing

    // Android instrumented test dependencies
    androidTestImplementation(libs.androidx.junit) // JUnit for instrumentation
    androidTestImplementation(libs.androidx.espresso.core) // UI testing with Espresso
    androidTestImplementation(libs.androidxTestRules) // For handling activity rules
    androidTestImplementation(libs.androidxTestRunner) // Running tests on the emulator/device

    // RoomDB testing
    testImplementation(libs.roomTesting)
}