# R8 is disabled for the first release (isMinifyEnabled=false). These rules are ready for
# the staged step where minify + shrinkResources are enabled.

# ---- Kotlinx Serialization ----
-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep generated serializers and Companion objects of @Serializable app classes
# (GameData, Settings and the typed navigation routes).
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.jovialjewels.game.**$$serializer { *; }
-keepclassmembers class com.jovialjewels.game.** {
    *** Companion;
}
-keepclasseswithmembers class com.jovialjewels.game.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Navigation type-safe routes are resolved by serializer name.
-keepnames class com.jovialjewels.game.ui.navigation.** { *; }
