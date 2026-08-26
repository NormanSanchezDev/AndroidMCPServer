plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.corporate.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.corporate.app"
        minSdk = 26
        targetSdk = 35
    }
}

dependencies {
    implementation(project(":core-data"))
    implementation("androidx.core:core-ktx:1.15.0")
}
