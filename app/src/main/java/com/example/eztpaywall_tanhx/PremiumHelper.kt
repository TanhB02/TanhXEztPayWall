package com.example.eztpaywall_tanhx

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleCoroutineScope
import com.tanhxpurchase.PurchaseUtils

object PremiumHelper {

    fun showDialogPayWall(
        context: Activity,
        lifecycleCoroutineScope: LifecycleCoroutineScope,
        screenName: String,
        isFromTo: String,
        onFailure: () -> Unit
    ) {
        PurchaseUtils.showDialogPayWall(
            context = context,
            screenName = screenName,
            isFromTo = isFromTo,
            lifecycleCoroutineScope = lifecycleCoroutineScope,
            onUpgradeNow = {
                startIAP(context, "PayWallIAPDefault",isFromTo, onReceivedError = {

                })
            }, watchAdsCallBack = {

            },
            onFailure = {
                onFailure.invoke()
            })
    }


    fun showBottomSheetPayWall(
        activity: FragmentActivity,
        screenName: String,
        isFromTo : String,
        onFailure: () -> Unit,
        watchAdsCallBack: (() -> Unit)? = null,
    ) {
        PurchaseUtils.showBottomSheetPayWall(
            activity,
            screenName,
            isFromTo = isFromTo,
            onUpgradeNow = {
                startIAP(activity,"PayWallIAPDefault",isFromTo, onReceivedError = {

                })
            },
            watchAdsCallBack = {
                watchAdsCallBack?.invoke()
            },
            onFailure = {
                onFailure.invoke()
            })
    }


    fun startIAP(
        activity: Activity,
        screenName: String,
        isFromTo : String,
        onReceivedError: () -> Unit,
    ) {
        PurchaseUtils.startActivityIAP(
            context = activity,
            screenName = screenName,
            isFromTo = isFromTo,
            onPurchaseSuccess = {

            },
            onReceivedError = {
                onReceivedError()
            },
            onCloseClicked = {

            }
        )
    }
}