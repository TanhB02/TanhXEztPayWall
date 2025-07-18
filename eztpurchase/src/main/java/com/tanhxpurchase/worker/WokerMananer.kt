package com.tanhxpurchase.worker

import android.content.Context
import androidx.annotation.Keep
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.Gson
import com.tanhxpurchase.ConstantsPurchase.API
import com.tanhxpurchase.model.iap.InfoScreen
import com.tanhxpurchase.model.iap.Purchase
import com.tanhxpurchase.model.template.TrackingEventRequest
import com.tanhxpurchase.util.logd
import com.tanhxpurchase.worker.paydone.IAPLoggingWorker
import com.tanhxpurchase.worker.registerdevice.DeviceRegistrationWorker
import com.tanhxpurchase.worker.trackingevent.TrackingEventWorker
import java.util.concurrent.TimeUnit

@Keep
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

    fun enqueueIAPLogging(context: Context, purchase: Purchase, infoScreen: InfoScreen) {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val inputData = Data.Builder()
                .putString(IAPLoggingWorker.KEY_PURCHASE_DATA, Gson().toJson(purchase))
                .putString(IAPLoggingWorker.INFO_SCREEN,Gson().toJson(infoScreen))
                .build()

            val workRequest = OneTimeWorkRequestBuilder<IAPLoggingWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "${IAP_LOGGING_WORK_NAME}_${System.currentTimeMillis()}",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        } catch (e: Exception) {
            logd("Failed to enqueue IAP logging work: ${e.message}", API)
        }
    }

    fun enqueueTrackingEvent(
        context: Context,
        trackingEvent: TrackingEventRequest
    ) {
        try {
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
        } catch (e: Exception) {
            logd(
                "TrackingEventManager: Failed to enqueue custom tracking event work: ${e.message}",
                API
            )
        }
    }
}