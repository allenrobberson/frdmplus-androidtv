import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Release signing comes from keystore.properties (git-ignored), so no secrets
// ever land in the public repo. The same private key signs the tester APK and
// the Play upload bundle (see PLAY_STORE_CHECKLIST.md).
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
        // CI sets VERSION_CODE to the build number, so every upload to Play is higher
        // than the last (Play rejects a repeated versionCode).
        versionCode = (System.getenv("VERSION_CODE") ?: "1").toInt()
        versionName = System.getenv("VERSION_NAME")?.takeIf { it.isNotBlank() } ?: "0.11"

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
