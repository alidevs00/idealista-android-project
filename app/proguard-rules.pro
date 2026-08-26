# Add project specific ProGuard rules here.
# See https://developer.android.com/build/shrink-code for more details.

# kotlinx.serialization keeps its own consumer rules via the serialization runtime,
# these extras cover reflection-based lookups on serializable classes.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class com.idealista.challenge.data.remote.dto.** {
    kotlinx.serialization.KSerializer serializer(...);
}
