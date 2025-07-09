package com.tanhxpurchase.model.template

import com.google.gson.annotations.SerializedName

data class TrackingEventRequest(
    @SerializedName("paywall_config_id")
    val paywallConfigId: Int,
    @SerializedName("type")
    val type: Int,
    @SerializedName("store_id")
    val storeId: Int,
    @SerializedName("template_id")
    val templateId: Int,
    @SerializedName("payload")
    val payload: TrackingEventPayload? = null
)

data class TrackingEventPayload(
    @SerializedName("product_id")
    val productId: String? = null,
    @SerializedName("is_from_to")
    val isFromTo: String? = null
)

data class TrackingEventResponse(
    @SerializedName("data")
    val data: TrackingEventData
)

data class TrackingEventData(
    @SerializedName("id")
    val id: String,
    @SerializedName("paywall_config_id")
    val paywallConfigId: Int,
    @SerializedName("type")
    val type: Int,
    @SerializedName("store_id")
    val storeId: Int,
    @SerializedName("template_id")
    val templateId: Int,
    @SerializedName("payload")
    val payload: TrackingEventPayload?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?
)