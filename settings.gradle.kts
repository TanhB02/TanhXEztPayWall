pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()

        maven(url = "https://jitpack.io")
        maven(url = "https://android-sdk.is.com")
        maven(url = "https://artifact.bytedance.com/repository/pangle")
        maven(url = "https://artifacts.applovin.com/android")
        maven(url = "https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea")

        val artifactoryUrl = providers.gradleProperty("artifactory_contextUrl").get()
        val artifactoryUser = providers.gradleProperty("gpr.user").get()
        val artifactoryPassword = providers.gradleProperty("gpr.token").get()

        maven(url = uri(artifactoryUrl)) {
            isAllowInsecureProtocol = true
            credentials {
                username = artifactoryUser
                password = artifactoryPassword
            }
        }


    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven(url = "https://jitpack.io")
        maven(url = "https://android-sdk.is.com")
        maven(url = "https://artifact.bytedance.com/repository/pangle")
        maven(url = "https://artifacts.applovin.com/android")
        maven(url = "https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea")

        val artifactoryUrl = providers.gradleProperty("artifactory_contextUrl").get()
        val artifactoryUser = providers.gradleProperty("gpr.user").get()
        val artifactoryPassword = providers.gradleProperty("gpr.token").get()


        maven(url = uri(artifactoryUrl)) {
            isAllowInsecureProtocol = true
            credentials {
                username = artifactoryUser
                password = artifactoryPassword
            }
        }


    }
}

rootProject.name = "TanhX_Lib_IAA_IAP"
include(":app", ":eztPurchase")
