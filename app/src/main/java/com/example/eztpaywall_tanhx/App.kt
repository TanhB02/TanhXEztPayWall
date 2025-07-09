package com.example.eztpaywall_tanhx

import com.tanhxpurchase.EztApplication
import com.tanhxpurchase.model.iap.RemoteProductConfig


class App : EztApplication() {
    companion object {
        var instance: App? = null
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun getDefaultProductConfig(): RemoteProductConfig {
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



