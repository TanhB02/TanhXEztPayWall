package com.tanhxpurchase.model.template

import com.google.gson.annotations.SerializedName

data class DeviceRequest(
    @SerializedName("secret")
    val secret: String
)

data class DeviceResponse(
    @SerializedName("data")
    val data: DeviceData
)

data class DeviceData(
    @SerializedName("device")
    val device: Device,
    @SerializedName("access_token")
    val accessToken: String
)

data class Device(
    @SerializedName("client_id")
    val clientId: String,
    @SerializedName("client_id_md5")
    val clientIdMd5: String,
    @SerializedName("platform")
    val platform: Int,
    @SerializedName("id")
    val id: String
)
