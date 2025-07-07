package com.tanhxpurchase.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.tanhxpurchase.API
import com.tanhxpurchase.util.logd

object DeviceRegistrationManager {
    
    private const val DEVICE_REGISTRATION_WORK_NAME = "device_registration_work"
    
    fun enqueueDeviceRegistration(context: Context) {
        try {
            logd("Enqueuing device registration work", API)
            
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // Yêu cầu có mạng
                .build()
            
            val workRequest = OneTimeWorkRequestBuilder<DeviceRegistrationWorker>()
                .setConstraints(constraints)
                .build()
            
            WorkManager.getInstance(context).enqueueUniqueWork(
                DEVICE_REGISTRATION_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            
            logd("Device registration work enqueued successfully", API)
            
        } catch (e: Exception) {
            logd("Failed to enqueue device registration work: ${e.message}", API)
        }
    }
    
    fun cancelDeviceRegistration(context: Context) {
        try {
            WorkManager.getInstance(context).cancelUniqueWork(DEVICE_REGISTRATION_WORK_NAME)
            logd("Device registration work cancelled", API)
        } catch (e: Exception) {
            logd("Failed to cancel device registration work: ${e.message}", API)
        }
    }
}