package com.tanhxpurchase.model.template

data class DeviceResponse(
    val data: DeviceData
)

data class DeviceData(
    val device: Device,
    val access_token: String
)

data class Device(
    val client_id: String,
    val client_id_md5: String,
    val platform: Int,
    val id: String
) 