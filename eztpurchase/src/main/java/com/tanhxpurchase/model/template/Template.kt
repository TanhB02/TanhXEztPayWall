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