-libraryjars libs

-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
-keepattributes InnerClasses,EnclosingMethod

-printmapping /proguard/mapping.txt

-keep class com.elementary.tasks.reminder.models.** { *; }
-keep class com.elementary.tasks.places.RealmPlace { *; }
-keep class com.elementary.tasks.places.PlaceItem { *; }
-keep class com.elementary.tasks.notes.RealmNote { *; }
-keep class com.elementary.tasks.notes.RealmImage { *; }
-keep class com.elementary.tasks.notes.NoteItem { *; }
-keep class com.elementary.tasks.notes.NoteImage { *; }
-keep class com.elementary.tasks.missed_calls.CallItem { *; }
-keep class com.elementary.tasks.missed_calls.RealmCallItem { *; }
-keep class com.elementary.tasks.groups.GroupItem { *; }
-keep class com.elementary.tasks.groups.RealmGroup { *; }
-keep class com.elementary.tasks.google_tasks.TaskListItem { *; }
-keep class com.elementary.tasks.google_tasks.TaskItem { *; }
-keep class com.elementary.tasks.google_tasks.RealmTaskList { *; }
-keep class com.elementary.tasks.google_tasks.RealmTask { *; }
-keep class com.elementary.tasks.birthdays.BirthdayItem { *; }
-keep class com.elementary.tasks.birthdays.RealmBirthdayItem { *; }

-keepclassmembers class * extends io.realm.RealmObject { *; }

-keepnames public class * extends io.realm.RealmObject
-keep @io.realm.annotations.RealmModule class *
-keep class io.realm.** { *; }
-dontwarn javax.**
-dontwarn io.realm.**

-keep class io.realm.annotations.RealmModule

-keep class android.support.v4.app.** { *; }
-keep interface android.support.v4.app.** { *; }
-keep class com.dropbox.** {*;}
-keep class org.apache.http.** { *; }
-keep class ch.boye.** { *; }
-keep class jp.wasabeef.** { *; }
-keep class io.codetail.animation.arcanimator.** { *; }
-keep class android.support.v8.renderscript.** { *; }
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep class com.meg7.widget.** { *; }
-keep class android.support.v7.widget.** { *; }
-keep class android.support.v7.widget.SearchView { *; }
-keep interface android.support.v7.widget.** { *; }
-keep public class android.support.design.R$* { *; }
-keep public class * extends android.support.v7.widget.SearchView {
   public <init>(android.content.Context);
   public <init>(android.content.Context, android.util.AttributeSet);
}

-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

-dontwarn org.apache.**
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

-dontnote android.net.http.**
-dontnote org.apache.http.**
-dontnote com.dropbox.**
-dontnote org.json.**
-dontnote org.dmfs.rfc5545.**
-dontnote com.backdoor.simpleai.**

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

# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html

-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Optimization is turned off by default. Dex does not like code run
# through the ProGuard optimize and preverify steps (and performs some
# of these optimizations on its own).
-dontoptimize
-dontpreverify
# Note that if you want to enable optimization, you cannot just
# include optimization flags in your own project configuration file;
# instead you will need to point to the
# "proguard-android-optimize.txt" file instead of this one from your
# project.properties file.

-keepattributes *Annotation*
-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}

# keep setters in Views so that animations can still work.
# see http://proguard.sourceforge.net/manual/examples.html#beans
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

# We want to keep methods in Activity that could be used in the XML attribute onClick
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.**

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
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}


