# AuraFlow ProGuard Rules
# Phase 20 will add complete rules. This baseline covers common KMP + Compose needs.

# Kotlin
-keepclassmembers class **$WhenMappings { <fields>; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata { *; }

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class **$$serializer { *; }
-keep @kotlinx.serialization.Serializable class * { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *

# Koin
-keepclassmembers class org.koin.** { *; }

# Ktor
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-keep class dev.gitlive.firebase.** { *; }

# AdMob
-keep class com.google.android.gms.ads.** { *; }

# Play Billing
-keep class com.android.billingclient.** { *; }

# Compose
-keep class androidx.compose.** { *; }

# Coil
-keep class coil3.** { *; }

# Multiplatform Settings
-keep class com.russhwolf.settings.** { *; }

# AuraFlow data classes (Room entities + serialized models)
-keep class com.auraflow.garden.data.** { *; }
-keep class com.auraflow.garden.platform.billing.** { *; }

# Coroutines
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**
