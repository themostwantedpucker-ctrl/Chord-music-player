# Keep Hilt generated classes
-keep class dagger.hilt.internal.** { *; }
-keep class *$$InjectAdapter { *; }
-keep class *$$ModuleAdapter { *; }
-keep class *$$MembersInjector { *; }

# Keep Kotlin metadata
-keepclassmembers class kotlin.Metadata { *; }

# Media3 keep rules
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
