# Firebase Firestore and gRPC rules
-keepclassmembernames class com.google.** { *; }
-keepnames class com.google.** { *; }

# gRPC - Keep all classes and methods
-dontwarn io.grpc.**
-keep class io.grpc.** { *; }
-keepclassmembers class io.grpc.** { *; }
-keep interface io.grpc.** { *; }
-keepnames class io.grpc.** { *; }

# Keep specific gRPC internal classes that are accessed reflectively
-keep class io.grpc.internal.** { *; }
-keep class io.grpc.util.** { *; }
-keep class * extends io.grpc.** { *; }

# Firestore
-keep class com.google.firebase.firestore.** { *; }
-keep interface com.google.firebase.firestore.** { *; }
-keepclassmembers class com.google.firebase.firestore.** { *; }

# Firestore internal classes
-keep class com.google.firestore.** { *; }
-keepclassmembers class com.google.firestore.** { *; }

# Keep data model classes for Firestore serialization
-keepclassmembers class com.github.naz013.reviews.db.ReviewEntity { *; }
-keep class com.github.naz013.reviews.db.ReviewEntity { *; }

# Protobuf
-keep class com.google.protobuf.** { *; }
-keepclassmembers class com.google.protobuf.** { *; }
-dontwarn com.google.protobuf.**

# Keep all protobuf generated classes
-keepclassmembers class * extends com.google.protobuf.** { *; }

# OkHttp (used by Firestore)
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Conscrypt (used by Firestore for SSL)
-keep class org.conscrypt.** { *; }
-dontwarn org.conscrypt.**

# Perfmark (used by gRPC)
-keep class io.perfmark.** { *; }
-dontwarn io.perfmark.**

# OpenCensus (used by gRPC)
-dontwarn io.opencensus.**
-keep class io.opencensus.** { *; }

# Android X annotations
-dontwarn javax.annotation.**
-keep class javax.annotation.** { *; }

