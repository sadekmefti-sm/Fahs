# Fahs (dz.fahs.app) — Production ProGuard / R8 Rules
# Preserves Room entities/DAOs, Cloud Firestore models, Google ML Kit OCR, and Credential Manager

-keepattributes SourceFile,LineNumberTable
-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*

# Room Database Entities & DAOs
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep class com.example.data.local.** { *; }

# Cloud Firestore Serializable Models (CloudInspectionBackup, ShopSettings, WilayaDeliveryFee)
-keep class com.example.data.repository.CloudInspectionBackup { *; }
-keep class com.example.data.repository.ShopSettings { *; }
-keep class com.example.data.repository.WilayaDeliveryFee { *; }
-keep class com.example.data.model.** { *; }

# Google ML Kit Bundled Text Recognition (Odometer & Tyre DOT OCR)
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Firebase Auth, Firestore & Jetpack Credential Manager
-keep class com.google.firebase.** { *; }
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-dontwarn com.google.firebase.**
