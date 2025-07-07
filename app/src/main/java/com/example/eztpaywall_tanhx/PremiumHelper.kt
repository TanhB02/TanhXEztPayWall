package com.example.eztpaywall_tanhx

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleCoroutineScope
import com.tanhxpurchase.PayWallIAPDefault
import com.tanhxpurchase.PurchaseUtils
import com.tanhxpurchase.PurchaseUtils.getPayWall
import kotlinx.coroutines.launch

object PremiumHelper {

    fun showDialogPayWall(
        context: Activity,
        lifecycleCoroutineScope: LifecycleCoroutineScope,
        screenName: String = "",
        onFailure: () -> Unit
    ) {
        var url = getPayWall( context.packageName,keyConfig = screenName)
        if (url.isEmpty()) {
            onFailure.invoke()
        }
        lifecycleCoroutineScope.launch {
            PurchaseUtils.showDialogPayWall(
                context,
                lifecycleCoroutineScope = lifecycleCoroutineScope,
                url,
                onUpgradeNow = {
                    startIAP(context, lifecycleCoroutineScope, onReceivedError = {

                    })
                }, watchAdsCallBack = {

                },
                onFailure = {
                    onFailure.invoke()
                })
        }
    }


    fun showBottomSheetPayWall(
        activity: FragmentActivity,
        lifecycleCoroutineScope: LifecycleCoroutineScope,
        screenName: String = "",
        onFailure: () -> Unit,
        watchAdsCallBack: (() -> Unit)? = null,
    ) {
        var url = getPayWall(activity.packageName,keyConfig = screenName)
        if (url.isEmpty()) {
            onFailure.invoke()
            return
        }
        lifecycleCoroutineScope.launch {
            PurchaseUtils.showBottomSheetPayWall(
                activity,
                url,
                onUpgradeNow = {
                    startIAP(activity, lifecycleCoroutineScope, onReceivedError = {

                    })
                },
                watchAdsCallBack = {
                    watchAdsCallBack?.invoke()
                },
                onFailure = {
                    onFailure.invoke()
                })
        }
    }


    fun startIAP(
        activity: Activity,
        lifecycleCoroutineScope: LifecycleCoroutineScope,
        onReceivedError: () -> Unit,
        screenName: String = PayWallIAPDefault,
    ) {
        var url = screenName
        if (url.isEmpty()) {
            url = PayWallIAPDefault
        }
        lifecycleCoroutineScope.launch {
            PurchaseUtils.startActivityIAP(
                context = activity,
                urlWeb = getPayWall(activity.packageName,keyConfig = screenName),
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


}