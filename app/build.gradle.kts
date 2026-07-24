plugins {
    id("com.android.application")
}

android {
    namespace = "de.haidox.normandiereise"
    compileSdk = 35

    defaultConfig {
        applicationId = "de.haidox.normandiereise"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
