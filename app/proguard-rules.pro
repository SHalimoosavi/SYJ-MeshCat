# SYJ-MeshChat release ProGuard/R8 rules.
# Most defaults come from the AGP-provided proguard-android-optimize.txt.
# Project-specific rules are added here as later milestones introduce
# reflection-sensitive libraries (Room, Hilt, kotlinx.serialization).

# Keep line numbers for readable stack traces in crash reports.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Hilt / Dagger ---
# Hilt's own consumer ProGuard rules (bundled in the AAR) handle most of
# this automatically; kept here as an explicit safety net.
-keep class dagger.hilt.internal.aggregatedroot.codegen.* { *; }
-keep class hilt_aggregated_deps.* { *; }

# --- Room ---
-keep class androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }

# --- kotlinx.serialization ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
