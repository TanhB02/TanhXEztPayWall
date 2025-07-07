package com.tanhxpurchase.model.template

import com.google.gson.annotations.SerializedName

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
