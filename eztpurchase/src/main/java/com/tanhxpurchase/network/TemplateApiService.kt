package com.tanhxpurchase.network

import com.tanhxpurchase.model.template.GetTemplateRequest
import com.tanhxpurchase.model.template.GetTemplateResponse
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
} 