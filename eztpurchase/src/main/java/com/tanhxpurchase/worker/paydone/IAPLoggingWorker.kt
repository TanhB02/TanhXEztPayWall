package com.tanhxpurchase.worker.paydone

import android.content.Context
import androidx.annotation.Keep
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.tanhxpurchase.ConstantsPurchase.API
import com.tanhxpurchase.hawk.EzTechHawk.accessToken
import com.tanhxpurchase.hawk.EzTechHawk.configIAP
import com.tanhxpurchase.model.iap.Purchase
import com.tanhxpurchase.model.template.IAPPurchase
import com.tanhxpurchase.repository.TemplateRepository
import com.tanhxpurchase.util.ApiResult
import com.tanhxpurchase.util.logD
import com.tanhxpurchase.util.logd
import com.tanhxpurchase.worker.WokerMananer.enqueueDeviceRegistration

@Keep
class IAPLoggingWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    private val repository = TemplateRepository()

    override suspend fun doWork(): Result {
        return try {
            if (runAttemptCount >= 1) return Result.failure()

            val purchaseJson = inputData.getString(KEY_PURCHASE_DATA)

            if (purchaseJson.isNullOrEmpty()) return Result.failure()
            val iapPurchase = IAPPurchase.from(Gson().fromJson(purchaseJson, Purchase::class.java))
            val infoScreen = inputData.getString(INFO_SCREEN)?.let {
                Gson().fromJson(it, com.tanhxpurchase.model.iap.InfoScreen::class.java)
            }
            if (configIAP == null) return Result.failure()

            if (iapPurchase.payload?.productId in configIAP!!.oneTimeProducts) iapPurchase.type = 2
            iapPurchase.templateId = infoScreen?.templateId ?: 0
            iapPurchase.paywallConfigId = infoScreen?.paywallConfigId ?: 0
            iapPurchase.storeId = infoScreen?.storeId ?: 0

            var finalResult: Result = Result.failure()
            if (accessToken.isEmpty()) {
                enqueueDeviceRegistration(applicationContext)
                return Result.retry()
            }

            repository.logIAPPurchase(iapPurchase).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        logd("IAPLoggingWorker: API call in progress...", API)
                    }

                    is ApiResult.Success -> {
                        logd("IAPLoggingWorker: successful - ID: ${result.data.data.id}", API)
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
        const val INFO_SCREEN = "info_screen"
    }
}