package com.tanhxpurchase

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleCoroutineScope
import com.tanhxpurchase.ConstantsPurchase.EZT_Purchase
import com.tanhxpurchase.activity.IAPWebViewActivity
import com.tanhxpurchase.billing.BillingService
import com.tanhxpurchase.dialog.PremiumDialog
import com.tanhxpurchase.dialog.PremiumBottomSheet
import com.tanhxpurchase.listeners.PurchaseUpdateListener
import com.tanhxpurchase.model.iap.Purchase
import com.tanhxpurchase.hawk.EzTechHawk.countryCode
import com.tanhxpurchase.hawk.EzTechHawk.isDarkMode
import com.tanhxpurchase.hawk.EzTechHawk.isFreeTrial
import com.tanhxpurchase.hawk.EzTechHawk.privacyPolicy
import com.tanhxpurchase.hawk.EzTechHawk.producFreetrial
import com.tanhxpurchase.hawk.EzTechHawk.termsOfService
import com.tanhxpurchase.hawk.EzTechHawk.timeOutPayWall
import com.tanhxpurchase.model.iap.InfoScreen
import com.tanhxpurchase.util.TemplateDataManager.getTemplateUrlByName
import com.tanhxpurchase.util.logD
import com.tanhxpurchase.util.logd
import com.tanhxpurchase.worker.WokerMananer.enqueueIAPLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

object PurchaseUtils : PurchaseUpdateListener {

    private val _isRemoveAds = MutableStateFlow(false)
    val isRemoveAds: Flow<Boolean> get() = _isRemoveAds

    fun getInstance() = this

    private val billingService by lazy {
        BillingService.getInstance().apply {
            addPurchaseUpdateListener(this@PurchaseUtils)
            onInitBillingFinish = {
                onInitBillingFinish()
            }
        }
    }

    val ownedProducts by lazy { billingService.ownedProducts }

    private val initFinishListener = arrayListOf<() -> Unit>()
    private var mPurchaseUpdateListener: PurchaseUpdateListener? = null
    private var initBillingFinish = false
    private var isAvailable = false

    fun init(context: Context) {
        billingService.initBillingClient(context)
    }

    private fun onInitBillingFinish() {
        initBillingFinish = true
        isAvailable = true
        CoroutineScope(Dispatchers.IO).launch {
            delay(200)
            initFinishListener.forEach {
                it.invoke()
            }
        }
    }

    fun showDialogPayWall(
        context: Activity,
        screenName : String,
        isFromTo : String,
        lifecycleCoroutineScope: LifecycleCoroutineScope,
        onUpgradeNow: () -> Unit,
        watchAdsCallBack: () -> Unit,
        onFailure: () -> Unit
    ) {
        val dialog = PremiumDialog(
            context = context,
            lifecycles = lifecycleCoroutineScope,
            isFromTo = isFromTo,
            screenName = screenName,
            onUpgradeCallback = {
                onUpgradeNow()
            },
            watchAdsCallBack = {
                watchAdsCallBack()
            },
            onFailureCallback = {
                logd("showDialogPayWall onFailureCallback", EZT_Purchase)
                onFailure()
            }
        )
        dialog.show()
    }

    fun showBottomSheetPayWall(
        activity: FragmentActivity,
        screenName: String,
        isFromTo : String,
        onUpgradeNow: () -> Unit,
        watchAdsCallBack: () -> Unit,
        onFailure: () -> Unit
    ) {
        val bottomSheet = PremiumBottomSheet.newInstance(
            screenName = screenName,
            isFromTo = isFromTo,
            onUpgradeCallback = {
                onUpgradeNow()
            },
            watchAdsCallBack = {
                watchAdsCallBack()
            },
            onFailureCallback = {
                logd("showBottomSheetPayWall onFailureCallback", EZT_Purchase)
                onFailure()
            }
        )
        bottomSheet.show(activity.supportFragmentManager, "PremiumBottomSheet")
    }

    fun getPayWall(packageName: String, keyConfig: String): String {
        val url = getTemplateUrlByName(packageName, keyConfig)
        logD("TANHXXXX =>>>>> url:${url}")
        return url
    }


    fun addInitBillingFinishListener(listener: () -> Unit) {
        initFinishListener.add(listener)
        if (isAvailable) listener()
    }

    fun buy(
        activity: Activity, id: String,infoScreen: InfoScreen,
        onPurchaseFailure: (code: Int, errorMsg: String?) -> Unit = { _, _ -> },
        onOwnedProduct: (productId: String) -> Unit = {},
        onUserCancelBilling: () -> Unit = {},
        onPurchaseSuccess: (purchases: Purchase) -> Unit = {},
    ) {
        mPurchaseUpdateListener = object : PurchaseUpdateListener {
            override fun onPurchaseSuccess(purchases: Purchase) {
                onPurchaseSuccess(purchases)
                logD("TANHXXXX =>>>>> purchases:${purchases}")
                logD("TANHXXXX =>>>>> infoScreen: ${infoScreen}")
                enqueueIAPLogging(activity.applicationContext, purchases,infoScreen)
            }

            override fun onPurchaseFailure(code: Int, errorMsg: String?) {
                onPurchaseFailure(code, errorMsg)
            }

            override fun onOwnedProduct(productId: String) {
                onOwnedProduct(productId)
            }

            override fun onUserCancelBilling() {
                onUserCancelBilling()
            }
        }
        billingService.buy(activity, id)
    }

    fun startActivityIAP(
        context: Activity,
        screenName: String,
        isFromTo : String,
        onPurchaseSuccess: (() -> Unit)? = null,
        onReceivedError: (() -> Unit)? = null,
        onCloseClicked: (() -> Unit)? = null,
    ) {
        checkFreeTrial()
        IAPWebViewActivity.start(
            activity = context,
            screenName = screenName,
            isFromTo = isFromTo,
            onPurchaseSuccess = onPurchaseSuccess,
            onReceivedError = {
                onReceivedError?.invoke()
            },
            onCloseClicked = onCloseClicked,
        )
    }

    fun getPrice(id: String): String = billingService.getPrice(id)

    fun getPriceWithoutCurrency(id: String): Float = billingService.getPriceWithoutCurrency(id)

    fun getCurrency(id: String): String = billingService.getCurrency(id)

    fun getDiscountPrice(id: String): String = billingService.getDiscountPrice(id)

    fun setActionPurchase(actionSuccess: () -> Unit, actionFailed: () -> Unit) {
        if (isRemoveAds()) {
            actionSuccess()
        } else {
            actionFailed()
        }
    }

    fun isRemoveAds() = billingService.isRemoveAds()

    fun checkPurchased() = billingService.checkPurchased()

    fun addSubscriptionAndProduct(
        listSubscriptionId: List<String> = listOf(),
        listOnetimeProductId: List<String> = listOf(),
        listConsumableProductId: List<String> = listOf()
    ) {
        billingService.addAllSubsAndProduct(
            listSubscriptionId,
            listOnetimeProductId,
            listConsumableProductId
        )
    }

    fun setListRemoveAdsId(list: List<String>) {
        billingService.setListRemoveAdsId(list)
    }


    fun getPayload(): String {
        Log.d(EZT_Purchase, "getPayload: ${billingService.getStandardJsonPayload()}")
        return billingService.getStandardJsonPayload()
    }

    fun checkFreeTrial(): Boolean {
        if (getPrice(producFreetrial) != "") {
            isFreeTrial = true
            return true
        } else {
            isFreeTrial = false
            return false
        }
    }

    fun setCountryCode(countryCodeX: String) {
        countryCode = countryCodeX
    }

    fun setDarkMode(isDarkModeX: Boolean) {
        isDarkMode = isDarkModeX
    }

    fun setLinkPolicy(url : String){
        privacyPolicy = url
    }

    fun setLinkTerms(url: String){
        termsOfService = url
    }

    fun setTimeOutPayWall(timeOut : Long){
        timeOutPayWall = timeOut
    }

    override fun onPurchaseSuccess(purchases: Purchase) {
        super.onPurchaseSuccess(purchases)
        mPurchaseUpdateListener?.onPurchaseSuccess(purchases)
        _isRemoveAds.value = isRemoveAds()
    }

    override fun onPurchaseFailure(code: Int, errorMsg: String?) {
        super.onPurchaseFailure(code, errorMsg)
        try {
            mPurchaseUpdateListener?.onPurchaseFailure(code, errorMsg)
        } catch (ex: Exception) {
            logD("TANHXXXX =>>>>> error: ${ex}")
        }
    }

    override fun onOwnedProduct(productId: String) {
        super.onOwnedProduct(productId)
        mPurchaseUpdateListener?.onOwnedProduct(productId)
        _isRemoveAds.value = isRemoveAds()
    }

    override fun onUserCancelBilling() {
        super.onUserCancelBilling()
        mPurchaseUpdateListener?.onUserCancelBilling()
    }


}