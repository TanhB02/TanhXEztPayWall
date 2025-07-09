package com.tanhxpurchase.model.template

import com.tanhxpurchase.model.iap.Purchase as CustomPurchase
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
        fun from(purchase: CustomPurchase): IAPPurchase {
            return IAPPurchase().apply {
                payload?.let { pload ->
                    pload.productId = purchase.productId
                    pload.quantity = purchase.quantity
                    pload.purchaseTime = purchase.purchaseTime
                    pload.purchaseState = purchase.purchaseState
                    pload.orderId = purchase.orderId
                    pload.packageName = purchase.packageName
                    pload.purchaseToken = purchase.purchaseToken
                    pload.autoRenewing = purchase.autoRenewing
                    pload.acknowledged = purchase.acknowledged
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

data class IapLogResponse(
    @SerializedName("data")
    val data: IapLogData
)

data class IapLogData(
    @SerializedName("type")
    val type: Int,
    @SerializedName("payload")
    val payload: IAPPayload?,
    @SerializedName("step")
    val step: Int,
    @SerializedName("store_temporary_id")
    val storeTemporaryId: String?,
    @SerializedName("device_id")
    val deviceId: String?,
    @SerializedName("id_db")
    val idDb: Int,
    @SerializedName("order_id_md5")
    val orderIdMd5: String?,
    @SerializedName("purchase_token_md5")
    val purchaseTokenMd5: String?,
    @SerializedName("subscription_temp_id")
    val subscriptionTempId: String?,
    @SerializedName("purchase_time")
    val purchaseTime: Long,
    @SerializedName("firebase_type")
    val firebaseType: Int,
    @SerializedName("updated_at")
    val updatedAt: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("id")
    val id: String?
)

