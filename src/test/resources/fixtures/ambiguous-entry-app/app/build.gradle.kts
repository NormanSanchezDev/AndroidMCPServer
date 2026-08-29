plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.acme.launch"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.acme.launch"
        minSdk = 24
        targetSdk = 35
    }
}