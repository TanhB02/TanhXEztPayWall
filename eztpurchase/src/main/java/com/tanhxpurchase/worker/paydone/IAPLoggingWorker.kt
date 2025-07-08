package com.tanhxpurchase.worker.paydone

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.tanhxpurchase.API
import com.tanhxpurchase.model.Purchase
import com.tanhxpurchase.model.template.IAPPurchase
import com.tanhxpurchase.repository.TemplateRepository
import com.tanhxpurchase.util.ApiResult
import com.tanhxpurchase.util.logd

class IAPLoggingWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val repository = TemplateRepository()

    override suspend fun doWork(): Result {
        return try {
            val purchaseJson = inputData.getString(KEY_PURCHASE_DATA)
            if (purchaseJson.isNullOrEmpty()) {
                logd("IAPLoggingWorker: Purchase data not found", API)
                return Result.failure()
            }

            logd("IAPLoggingWorker: Processing IAP logging", API)

            // Convert JSON back to Purchase object and create IAPPurchase
            val purchase = Gson().fromJson(purchaseJson, Purchase::class.java)
            val iapPurchase = IAPPurchase.from(purchase)

            var finalResult: Result = Result.failure()

            repository.logIAPPurchase(iapPurchase).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        logd("IAPLoggingWorker: API call in progress...", API)
                    }
                    is ApiResult.Success -> {
                        logd("IAPLoggingWorker: IAP logging successful - ID: ${result.data.data.id}", API)
                        finalResult = Result.success()
                        return@collect
                    }
                    is ApiResult.Error -> {
                        logd("IAPLoggingWorker: IAP logging failed - ${result.message}", API)
                        finalResult = Result.retry()
                        return@collect
                    }
                }
            }

            finalResult
        } catch (e: Exception) {
            logd("IAPLoggingWorker: Exception occurred - ${e.message}", API)
            Result.retry()
        }
    }

    companion object {
        const val KEY_PURCHASE_DATA = "purchase_data"
    }
} 