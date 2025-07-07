package com.tanhxpurchase.model.template

import com.google.gson.annotations.SerializedName

data class TemplateAll(
    @SerializedName("package_id")
    val packageId: String
)

data class GetTemplateResponseAll(
    @SerializedName("data") val data: TemplateDataAll?
)

data class TemplateDataAll(
    val id: Int,
    val name: String,
    @SerializedName("store_id") val storeId: String,
    @SerializedName("store_md5") val storeMd5: String,
    val platform: Int,
    val params: List<Param>
)

data class Param(
    val id: Int,
    @SerializedName("params_md5") val paramsMd5: String,
    val params: String,
    val active: Int,
    val description: String?,
    @SerializedName("store_id") val storeId: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("firebase_values") val firebaseValues: List<FirebaseValue>
)

data class FirebaseValue(
    val id: Int,
    val value: String,
    @SerializedName("value_md5") val valueMd5: String,
    val description: String?,
    @SerializedName("firebase_params_id") val firebaseParamsId: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    val templates: List<TemplateAlll>
)

data class TemplateAlll(
    val id: Int,
    val name: String,
    val url: String,
    @SerializedName("design_url") val designUrl: String?,
    val active: Int,
    val status: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    val thumbnail: String?,
    @SerializedName("laravel_through_key") val laravelThroughKey: Int,
    @SerializedName("paywall_config") val paywallConfig: PaywallConfig?
)

data class PaywallConfig(
    val id: Int,
    @SerializedName("template_id") val templateId: Int,
    @SerializedName("firebase_value_id") val firebaseValueId: Int
)
