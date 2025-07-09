# ===================================================================
# 📦 MULTIDEX CONFIGURATION FOR EZTPURCHASE MODULE
# ===================================================================

# Keep Application class in main dex
-keep public class com.tanhxpurchase.EztApplication { *; }

# Keep essential classes for startup in main dex
-keep class com.tanhxpurchase.PurchaseUtils { *; }
-keep class com.tanhxpurchase.billing.BillingService { *; }

# Keep Firebase classes needed for startup
-keep class com.google.firebase.FirebaseApp { *; }
-keep class com.google.firebase.remoteconfig.FirebaseRemoteConfig { *; }

# Keep Hawk for preferences
-keep class com.orhanobut.hawk.Hawk { *; }

# Keep JWT utilities
-keep class com.tanhxpurchase.util.JwtPayWall { *; }

# Keep essential Android components
-keep class androidx.multidex.** { *; }
-keep class android.support.multidex.** { *; }

# Keep essential constants
-keep class com.tanhxpurchase.ConstantsPurchase { *; } 