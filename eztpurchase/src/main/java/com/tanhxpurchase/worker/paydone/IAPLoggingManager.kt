package com.tanhxpurchase.worker.paydone

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.Gson
import com.tanhxpurchase.API
import com.tanhxpurchase.model.Purchase
import com.tanhxpurchase.util.logd

object IAPLoggingManager {
    
    private const val IAP_LOGGING_WORK_NAME = "iap_logging_work"

    fun enqueueIAPLogging(context: Context, purchase: Purchase) {
        try {
            logd("Enqueuing IAP logging work for product: ${purchase.productId}", API)
            
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
                ExistingWorkPolicy.APPEND,
                workRequest
            )
            
            logd("IAP logging work enqueued successfully", API)
            
        } catch (e: Exception) {
            logd("Failed to enqueue IAP logging work: ${e.message}", API)
        }
    }
}