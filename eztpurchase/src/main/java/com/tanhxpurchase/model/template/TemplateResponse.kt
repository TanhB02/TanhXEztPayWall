package com.tanhxpurchase.model.template

import com.google.gson.annotations.SerializedName

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