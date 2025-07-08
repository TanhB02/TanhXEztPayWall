package com.tanhxpurchase.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.Gson
import com.tanhxpurchase.API
import com.tanhxpurchase.model.Purchase
import com.tanhxpurchase.model.template.TrackingEventPayload
import com.tanhxpurchase.model.template.TrackingEventRequest
import com.tanhxpurchase.util.logd
import com.tanhxpurchase.worker.paydone.IAPLoggingWorker
import com.tanhxpurchase.worker.registerdevice.DeviceRegistrationWorker
import com.tanhxpurchase.worker.trackingevent.TrackingEventWorker
import java.util.concurrent.TimeUnit

object WokerMananer {
    private const val DEVICE_REGISTRATION_WORK_NAME = "device_registration_work"
    private const val IAP_LOGGING_WORK_NAME = "iap_logging_work"

    fun enqueueDeviceRegistration(context: Context) {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<DeviceRegistrationWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                DEVICE_REGISTRATION_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        } catch (e: Exception) {
            logd("Failed to enqueue device registration work: ${e.message}", API)
        }
    }


    fun enqueueIAPLogging(context: Context, purchase: Purchase) {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val purchaseJson = Gson().toJson(purchase)
            val inputData = Data.Builder()
                .putString(IAPLoggingWorker.KEY_PURCHASE_DATA, purchaseJson)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<IAPLoggingWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "${IAP_LOGGING_WORK_NAME}_${System.currentTimeMillis()}", // Unique name với timestamp
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        } catch (e: Exception) {
            logd("Failed to enqueue IAP logging work: ${e.message}", API)
        }
    }


    //TODO fun này tracking close, show
    fun enqueueTrackingEvent(
        context: Context,
        paywallConfigId: Int,
        type: Int,
        storeId: Int,
        templateId: Int,
        productId: String? = null
    ) {
        try {
            logd("TrackingEventManager: Enqueuing tracking event - Type: $type, Template: $templateId", API)

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val payload = if (productId != null) {
                TrackingEventPayload(productId = productId)
            } else null

            val trackingEvent = TrackingEventRequest(
                paywallConfigId = paywallConfigId,
                type = type,
                storeId = storeId,
                templateId = templateId,
                payload = payload
            )

            val eventJson = Gson().toJson(trackingEvent)
            val inputData = Data.Builder()
                .putString(TrackingEventWorker.KEY_EVENT_DATA, eventJson)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<TrackingEventWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    10, TimeUnit.SECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)

            logd("TrackingEventManager: Tracking event work enqueued successfully", API)

        } catch (e: Exception) {
            logd("TrackingEventManager: Failed to enqueue tracking event work: ${e.message}", API)
        }
    }


    //TODO fun này tracking click buy
    fun enqueueTrackingEvent(
        context: Context,
        trackingEvent: TrackingEventRequest
    ) {
        try {
            logd("TrackingEventManager: Enqueuing tracking event with custom request", API)

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val eventJson = Gson().toJson(trackingEvent)
            val inputData = Data.Builder()
                .putString(TrackingEventWorker.KEY_EVENT_DATA, eventJson)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<TrackingEventWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    10, TimeUnit.SECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)

            logd("TrackingEventManager: Custom tracking event work enqueued successfully", API)

        } catch (e: Exception) {
            logd("TrackingEventManager: Failed to enqueue custom tracking event work: ${e.message}", API)
        }
    }
}