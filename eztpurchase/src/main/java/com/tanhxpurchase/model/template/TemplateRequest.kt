package com.tanhxpurchase.model.template

import com.google.gson.annotations.SerializedName

data class TemplateKeyParam(
    @SerializedName("params")
    val params: String
)

data class GetTemplateRequest(
    @SerializedName("keys")
    val keys: List<TemplateKeyParam>
) 