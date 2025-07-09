package com.tanhxpurchase.repository

import android.content.Context
import com.tanhxpurchase.ConstantsPurchase.API

import com.tanhxpurchase.hawk.EzTechHawk.accessToken
import com.tanhxpurchase.hawk.EzTechHawk.authenPayWall
import com.tanhxpurchase.hawk.EzTechHawk.authenTracking
import com.tanhxpurchase.model.template.DeviceRequest
import com.tanhxpurchase.model.template.DeviceResponse
import com.tanhxpurchase.model.template.GetTemplateRequest
import com.tanhxpurchase.model.template.GetTemplateResponse
import com.tanhxpurchase.model.template.GetTemplateResponseAll
import com.tanhxpurchase.model.template.IAPPurchase
import com.tanhxpurchase.model.template.IapLogResponse
import com.tanhxpurchase.model.template.TemplateAll
import com.tanhxpurchase.model.template.TemplateKeyParam
import com.tanhxpurchase.model.template.TrackingEventRequest
import com.tanhxpurchase.model.template.TrackingEventResponse
import com.tanhxpurchase.network.NetworkModule
import com.tanhxpurchase.network.TemplateApiService
import com.tanhxpurchase.util.ApiResult
import com.tanhxpurchase.util.JwtPayWall
import com.tanhxpurchase.util.JwtPayWall.generateTrackingToken
import com.tanhxpurchase.util.logd
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TemplateRepository(
    private val templateApiService: TemplateApiService = NetworkModule.templateApiService
) {
    //TODO delected ----> get all + flatmap by keys
    fun getTemplatesByKeys(keys: List<String>): Flow<ApiResult<GetTemplateResponse>> = flow {
        try {
            emit(ApiResult.Loading)

            if (authenPayWall.isEmpty()) {
                emit(ApiResult.Error("Token not found"))
                return@flow
            }

            val request = GetTemplateRequest(
                keys = keys.map { TemplateKeyParam(it) }
            )

            val response = templateApiService.getTemplatesByKey(
                token = authenPayWall,
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
    
    fun registerDevice(context: Context): Flow<ApiResult<DeviceResponse>> = flow {
        try {
            val isTrackingExpired = JwtPayWall.isTrackingTokenExpired()

            //Token còn hạn
            if (accessToken.isNotEmpty() && !isTrackingExpired) {
                logd("Access token exists and tracking token still valid, skipping API call", API)
                return@flow
            }
            logd("Need to call API - Access token exists: ${!accessToken.isNullOrEmpty()}, Tracking expired: $isTrackingExpired", API)
            if (isTrackingExpired) {
                generateTrackingToken(context, context.packageName)
            }
            
            emit(ApiResult.Loading)
            
            if (authenTracking.isEmpty()) {
                logd("Failed to get tracking token after generation", API)
                emit(ApiResult.Error("Failed to get tracking token"))
                return@flow
            }
            
            logd("Calling device registration API", API)
            
            val request = DeviceRequest(secret = authenTracking)
            
            val response = templateApiService.registerDevice(request = request)
            
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    logd("Device registration successful: ${body.data.device.id}", API)

                    accessToken = body.data.accessToken
                    logd("Access token saved to storage", API)
                    
                    emit(ApiResult.Success(body))
                } ?: run {
                    logd("Device registration response body is null", API)
                    emit(ApiResult.Error("Response body is null"))
                }
            } else {
                val errorMessage = "Device registration failed: ${response.code()} - ${response.message()}"
                logd(errorMessage, API)
                emit(ApiResult.Error(errorMessage))
            }
        } catch (e: Exception) {
            val errorMessage = "Device registration exception: ${e.message ?: "Unknown error occurred"}"
            logd(errorMessage, API)
            emit(ApiResult.Error(errorMessage))
        }
    }

    fun getTemplatesByPackageId(packageId: String): Flow<ApiResult<GetTemplateResponseAll>> = flow {
        emit(ApiResult.Loading)

        if (authenPayWall.isBlank()) {
            emit(ApiResult.Error("Authorization token is missing"))
            return@flow
        }

        try {
            val response = templateApiService.getTemplatesByPackageId(
                token = authenPayWall,
                request = TemplateAll(packageId = packageId)
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    emit(ApiResult.Success(body))
                } else {
                    emit(ApiResult.Error("API returned empty response"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                emit(ApiResult.Error("API Error ${response.code()}: ${response.message()}\n$errorBody"))
            }

        } catch (e: Exception) {
            emit(ApiResult.Error("Network/API exception: ${e.localizedMessage ?: "Unknown error"}"))
        }
    }
    
    fun logIAPPurchase(iapPurchase: IAPPurchase): Flow<ApiResult<IapLogResponse>> = flow {
        try {
            emit(ApiResult.Loading)
            
            if (accessToken.isEmpty()) {
                emit(ApiResult.Error("Access token not found"))
                return@flow
            }

            val response = templateApiService.logIAPPurchase(
                accessToken = accessToken,
                purchase = iapPurchase
            )
            
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    emit(ApiResult.Success(body))
                } ?: run {
                    emit(ApiResult.Error("Response body is null"))
                }
            } else {
                val errorMessage = "IAP logging failed: ${response.code()} - ${response.message()}"
                logd(errorMessage, API)
                emit(ApiResult.Error(errorMessage))
            }
        } catch (e: Exception) {
            val errorMessage = "IAP logging exception: ${e.message ?: "Unknown error occurred"}"
            logd(errorMessage, API)
            emit(ApiResult.Error(errorMessage))
        }
    }

    fun logTrackingEvent(trackingEvent: TrackingEventRequest): Flow<ApiResult<TrackingEventResponse>> = flow {
        try {
            emit(ApiResult.Loading)

            if (accessToken.isEmpty()) {
                emit(ApiResult.Error("Access token not found"))
                return@flow
            }

            logd("TemplateRepository: Sending tracking event to API - Type: ${trackingEvent.type}, Template: ${trackingEvent.templateId}", API)

            val response = templateApiService.logTrackingEvent(
                accessToken = accessToken,
                request = trackingEvent
            )

            if (response.isSuccessful) {
                response.body()?.let { body ->
                    logd("TemplateRepository: Tracking event logged successfully - ID: ${body.data.id}", API)
                    emit(ApiResult.Success(body))
                } ?: run {
                    logd("TemplateRepository: Tracking event response body is null", API)
                    emit(ApiResult.Error("Response body is null"))
                }
            } else {
                val errorMessage = "Tracking event logging failed: ${response.code()} - ${response.message()}"
                logd(errorMessage, API)
                emit(ApiResult.Error(errorMessage))
            }
        } catch (e: Exception) {
            val errorMessage = "Tracking event logging exception: ${e.message ?: "Unknown error occurred"}"
            logd(errorMessage, API)
            emit(ApiResult.Error(errorMessage))
        }
    }
}