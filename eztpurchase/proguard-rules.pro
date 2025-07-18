-repackageclasses 'com.lib.tanhx_purchase.obf'
-printmapping out/mapping.txt
#-assumenosideeffects class android.util.Log {
#    public static boolean isLoggable(java.lang.String, int);
#    public static int v(...);
#    public static int i(...);
#    public static int w(...);
#    public static int d(...);
#    public static int e(...);
#}

-assumenosideeffects class java.io.PrintStream {
    public void println(%);
    public void println(**);
}


-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
    static void checkExpressionValueIsNotNull(java.lang.Object, java.lang.String);
    static void checkNotNullExpressionValue(java.lang.Object, java.lang.String);
    static void checkReturnedValueIsNotNull(java.lang.Object, java.lang.String, java.lang.String);
}
-keep class kotlin.jvm.internal.** { *; }
-keep class kotlin.collections.** { *; }
-keep class kotlin.coroutines.** { *; }
-keep class kotlin.sequences.** { *; }


-keepclassmembers class **.R$* {
    public static <fields>;
}
-keep class **.R$*

-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keep class com.tanhxpurchase.model.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**
-keep class com.google.firebase.remoteconfig.** { *; }
-keep class com.google.firebase.analytics.** { *; }
-keep class com.android.billingclient.api.** { *; }
-keep interface com.android.billingclient.api.** { *; }
-keep public interface com.android.billingclient.api.PurchasesUpdatedListener
-keep public class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**
-keep class com.auth0.jwt.** { *; }
-dontwarn com.auth0.jwt.**
-keep class com.orhanobut.hawk.** { *; }
-dontwarn com.orhanobut.hawk.**
-keep class android.webkit.** { *; }
-keep interface android.webkit.** { *; }
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keep class com.tanhxpurchase.worker.** { *; }
-keep class * implements androidx.viewbinding.ViewBinding {
    public static *** inflate(...);
    public static *** bind(...);
}
-keep class androidx.databinding.** { *; }
-keep public class com.tanhxpurchase.PurchaseUtils { *; }
-keep public class com.tanhxpurchase.EztApplication { *; }
-keep public class com.tanhxpurchase.TrackingUtils { *; }
-keep public class com.tanhxpurchase.worker.WokerMananer { *; }
-keep public class com.tanhxpurchase.worker.trackingevent.TrackingEventWorker { *; }
-keep public class com.tanhxpurchase.worker.registerdevice.DeviceRegistrationWorker { *; }
-keep public class com.tanhxpurchase.worker.paydone.IAPLoggingWorker { *; }
-keep public class com.tanhxpurchase.base.** { *; }
-keep public class com.tanhxpurchase.dialog.** { *; }
-keep public class com.tanhxpurchase.customview.** { *; }
-keep interface com.tanhxpurchase.listeners.** { *; }
-keep class * implements com.tanhxpurchase.listeners.** { *; }
-keep class com.tanhxpurchase.ConstantsPurchase { *; }
-dontwarn android.support.**
-dontwarn androidx.appcompat.widget.**
-dontwarn org.checkerframework.**
-dontwarn javax.annotation.**
-dontwarn edu.umd.cs.findbugs.annotations.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn kotlin.reflect.**
-dontwarn kotlin.Unit
-dontwarn java.lang.invoke.StringConcatFactory
-dontwarn java.lang.invoke.MethodHandles$Lookup
-dontwarn java.lang.invoke.MethodHandle
-dontwarn java.lang.invoke.MethodType
-dontwarn java.lang.invoke.CallSite
-keep class java.lang.invoke.** { *; }
-adaptresourcefilecontents **.properties,META-INF/MANIFEST.MF
-keep class **.R$layout {
    public static final int activity_iap_web_view;
    public static final int bottom_sheet_premium;
    public static final int dialog_premium;
    public static final int item_iap;
}
-keep class **.R$string {
    public static final int continue_;
    public static final int privacy;
    public static final int terms_of_use;
    public static final int restore;
    public static final int high_speed;
    public static final int all_server_locations;
    public static final int no_comitent;
    public static final int remove_ads;
    public static final int one_month;
    public static final int six_months;
    public static final int one_year;
    public static final int save_20;
    public static final int day_free_trial;
    public static final int enjoy_ad_free;
}
-keep class **.R$drawable {
    public static final int bg_*;
    public static final int ic_*;
}
-keep class **.R$raw {
    public static final int anim_loading;
    public static final int anim_map;
}
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable


-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(...);
    static void checkExpressionValueIsNotNull(...);
    static void checkNotNullExpressionValue(...);
    static void checkReturnedValueIsNotNull(...);
}

-printseeds out/seeds.txt
-printusage out/usage.txt
-printconfiguration out/configuration.txt
