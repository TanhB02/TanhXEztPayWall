package com.tanhxpurchase.model.template

import com.google.gson.annotations.SerializedName

data class TemplateData(
    @SerializedName("value")
    val value: String,
    @SerializedName("templates")
    val templates: List<Template>
) 