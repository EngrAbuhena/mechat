# MeChat release rules — kept minimal (RULE: only what is needed).
-keepattributes Signature
-keepattributes *Annotation*
# kotlinx.serialization
-keepclassmembers class **$$serializer { *; }
-keepclassmembers class com.mechat.app.network.protocol.** { *; }
# Hilt
-keep class dagger.hilt.** { *; }
