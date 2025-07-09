package com.tanhxpurchase.model.template

import com.google.gson.annotations.SerializedName

data class Template(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("design_url")
    val designUrl: String?,
    @SerializedName("active")
    val active: Int,
    @SerializedName("status")
    val status: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("thumbnail")
    val thumbnail: String?
)

data class TemplateKeyParam(
    @SerializedName("params")
    val params: String
)

data class GetTemplateRequest(
    @SerializedName("keys")
    val keys: List<TemplateKeyParam>
)

data class TemplateData(
    @SerializedName("value")
    val value: String,
    @SerializedName("templates")
    val templates: List<Template>
)

data class TemplateResponse(
    @SerializedName("params")
    val params: String,
    @SerializedName("data")
    val data: List<TemplateData>
)

data class GetTemplateResponse(
    @SerializedName("data")
    val data: List<TemplateResponse>
)