package com.tanhxpurchase

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import com.orhanobut.hawk.Hawk
import com.tanhxpurchase.model.RemoteProductConfig
import com.tanhxpurchase.repository.TemplateRepository
import com.tanhxpurchase.sharepreference.EzTechPreferences.producFreetrial
import com.tanhxpurchase.util.ApiResult
import com.tanhxpurchase.util.JwtPayWall
import com.tanhxpurchase.util.TemplateDataManager
import com.tanhxpurchase.util.logD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

open class EztApplication : Application() {

    companion object {
        var infoProductID : RemoteProductConfig? = null
        private lateinit var instance: EztApplication
        private var templateRepository: TemplateRepository? = null
        private var templateJob: Job? = null
        private val templateScope = CoroutineScope(Dispatchers.IO)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Hawk.init(this).build()
        FirebaseApp.initializeApp(this@EztApplication)
        PurchaseUtils.init(this@EztApplication)
        initFirebaseRemoteConfig()
        JwtPayWall.jwtToken(instance)
        loadTemplateData()
        PurchaseUtils.checkFreeTrial()
    }

    private fun loadTemplateData() {
        templateRepository = TemplateRepository()

        templateJob = templateScope.launch {
            try {

                templateRepository?.getTemplatesByKeys(listOf(packageName))?.collect { result ->
                    when (result) {
                        is ApiResult.Success -> {
                            val templateResponse = result.data.data.find { it.params == packageName }
                            templateResponse?.let { template ->
                                TemplateDataManager.saveTemplateData(packageName, template.data)
                                template.data.forEach { templateData ->
                                    templateData.templates.forEach {
                                        Log.d(EZT_Purchase, "observeViewModel: Template URL:${it.url}")
                                    }
                                }
                                cleanupTemplateResources()
                            }
                        }
                        is ApiResult.Error -> {
                            logD("TANHXXXX =>>>>> Error loading templates: ${result.message}")
                            cleanupTemplateResources()
                        }
                        is ApiResult.Loading -> {
                            logD("TANHXXXX =>>>>> Loading templates...")
                        }
                    }
                }
            } catch (e: Exception) {
                logD("TANHXXXX =>>>>> Exception loading templates: ${e.message}")
                cleanupTemplateResources()
            }
        }
    }

    private fun cleanupTemplateResources() {
        templateJob?.cancel()
        templateJob = null
        templateRepository = null
    }

    private fun setupPurchaseProducts() {
        val remoteConfig = getRemoteProductConfig()
        infoProductID = remoteConfig
        producFreetrial = remoteConfig.freeTrial.toString()

        PurchaseUtils.Builder()
            .fromRemoteConfig(remoteConfig)
            .build()
    }

    fun getRemoteProductConfig(): RemoteProductConfig {
        return try {
            fetchRemoteProductConfig() ?: getDefaultProductConfig()
        } catch (e: Exception) {
            getDefaultProductConfig()
        }
    }

    private fun initFirebaseRemoteConfig() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                setupPurchaseProducts()
            } else {
                setupPurchaseProducts()
            }
        }
    }

    private fun fetchRemoteProductConfig(): RemoteProductConfig? {
        return try {
            val remoteConfig = FirebaseRemoteConfig.getInstance()
            val configJson = remoteConfig.getString(CONFIG_IAP_KEY)
            if (configJson.isNotEmpty()) {
                Gson().fromJson(configJson, RemoteProductConfig::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

     open fun getDefaultProductConfig(): RemoteProductConfig {
        return RemoteProductConfig(
            subscriptions = listOf("product_id_yearly", "product_id_1monthly", "product_id_6monthly"),
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

fun PurchaseUtils.Builder.fromRemoteConfig(config: RemoteProductConfig): PurchaseUtils.Builder {
    subscriptions(config.subscriptions)
    oneTimeProducts(config.oneTimeProducts)
    consumableProducts(config.consumableProducts)
    removeAds(config.removeAds)
    return this
}