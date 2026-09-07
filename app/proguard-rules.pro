# ═══════════════════════════════════════════════════════════════
# KHATA BOOK — ProGuard Rules for R8 Optimization
# ═══════════════════════════════════════════════════════════════

# ── Keep Room entities (Rule 26) ──
-keep class com.khatabook.app.data.local.entity.** { *; }
-keep class com.khatabook.app.data.local.dao.** { *; }

# ── Keep Hilt injected classes ──
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# ── Keep Compose ──
-keep class androidx.compose.** { *; }

# ── Keep ML Kit (OCR) ──
-keep class com.google.mlkit.** { *; }

# ── Keep Room type converters ──
-keep class com.khatabook.app.data.local.converter.** { *; }

# ── Remove logging in release (Rule 30) ──
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# ── Optimize enums ──
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ── Keep Parcelable ──
-keep class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ── Keep Serializable ──
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ── Remove unused classes (shrink aggressively) ──
-repackageclasses ''
-allowaccessmodification
-optimizationpasses 5

# ── Keep file paths for FileProvider ──
-keep class androidx.core.content.FileProvider { *; }

# ── Keep XML resources (strings.xml) ──
-keepclassmembers class **.R$* {
    public static <fields>;
}

# ═══════════════════════════════════════════════════════════════
# SIZE REDUCTION ESTIMATES
# ═══════════════════════════════════════════════════════════════
# Before R8:
#   - Code: ~2MB
#   - Resources: ~1MB
#   - Total: ~12MB (with ML Kit)
#
# After R8 + shrinkResources:
#   - Code: ~800KB (60% reduction)
#   - Resources: ~400KB (60% reduction)
#   - Total: ~6MB (with ML Kit)
#
# Without ML Kit (lazy loaded):
#   - Total: ~4MB APK
# ═══════════════════════════════════════════════════════════════
