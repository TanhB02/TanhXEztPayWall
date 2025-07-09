package com.tanhxpurchase.worker.trackingevent

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.tanhxpurchase.ConstantsPurchase.API
import com.tanhxpurchase.hawk.EzTechHawk.accessToken
import com.tanhxpurchase.model.template.TrackingEventRequest
import com.tanhxpurchase.repository.TemplateRepository
import com.tanhxpurchase.util.ApiResult
import com.tanhxpurchase.util.logd
import com.tanhxpurchase.worker.WokerMananer.enqueueDeviceRegistration

class TrackingEventWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val repository = TemplateRepository()

    override suspend fun doWork(): Result {
        return try {
            if (runAttemptCount >= 1) return Result.failure()

            val eventJson = inputData.getString(KEY_EVENT_DATA)
            if (eventJson.isNullOrEmpty()) return Result.failure()

            val trackingEvent = Gson().fromJson(eventJson, TrackingEventRequest::class.java)

            var finalResult: Result = Result.failure()
            if (accessToken.isEmpty()) {
                logd("TrackingEventWorker: Access token not found, registering device first", API)
                enqueueDeviceRegistration(applicationContext)
                return Result.retry()
            }

            logd("TrackingEventWorker: Processing tracking event - Type: ${trackingEvent.type}, Template: ${trackingEvent.templateId}", API)

            repository.logTrackingEvent(trackingEvent).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        logd("TrackingEventWorker: API call in progress...", API)
                    }
                    is ApiResult.Success -> {
                        logd("TrackingEventWorker: Tracking event logged successfully - ID: ${result.data.data.id}", API)
                        finalResult = Result.success()
                        return@collect
                    }
                    is ApiResult.Error -> {
                        logd("TrackingEventWorker: Tracking event logging failed - ${result.message}", API)
                        finalResult = Result.retry()
                        return@collect
                    }
                }
            }

            finalResult
        } catch (e: Exception) {
            logd("TrackingEventWorker: Exception occurred - ${e.message}", API)
            Result.retry()
        }
    }

    companion object {
        const val KEY_EVENT_DATA = "event_data"
    }
}