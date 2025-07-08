package com.tanhxpurchase.worker.registerdevice

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.orhanobut.hawk.Hawk
import com.tanhxpurchase.AUTHEN_TRACKING
import com.tanhxpurchase.API
import com.tanhxpurchase.repository.TemplateRepository
import com.tanhxpurchase.util.ApiResult
import com.tanhxpurchase.util.JwtPayWall.generateTrackingToken
import com.tanhxpurchase.util.logd

class DeviceRegistrationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val repository = TemplateRepository()

    override suspend fun doWork(): Result {
        return try {
            val trackingToken = Hawk.get<String>(AUTHEN_TRACKING, null)
            if (trackingToken.isNullOrEmpty()) {
                generateTrackingToken(applicationContext, applicationContext.packageName)
            }
            var finalResult: Result = Result.failure()

            repository.registerDevice(applicationContext).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        logd("DeviceRegistrationWorker: loadding", API)
                    }

                    is ApiResult.Success -> {
                        logd("DeviceRegistrationWorker: API success", API)
                        finalResult = Result.success()
                        return@collect
                    }

                    is ApiResult.Error -> {
                        logd("DeviceRegistrationWorker: ${result.message}", API)
                        finalResult = Result.retry()
                        return@collect
                    }
                }
            }
            finalResult
        } catch (e: Exception) {
            logd("DeviceRegistrationWorker: Exception occurred - ${e.message}", API)
            Result.retry()
        }
    }
} 