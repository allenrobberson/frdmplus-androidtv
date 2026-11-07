import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Release signing comes from keystore.properties (git-ignored), so no secrets
// ever land in the public repo. See README → "Publish to Google Play".
val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "com.frdmplus.tv"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.frdmplus.tv"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // The site the app opens. Change it here if the address ever moves.
        buildConfigField("String", "START_URL", "\"https://frdmplus.com/\"")
        // Sign-up / subscription pages. While ALLOW_SIGNUP is false the app lets
        // people sign in but sends new subscribers to the website, which keeps
        // the Play listing clear of Google Play's payments rules. Set it to true
        // only if FRDM+ enrolls in Play's alternative billing / external links
        // programs (or adds Google Play Billing).
        buildConfigField("boolean", "ALLOW_SIGNUP", "false")
        buildConfigField(
            "String", "SIGNUP_PATHS", // comma-separated
            "\"/registration,/subscribe,/alternativePurchase,/gift/purchase,/external\"",
        )
        // Shown first to anyone who isn't signed in yet.
        buildConfigField("String", "LOGIN_URL", "\"https://frdmplus.com/login\"")
        // Site endpoint that reports whether this device is signed in.
        buildConfigField("String", "PROFILE_API", "\"/api/core/user/profile\"")
    }

    signingConfigs {
        // Shared, public test key (the standard "android" passwords) so every
        // test build can install over the last one. Not used for Play releases.
        getByName("debug") {
            storeFile = rootProject.file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        if (keystoreProps.containsKey("storeFile")) {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfigs.findByName("release")?.let { signingConfig = it }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}
