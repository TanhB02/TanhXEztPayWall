package com.tanhxpurchase.repository

import com.orhanobut.hawk.Hawk
import com.tanhxpurchase.AUTHEN_PAYWALL
import com.tanhxpurchase.model.template.GetTemplateRequest
import com.tanhxpurchase.model.template.GetTemplateResponse
import com.tanhxpurchase.model.template.TemplateKeyParam
import com.tanhxpurchase.network.NetworkModule
import com.tanhxpurchase.network.TemplateApiService
import com.tanhxpurchase.util.ApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TemplateRepository(
    private val templateApiService: TemplateApiService = NetworkModule.templateApiService
) {
    
    fun getTemplatesByKeys(keys: List<String>): Flow<ApiResult<GetTemplateResponse>> = flow {
        try {
            emit(ApiResult.Loading)
            
            val token = Hawk.get<String>(AUTHEN_PAYWALL, null)
            if (token.isNullOrEmpty()) {
                emit(ApiResult.Error("Token not found"))
                return@flow
            }
            
            val request = GetTemplateRequest(
                keys = keys.map { TemplateKeyParam(it) }
            )
            
            val response = templateApiService.getTemplatesByKey(
                token = token,
                request = request
            )
            
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    emit(ApiResult.Success(body))
                } ?: emit(ApiResult.Error("Response body is null"))
            } else {
                emit(ApiResult.Error("API Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(ApiResult.Error(e.message ?: "Unknown error occurred"))
        }
    }
} 