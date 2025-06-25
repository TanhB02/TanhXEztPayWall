# EztPurchase Module - Created by TanhX

Module EztPurchase là một thư viện Android hỗ trợ Google Play Billing với tích hợp Firebase Remote Config và hệ thống Paywall động.

## Tính năng chính

- ✅ Hỗ trợ Google Play Billing (Subscription & In-app Products)
- ✅ Tích hợp Firebase Remote Config
- ✅ Hệ thống Paywall động từ server
- ✅ UI Components: Activity, Dialog, BottomSheet
- ✅ Free Trial support
- ✅ Dark mode support
- ✅ Translattion Paywall support

## 1. Setup và Cài đặt

### 1.1. Thêm dependency

Trong file `build.gradle` (Module: app):

```kotlin
dependencies {
    implementation project(':eztPurchase')
}

buildFeatures {
    dataBinding = true
}
```

### 1.2. Cấu hình Application Class

```kotlin
class App : EztApplication() {
    override fun onCreate() {
        super.onCreate()
        
        // Kiểm tra premium status
        val isPremium = PurchaseUtils.isRemoveAds()
        
        // Các logic khác của app
    }
    
    // Override để custom product config nếu cần
    override fun getDefaultProductConfig(): RemoteProductConfig {
        return RemoteProductConfig(
            subscriptions = listOf("your_yearly_sub", "your_monthly_sub"),
            oneTimeProducts = listOf("your_lifetime_product"),
            consumableProducts = emptyList(),
            removeAds = listOf("your_yearly_sub", "your_monthly_sub", "your_lifetime_product"),
            freeTrial = "your_free_trial_sub"
        )
    }
}
```

## 2. Cấu hình Firebase Remote Config

### 2.1. Cấu hình IAP Products

Tạo parameter `config_iap` trên Firebase Remote Config với format JSON:

```json
{
  "subscriptions": [
    "sub_yearly",
    "sub_1monthly", 
    "sub_6monthly"
  ],
  "one_time_products": [
    "sub_lifetime"
  ],
  "consumable_products": [],
  "remove_ads": [
    "sub_yearly",
    "sub_1monthly",
    "sub_6monthly", 
    "sub_lifetime"
  ],
  "free_trial": "sub-yearly-free-trial"
}
```

### 2.2. Cấu hình Paywall Templates

Trên BI Tool, tạo template với:
- **Package name**: Tên package của app
- **Template value**: Tương ứng với key sẽ dùng (ví dụ: `PayWallIAP1`, `DialogLimit`, `DialogRemove`)

## 3. Sử dụng Module

### 3.1. Khởi tạo và cấu hình

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set country code cho đa ngôn ngữ
        PurchaseUtils.setCountryCode("vi") // vi, en, ja, ko, etc.
        
        // Set dark mode
        PurchaseUtils.setDarkMode(isDarkMode)
        
        // Lắng nghe khi billing service sẵn sàng
        PurchaseUtils.addInitBillingFinishListener {
            val price = PurchaseUtils.getPrice("sub_yearly")
        }
    }
}
```

### 3.2. Kiểm tra trạng thái Premium

```kotlin
// Cách 1: Kiểm tra trực tiếp
if (PurchaseUtils.isRemoveAds()) {
    // User đã mua premium
} else {
    // User chưa mua premium
}

// Cách 2: Sử dụng lambda
PurchaseUtils.setActionPurchase(
    actionSuccess = {
        // User đã premium
    },
    actionFailed = {
        // User chưa premium, hiển thị paywall
    }
)

// Cách 3: Observe flow
lifecycleScope.launch {
    PurchaseUtils.isRemoveAds.collect { isPremium ->
        if (isPremium) {
            // Update UI cho premium user
        } else {
            // Update UI cho free user
        }
    }
}
```

### 3.3. Hiển thị Paywall

#### 3.3.1. Activity Paywall

```kotlin
// Lấy URL paywall từ remote config sau đó call api để lấy template
val paywallUrl = PurchaseUtils.getPayWall(packageName, "PayWallIAP1")

PurchaseUtils.startActivityIAP(
    context = this@MainActivity,
    urlWeb = paywallUrl,
    onPurchaseSuccess = {
        // Xử lý khi mua thành công
        Toast.makeText(this, "Purchase successful!", Toast.LENGTH_SHORT).show()
    },
    onCloseClicked = {
        // Xử lý khi user đóng paywall
    }
)
```

#### 3.3.2. Dialog Paywall

```kotlin
val paywallUrl = PurchaseUtils.getPayWall(packageName, "DialogLimit")

PurchaseUtils.showDialogPayWall(
    context = this@MainActivity,
    lifecycleCoroutineScope = lifecycleScope,
    url = paywallUrl,
    onUpgradeNow = {
        // Xử lý khi user click upgrade
        startPurchaseFlow()
    },
    onFailure = {
        // Xử lý khi load paywall thất bại
    }
)
```

#### 3.3.3. BottomSheet Paywall

```kotlin
val paywallUrl = PurchaseUtils.getPayWall(packageName, "DialogRemove")

PurchaseUtils.showBottomSheetPayWall(
    activity = this@MainActivity,
    url = paywallUrl,
    onUpgradeNow = {
        // Xử lý upgrade
    },
    watchAdsCallBack = {
        // Xử lý xem ads (nếu có)
    },
    onFailure = {
        // Xử lý lỗi
    }
)
```

### 3.4. Thực hiện mua hàng

```kotlin
private fun startPurchaseFlow() {
    val productId = "sub_yearly" // hoặc product ID khác
    
    PurchaseUtils.buy(
        activity = this@MainActivity,
        id = productId,
        onPurchaseSuccess = { purchase ->
            // Mua thành công
            Toast.makeText(this, "Purchase successful!", Toast.LENGTH_SHORT).show()
            // Update UI
        },
        onPurchaseFailure = { code, errorMsg ->
            // Mua thất bại
            Toast.makeText(this, "Purchase failed: $errorMsg", Toast.LENGTH_SHORT).show()
        },
        onOwnedProduct = { productId ->
            // Product đã được sở hữu
        },
        onUserCancelBilling = {
            // User hủy bỏ
        }
    )
}
```

### 3.5. Lấy thông tin giá

```kotlin
// Lấy giá có đơn vị tiền tệ
val priceWithCurrency = PurchaseUtils.getPrice("sub_yearly") // "$9.99"

// Lấy giá không có đơn vị tiền tệ
val priceAmount = PurchaseUtils.getPriceWithoutCurrency("sub_yearly") // 9.99

// Lấy đơn vị tiền tệ
val currency = PurchaseUtils.getCurrency("sub_yearly") // "USD"

// Lấy giá giảm (cho offers)
val discountPrice = PurchaseUtils.getDiscountPrice("offer_id")
```

### 3.6. Sử dụng Custom Views

#### PremiumConstraintLayout

View này sẽ tự động ẩn khi user đã premium:

```xml
<com.tanhxpurchase.customview.PremiumConstraintLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
    
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Upgrade to Premium!" />
        
</com.tanhxpurchase.customview.PremiumConstraintLayout>
```

## 4. Các Constants quan trọng

```kotlin
// Product IDs mặc định
const val Base_Plan_Id_1Monthly = "sub-1monthly"
const val Base_Plan_Id_6Monthly = "sub-6monthly" 
const val Base_Plan_Id_Yearly = "sub-yearly"
const val Base_Plan_Id_Yearly_Trial = "sub-yearly-free-trial"

// Paywall keys mặc định sẽ phải tự config tùy các app và tùy dev quy định
const val PayWallIAPDefault = "PayWallIAPDefault"
const val DialogLimitDefault = "DialogLimitDefault"
const val DialogRemoveDefault = "DialogRemoveDefault"
const val DialogHighSpeedDefault = "DialogHighSpeedDefault"
const val DialogProServerDefault = "DialogProServerDefault"

// Remote config key
const val CONFIG_IAP_KEY = "config_iap"
```

## 5. Xử lý lỗi phổ biến

### 5.1. Billing Service chưa sẵn sàng

```kotlin
PurchaseUtils.addInitBillingFinishListener {
    // Chỉ gọi các method lấy giá sau khi listener này được trigger
    updatePriceUI()
}
```




