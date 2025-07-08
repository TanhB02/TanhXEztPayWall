package com.tanhxpurchase.worker.trackingevent

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.orhanobut.hawk.Hawk
import com.tanhxpurchase.ACCESS_TOKEN
import com.tanhxpurchase.API
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
            // ✅ Chỉ retry 1 lần duy nhất
            if (runAttemptCount >= 1) {
                logd("TrackingEventWorker: Already retried once, giving up", API)
                return Result.failure()
            }

            val eventJson = inputData.getString(KEY_EVENT_DATA)
            if (eventJson.isNullOrEmpty()) {
                logd("TrackingEventWorker: Event data not found", API)
                return Result.failure()
            }

            val trackingEvent = Gson().fromJson(eventJson, TrackingEventRequest::class.java)

            var finalResult: Result = Result.failure()
            val accessToken = Hawk.get<String>(ACCESS_TOKEN, null)
            if (accessToken.isNullOrEmpty()) {
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