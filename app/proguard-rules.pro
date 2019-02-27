-verbose
-allowaccessmodification
-assumevalues class android.os.Build$VERSION {
    int SDK_INT return 21..2147483647;
}

# ServiceLoader support
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Most of volatile fields are updated with AFU and should not be mangled
#-keepclassmembernames class kotlinx.** {
#    volatile <fields>;
#}

-dontwarn com.google.errorprone.annotations.*
-dontwarn java.lang.ClassValue
-keep class java.lang.ClassValue { *; }

-dontwarn org.apache.**
-dontwarn de.hdodenhof.circleimageview.**
-dontwarn ch.boye.**
-dontwarn com.google.android.gms.**
-dontwarn com.google.api.client.http.**
-dontwarn org.bouncycastle.**
-dontwarn org.json.**
-dontwarn com.dropbox.**
-dontwarn jp.wasabeef.**
-dontwarn com.squareup.okhttp.**
-dontwarn android.support.v8.**
-dontwarn android.support.design.**
-dontwarn okio.**
-dontwarn org.mockito.**
-dontwarn sun.reflect.**
-dontwarn android.test.**
-dontwarn android.net.**
-dontwarn junit.framework.**

-dontnote android.net.http.**
-dontnote org.apache.http.**
-dontnote com.dropbox.**
-dontnote org.json.**
-dontnote org.dmfs.rfc5545.**
-dontnote com.backdoor.simpleai.**
-dontnote okhttp3.internal.platform.**

-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Needed to keep generic types and @Key annotations accessed via reflection

-keepattributes Signature,RuntimeVisibleAnnotations,AnnotationDefault

-keepclassmembers class * {
@com.google.api.client.util.Key <fields>;
}

# Needed by google-http-client-android when linking against an older platform version

-dontwarn com.google.api.client.extensions.android.**

# Needed by google-api-client-android when linking against an older platform version

-dontwarn com.google.api.client.googleapis.extensions.android.**

# Needed by google-play-services when linking against an older platform version

-dontwarn com.google.android.gms.**
-dontnote com.google.android.gms.**

# com.google.client.util.IOUtils references java.nio.file.Files when on Java 7+
-dontnote java.nio.file.Files, java.nio.file.Path

# Suppress notes on LicensingServices
-dontnote **.ILicensingService

# Suppress warnings on sun.misc.Unsafe
-dontnote sun.misc.Unsafe
-dontwarn sun.misc.Unsafe

-dontwarn java.lang.invoke.*

# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on RoboVM on iOS. Will not be used at runtime.
-dontnote retrofit2.Platform$IOS$MainThreadExecutor
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

-dontwarn org.hamcrest.**
-dontwarn android.test.**
-dontwarn android.support.test.**

-keep class org.hamcrest.** {
   *;
}

-keep class org.junit.** { *; }
-dontwarn org.junit.**

-keep class junit.** { *; }
-dontwarn junit.**

-keep class sun.misc.** { *; }
-dontwarn sun.misc.**

-dontwarn com.google.common.**

-dontwarn com.evernote.android.job.gcm.**
-dontwarn com.evernote.android.job.GcmAvailableHelper
-dontwarn com.evernote.android.job.work.**
-dontwarn com.evernote.android.job.WorkManagerAvailableHelper

-keep public class com.evernote.android.job.v21.PlatformJobService
-keep public class com.evernote.android.job.v14.PlatformAlarmService
-keep public class com.evernote.android.job.v14.PlatformAlarmReceiver
-keep public class com.evernote.android.job.JobBootReceiver
-keep public class com.evernote.android.job.JobRescheduleService
-keep public class com.evernote.android.job.gcm.PlatformGcmService
-keep public class com.evernote.android.job.work.PlatformWorker

-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**