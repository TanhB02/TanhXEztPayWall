# EztPayWall Library

## Contents

* [Install](#install)
* [Setup Lib](#setup-remote)
    * [Firebase] (Remote Config)
    * [CMS] (PayWall)
    
## Install

## Trong settings.gradle.kts hoặc settings.gradle

```
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/TanhB02/TanhXEztPayWall")
            credentials {
                username = providers.gradleProperty("gpr.user").orElse(System.getenv("GITHUB_ACTOR") ?: "").get()
                password = providers.gradleProperty("gpr.token").orElse(System.getenv("GITHUB_TOKEN") ?: "").get()
            }
        }
    }
}
```

### gradle.properties

```
android.useAndroidX=true
android.enableJetifier=true

gpr.user=TanhB02
gpr.token=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
```

### build.gradle(Module:app)

```
dependencies {
	implementation ("TanhB02:eztpaywall:1.0.0-beta")
}
```

## Setup Purchase + PayWall
Your application entends EztApplication

```
class App : EztApplication() {
    companion object {
        var instance: App? = null
    }

    override fun onCreate() {
        super.onCreate()
        //TODO Gán biến check đã premium hay chưa để check load ads dùng PurchaseUtils.isRemoveAds()
    }
    
    /**
     * TanhX - Setup local product config
     */
    override fun getDefaultProductConfig(): RemoteProductConfig {
        return RemoteProductConfig(
            subscriptions = listOf(
                "product_id_yearly",
                "product_id_1monthly",
                "product_id_6monthly"
            ),
            oneTimeProducts = listOf("product_id_lifetime"),
            consumableProducts = emptyList(),
            removeAds = listOf(
                "product_id_yearly",
                "product_id_1monthly",
                "product_id_6monthly",
                "product_id_lifetime"
            )
        )
    }
}

```

## Setup Remote Config Firebase

### Remote config on Firebase (Product ID)

Parameter name (key): config_iap 

Data type: Json

Mẫu value:

```
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
## 📋 Chi tiết cấu hình các trường

### Bảng mô tả chi tiết

| Trường | Loại dữ liệu | Mô tả | Cách sử dụng | Ví dụ |
|--------|--------------|-------|--------------|-------|
| **subscriptions** | `Array<String>` | **Add Product ID** của các gói subscription | Thêm trực tiếp Product ID được tạo trên Google Play Console | `["sub_yearly", "sub_1monthly", "sub_6monthly"]` |
| **one_time_products** | `Array<String>` | **Add Product ID** của các sản phẩm mua một lần | Thêm trực tiếp Product ID được tạo trên Google Play Console | `["sub_lifetime"]` |
| **consumable_products** | `Array<String>` | **Add Product ID** của các sản phẩm tiêu hao | Thêm trực tiếp Product ID được tạo trên Google Play Console | `[]` (empty - không có) |
| **remove_ads** | `Array<String>` | **Add Product ID** của các sản phẩm khi mua sẽ tắt ads | Thêm trực tiếp Product ID từ subscriptions hoặc one_time_products | `["sub_yearly", "sub_1monthly", "sub_6monthly", "sub_lifetime"]` |
| **free_trial** | `String` | **Base Plan ID** của subscription có free trial | Thêm Base Plan ID (không phải Product ID) của subscription có trial | `"sub-yearly-free-trial"` |

---

### CMS - BE (PayWall) ----- Liên hệ BE để rõ logic 
Setup Store và Key phải trùng với packagename của project

![Alt text](https://private-user-images.githubusercontent.com/165024827/458826192-a05fad14-0839-4226-a0f9-2a3b07e4e6dd.png?jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3NTA4NDczMTAsIm5iZiI6MTc1MDg0NzAxMCwicGF0aCI6Ii8xNjUwMjQ4MjcvNDU4ODI2MTkyLWEwNWZhZDE0LTA4MzktNDIyNi1hMGY5LTJhM2IwN2U0ZTZkZC5wbmc_WC1BbXotQWxnb3JpdGhtPUFXUzQtSE1BQy1TSEEyNTYmWC1BbXotQ3JlZGVudGlhbD1BS0lBVkNPRFlMU0E1M1BRSzRaQSUyRjIwMjUwNjI1JTJGdXMtZWFzdC0xJTJGczMlMkZhd3M0X3JlcXVlc3QmWC1BbXotRGF0ZT0yMDI1MDYyNVQxMDIzMzBaJlgtQW16LUV4cGlyZXM9MzAwJlgtQW16LVNpZ25hdHVyZT0wODJjNGVhNTJkYzVhYmI0NDE4NGQzMjgzMThmMWVjMTVmNzM5Yzc5MjYxMWE2YmE3OGJjYzY4ZjU1NDkwOTRiJlgtQW16LVNpZ25lZEhlYWRlcnM9aG9zdCJ9.UdK2t6HHjsdNpEhO9is5jVPcVn7NZ91B20Iv6zkOMAE)


Setup Template để call api lấy PayWall

![Alt text](https://private-user-images.githubusercontent.com/165024827/458828048-664557a6-57b4-47c6-8c11-a20bedda4ae2.png?jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3NTA4NDc2MTgsIm5iZiI6MTc1MDg0NzMxOCwicGF0aCI6Ii8xNjUwMjQ4MjcvNDU4ODI4MDQ4LTY2NDU1N2E2LTU3YjQtNDdjNi04YzExLWEyMGJlZGRhNGFlMi5wbmc_WC1BbXotQWxnb3JpdGhtPUFXUzQtSE1BQy1TSEEyNTYmWC1BbXotQ3JlZGVudGlhbD1BS0lBVkNPRFlMU0E1M1BRSzRaQSUyRjIwMjUwNjI1JTJGdXMtZWFzdC0xJTJGczMlMkZhd3M0X3JlcXVlc3QmWC1BbXotRGF0ZT0yMDI1MDYyNVQxMDI4MzhaJlgtQW16LUV4cGlyZXM9MzAwJlgtQW16LVNpZ25hdHVyZT02OWNmNmQ0YzljYzk5MzZjYzhmYTZhNDc2MmIxNDc0ODc0ODg5ZTNhZTAwMTE1MmMzYjAwNDI4ZjA3NzNmOTA1JlgtQW16LVNpZ25lZEhlYWRlcnM9aG9zdCJ9.4ZFf8t2u5oxBrhbKq4yiLx16bvboSk5y1ubVShzkSIg)




