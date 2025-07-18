package com.tanhxpurchase

import android.content.Context
import androidx.annotation.Keep
import com.tanhxpurchase.ConstantsPurchase.BUY_PAYWALL
import com.tanhxpurchase.ConstantsPurchase.CLOSE_PAYWALL
import com.tanhxpurchase.ConstantsPurchase.SHOW_PAYWALL
import com.tanhxpurchase.ConstantsPurchase.UPGRADE_PAYWALL
import com.tanhxpurchase.ConstantsPurchase.WATCHADS_PAYWALL
import com.tanhxpurchase.util.TemplateDataManager.createTrackingEventFromValue
import com.tanhxpurchase.util.logD
import com.tanhxpurchase.worker.WokerMananer.enqueueTrackingEvent

@Keep
object TrackingUtils {

    @Keep
    fun trackingShowScreen(context: Context, value: String, isFromTo: String) {
        createTrackingEventFromValue(value, isFromTo, SHOW_PAYWALL)?.let {
            enqueueTrackingEvent(context, it)
        }
    }

    @Keep
    fun trackingCloseScreen(context: Context, value: String, isFromTo: String) {
        createTrackingEventFromValue(value, isFromTo, CLOSE_PAYWALL)?.let {
            enqueueTrackingEvent(context, it)
        }
    }

    @Keep
    fun trackingBuy(context: Context, value: String, productId: String, isFromTo: String) {
        createTrackingEventFromValue(value, isFromTo, BUY_PAYWALL, productId)?.let {
            logD("TANHXXXX =>>>>> it:${it}")
            enqueueTrackingEvent(context, it)
        }
    }

    @Keep
    fun trackingWatchAds(context: Context, value: String, isFromTo: String) {
        createTrackingEventFromValue(value, isFromTo, WATCHADS_PAYWALL)?.let {
            enqueueTrackingEvent(context, it)
        }
    }

    @Keep
    fun trackingUpgrade(context: Context, value: String, isFromTo: String) {
        createTrackingEventFromValue(value, isFromTo, UPGRADE_PAYWALL)?.let {
            enqueueTrackingEvent(context, it)
        }
    }

}