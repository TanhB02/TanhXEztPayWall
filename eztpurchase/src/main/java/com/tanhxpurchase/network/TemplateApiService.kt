package com.tanhxpurchase.network

import com.tanhxpurchase.model.template.DeviceRequest
import com.tanhxpurchase.model.template.DeviceResponse
import com.tanhxpurchase.model.template.GetTemplateRequest
import com.tanhxpurchase.model.template.GetTemplateResponse
import com.tanhxpurchase.model.template.GetTemplateResponseAll
import com.tanhxpurchase.model.template.TemplateAll
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface TemplateApiService {
    
    @POST("/api/v1/templates/get-by-key")
    suspend fun getTemplatesByKey(
        @Header("AuthorizationApi") token: String,
        @Header("Accept") accept: String = "application/json",
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: GetTemplateRequest
    ): Response<GetTemplateResponse>

    @POST("/api/v1/templates/get-by-packageid")
    suspend fun getTemplatesByPackageId(
        @Header("AuthorizationApi") token: String,
        @Header("Accept") accept: String = "application/json",
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: TemplateAll
    ): Response<GetTemplateResponseAll>

    @POST("/api/v1/devices")
    suspend fun registerDevice(
        @Header("Accept") accept: String = "application/json",
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: DeviceRequest
    ): Response<DeviceResponse>
} 