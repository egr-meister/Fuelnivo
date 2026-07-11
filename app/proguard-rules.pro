# Fuelnivo ProGuard / R8 rules

# --- kotlinx.serialization ---------------------------------------------------
# Keep the serialization runtime annotations and generated serializers.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-keepclassmembers class **$$serializer {
    *** serializer(...);
}
-keepclassmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep every @Serializable model and its generated companion/serializer.
-if @kotlinx.serialization.Serializable class **
-keep class <1>
-keepclassmembers class <1> {
    *** Companion;
}
-keepclasseswithmembers class <1>$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}

# Fuelnivo data models (serialized to DataStore JSON).
-keep class com.fuelnivo.app.data.** { *; }

# --- Kotlin metadata ---------------------------------------------------------
-keep class kotlin.Metadata { *; }

# --- Compose -----------------------------------------------------------------
# AGP ships Compose keep rules; nothing extra required here.
