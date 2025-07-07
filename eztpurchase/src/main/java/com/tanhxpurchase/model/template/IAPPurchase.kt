package com.tanhxpurchase.model.template

import com.android.billingclient.api.Purchase
import com.google.gson.annotations.SerializedName

data class IAPPurchase(
    @SerializedName("type")
    var type: Int = 1,

    @SerializedName("payload")
    var payload: IAPPayload? = IAPPayload(),

    @SerializedName("step")
    var step: Int = 1
) {
    companion object {
        fun from(purchase: Purchase): IAPPurchase {
            return IAPPurchase().apply {
                payload?.let { pload ->
                    pload.purchaseTime = purchase.purchaseTime
                    pload.purchaseState = purchase.purchaseState
                    pload.purchaseToken = purchase.purchaseToken
                    pload.packageName = purchase.packageName
                    pload.productId = purchase.products.getOrNull(0)
                    pload.orderId = purchase.orderId
                    pload.quantity = purchase.quantity
                    pload.autoRenewing = purchase.isAutoRenewing
                    pload.acknowledged = purchase.isAcknowledged
                }
            }
        }
    }
}



data class IAPPayload(
    @SerializedName("orderId")
    var orderId: String? = null,
    @SerializedName("packageName")
    var packageName: String? = null,
    @SerializedName("productId")
    var productId: String? = null,
    @SerializedName("purchaseTime")
    var purchaseTime: Long = 0L,
    @SerializedName("purchaseState")
    var purchaseState: Int = 1,
    @SerializedName("purchaseToken")
    var purchaseToken: String? = null,
    @SerializedName("quantity")
    var quantity: Int = 1,
    @SerializedName("autoRenewing")
    var autoRenewing: Boolean = false,
    @SerializedName("acknowledged")
    var acknowledged: Boolean = false
)
