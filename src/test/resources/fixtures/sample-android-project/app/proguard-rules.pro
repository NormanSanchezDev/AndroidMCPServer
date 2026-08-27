# ProGuard rules for app module

-keep class com.corporate.app.** { *; }
-keep class com.corporate.data.** { *; }

-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

-dontwarn kotlinx.**
-dontwarn org.jetbrains.**
