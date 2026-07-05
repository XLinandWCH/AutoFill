# Keep main entry points and Kotlin runtime
-keepclasseswithmembers public class * {
    public static void main(java.lang.String[]);
}
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# Keep Compose Multiplatform Desktop internals
-keep class org.jetbrains.skia.** { *; }
-keep class org.jetbrains.skiko.** { *; }

# Keep Playwright class library completely — including the impl subpackage.
# com.microsoft.playwright.impl.Driver is loaded via reflection and contains
# the logic to extract the bundled Node.js driver ZIP. Without this rule,
# ProGuard strips it and CLI.main() fails with "Failed to create driver".
-keep class com.microsoft.playwright.** { *; }
-keep class com.microsoft.playwright.impl.** { *; }
-keepclassmembers class com.microsoft.playwright.impl.Driver {
    static *;
}
-dontwarn com.microsoft.playwright.**

# Keep Jsoup and ignore warnings
-keep class org.jsoup.** { *; }
-dontwarn org.jsoup.**

# Keep Ktor and ignore warnings
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Keep SLF4J
-keep class org.slf4j.** { *; }
-dontwarn org.slf4j.**

# Keep Coil and Markdown renderer
-keep class coil3.** { *; }
-dontwarn coil3.**
-keep class com.mikepenz.** { *; }
-dontwarn com.mikepenz.**

# General reflection/serialization support
-keepattributes Signature,InnerClasses,EnclosingMethod,AnnotationDefault,*Annotation*
-dontwarn javax.annotation.**
-dontwarn org.w3c.dom.css.**
