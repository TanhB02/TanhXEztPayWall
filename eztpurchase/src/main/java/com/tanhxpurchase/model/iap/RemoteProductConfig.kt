package com.tanhxpurchase.model.iap

import com.google.gson.annotations.SerializedName

data class RemoteProductConfig(
    @SerializedName("subscriptions")
    val subscriptions: List<String> = emptyList(),
    @SerializedName("one_time_products")
    val oneTimeProducts: List<String> = emptyList(),
    @SerializedName("consumable_products")
    val consumableProducts: List<String> = emptyList(),
    @SerializedName("remove_ads")
    val removeAds: List<String> = emptyList(),
    @SerializedName("free_trial")
    val freeTrial: String = "sub-yearly-free-trial"
)