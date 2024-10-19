# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# Obfuscation
-repackageclasses
-allowaccessmodification

# Kotlin
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
	public static void check*(...);
	public static void throw*(...);
}
-assumenosideeffects class java.util.Objects {
    public static ** requireNonNull(...);
}

# Activity and Fragment names
-keep class it.dhd.oxygencustomizer.aiplugin.ui.activities.**
-keep class it.dhd.oxygencustomizer.aiplugin.ui.fragments.**
-keep class it.dhd.oxygencustomizer.aiplugin.receivers.**
-keepnames class it.dhd.oxygencustomizer.aiplugin.receivers.**

# Strip debug log
-assumenosideeffects class android.util.Log {
    public static int v(...);
}

# org.pytorch.PyTorchAndroid
-keepnames class org.pytorch.**
-keep class org.pytorch.** { *; }
-keep class com.facebook.jni.* { *; }

# ONNX Runtime
-keep class ai.onnxruntime.** { *; }

-printusage release/usage.txt
-printmapping release/mapping.txt