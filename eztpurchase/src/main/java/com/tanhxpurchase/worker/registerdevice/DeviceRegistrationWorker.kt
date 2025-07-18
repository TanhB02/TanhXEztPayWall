package com.tanhxpurchase.worker.registerdevice

import android.content.Context
import androidx.annotation.Keep
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tanhxpurchase.ConstantsPurchase.API
import com.tanhxpurchase.repository.TemplateRepository
import com.tanhxpurchase.util.ApiResult
import com.tanhxpurchase.util.logd

@Keep
class DeviceRegistrationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val repository = TemplateRepository()

    override suspend fun doWork(): Result {
        return try {
            if (runAttemptCount >= 1)  return Result.failure()

            var finalResult: Result = Result.failure()

            repository.registerDevice(applicationContext).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        logd("DeviceRegistrationWorker: API call in progress...", API)
                    }
                    is ApiResult.Success -> {
                        logd("DeviceRegistrationWorker: Device registration successful - Device ID: ${result.data.data.device.id}", API)
                        finalResult = Result.success()
                        return@collect
                    }
                    is ApiResult.Error -> {
                        logd("DeviceRegistrationWorker: Device registration failed - ${result.message}", API)
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