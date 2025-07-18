-keep public class com.tanhxpurchase.PurchaseUtils { *; }
-keep public class com.tanhxpurchase.EztApplication { *; }
-keep public class com.tanhxpurchase.base.BaseActivity { *; }
-keep public class com.tanhxpurchase.base.BaseBottomSheetDialogFragment { *; }
-keep public class com.tanhxpurchase.base.BaseDialog { *; }
-keep public class com.tanhxpurchase.dialog.PremiumDialog { *; }
-keep public class com.tanhxpurchase.dialog.PremiumBottomSheet { *; }
-keep public class com.tanhxpurchase.customview.** { *; }

-keep interface com.tanhxpurchase.listeners.** { *; }
-keep class * implements com.tanhxpurchase.listeners.** { *; }

-keep class com.tanhxpurchase.ConstantsPurchase { *; }

-keep class com.tanhxpurchase.model.** { *; }

-keepattributes Signature
-keepattributes *Annotation*
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

-keep class * implements com.android.billingclient.api.PurchasesUpdatedListener { *; }
-keep class com.android.billingclient.api.** { *; }
-keep interface com.android.billingclient.api.** { *; }

-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

-keep class android.webkit.** { *; }
-keep interface android.webkit.** { *; }

-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker

-keep public class com.airbnb.lottie.** { *; }

-keep class com.auth0.jwt.** { *; }
-keep class com.orhanobut.hawk.** { *; }

-keep class * implements androidx.viewbinding.ViewBinding {
    public static *** inflate(...);
    public static *** bind(...);
}
-keep class androidx.databinding.** { *; }

-dontwarn com.tanhxpurchase.**

-dontwarn java.lang.invoke.StringConcatFactory
-dontwarn java.lang.invoke.MethodHandles$Lookup
-dontwarn java.lang.invoke.MethodHandle
-dontwarn java.lang.invoke.MethodType
-dontwarn java.lang.invoke.CallSite

-keep class java.lang.invoke.** { *; }
