package com.tanhxpurchase

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleCoroutineScope
import com.tanhxpurchase.activity.IAPWebViewActivity
import com.tanhxpurchase.billing.BillingService
import com.tanhxpurchase.dialog.DialogPremium
import com.tanhxpurchase.dialog.PremiumBottomSheet
import com.tanhxpurchase.listeners.PurchaseUpdateListener
import com.tanhxpurchase.model.Purchase
import com.tanhxpurchase.sharepreference.EzTechPreferences
import com.tanhxpurchase.sharepreference.EzTechPreferences.countryCode
import com.tanhxpurchase.sharepreference.EzTechPreferences.isDarkMode
import com.tanhxpurchase.sharepreference.EzTechPreferences.isFreeTrial
import com.tanhxpurchase.util.TemplateDataManager.getTemplateUrlByName
import com.tanhxpurchase.util.logD
import com.tanhxpurchase.util.logFirebaseEvent
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
        EzTechPreferences.init(context)
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

    fun showDialogPayWall(context: Context, lifecycleCoroutineScope: LifecycleCoroutineScope, url: String, onUpgradeNow: () -> Unit,watchAdsCallBack: () -> Unit, onFailure: () -> Unit) {
        val dialog = DialogPremium(
            context = context,
            lifecycles = lifecycleCoroutineScope,
            url = url,
            onUpgradeCallback = {
                onUpgradeNow()
            },
            watchAdsCallBack = {

            },
            onFailureCallback = {
                onFailure()
            }
        )
        dialog.show()
    }

    fun showBottomSheetPayWall(activity: FragmentActivity, url: String, onUpgradeNow: () -> Unit,watchAdsCallBack: () -> Unit, onFailure: () -> Unit) {
        val bottomSheet = PremiumBottomSheet.newInstance(
            url = url,
            onUpgradeCallback = {
                onUpgradeNow()
            },
            watchAdsCallBack = {
                watchAdsCallBack()
            },
            onFailureCallback = {
                onFailure()
            }
        )
        bottomSheet.show(activity.supportFragmentManager, "PremiumBottomSheet")
    }

    fun getPayWall(packageName : String,keyConfig: String): String {
        val url = getTemplateUrlByName(packageName,keyConfig)
        logD("TANHXXXX =>>>>> url:${url}")
        return url
    }


    fun addInitBillingFinishListener(listener: () -> Unit) {
        initFinishListener.add(listener)
        if (isAvailable) listener()
    }

    fun buy(
        activity: Activity, id: String,
        onPurchaseFailure: (code: Int, errorMsg: String?) -> Unit = { _, _ -> },
        onOwnedProduct: (productId: String) -> Unit = {},
        onUserCancelBilling: () -> Unit = {},
        onPurchaseSuccess: (purchases: Purchase) -> Unit = {},
    ) {
        mPurchaseUpdateListener = object : PurchaseUpdateListener {
            override fun onPurchaseSuccess(purchases: Purchase) {
                onPurchaseSuccess(purchases)
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
        urlWeb: String,
        onPurchaseSuccess: (() -> Unit)? = null,
        onReceivedError: (() -> Unit)? = null,
        onCloseClicked: (() -> Unit)? = null,
    ) {
        checkFreeTrial()
        IAPWebViewActivity.start(
            context,
            urlWeb,
            onPurchaseSuccess = onPurchaseSuccess,
            onReceivedError = onReceivedError,
            onCloseClicked = onCloseClicked,
        )
    }

    fun setCountryCode(countryCodeX: String) {
        countryCode = countryCodeX
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
        if (getPrice(EzTechPreferences.producFreetrial) != "") {
            isFreeTrial = true
            return true
        } else {
            isFreeTrial = false
            return false
        }
    }

    fun setDarkMode(isDarkModeX: Boolean) {
        isDarkMode = isDarkModeX
    }

    override fun onPurchaseSuccess(purchases: Purchase) {
        super.onPurchaseSuccess(purchases)
        logFirebaseEvent("${purchases.productId}_sub_ok")
        mPurchaseUpdateListener?.onPurchaseSuccess(purchases)
        _isRemoveAds.value = isRemoveAds()
    }

    override fun onPurchaseFailure(code: Int, errorMsg: String?) {
        super.onPurchaseFailure(code, errorMsg)
        try {
            logFirebaseEvent("sub_failed")
            mPurchaseUpdateListener?.onPurchaseFailure(code, errorMsg)
        } catch (ex: Exception) {
            logFirebaseEvent("sub_failed_trycatch")
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


    class Builder() {
        private val mSubscriptions: MutableList<String> = mutableListOf()
        private val mOneTimeProducts: MutableList<String> = mutableListOf()
        private val mConsumableProducts: MutableList<String> = mutableListOf()
        private val mRemoveAds: MutableList<String> = mutableListOf()

        /**
         * product ID của gói subscription
         */
        fun subscriptions(subscriptions: List<String>) = apply {
            mSubscriptions.clear()
            mSubscriptions.addAll(subscriptions)
        }

        fun subscriptions(vararg subscriptions: String) = apply {
            mSubscriptions.clear()
            mSubscriptions.addAll(subscriptions)
        }

        /**
         * sản phẩm chỉ mua một lần, không lặp lại.
         */
        fun oneTimeProducts(oneTimeProducts: List<String>) = apply {
            mOneTimeProducts.clear()
            mOneTimeProducts.addAll(oneTimeProducts)
        }

        fun oneTimeProducts(vararg oneTimeProducts: String) = apply {
            mOneTimeProducts.clear()
            mOneTimeProducts.addAll(oneTimeProducts)
        }

        /**
         * người dùng mua rồi dùng hết và có thể mua lại
         */
        fun consumableProducts(consumableProducts: List<String>) = apply {
            mConsumableProducts.clear()
            mConsumableProducts.addAll(consumableProducts)
        }

        fun consumableProducts(vararg consumableProducts: String) = apply {
            mConsumableProducts.clear()
            mConsumableProducts.addAll(consumableProducts)
        }

        /**
         * Các gói xóa ads
         */

        fun removeAds(removeAds: List<String>) = apply {
            mRemoveAds.clear()
            mRemoveAds.addAll(removeAds)
        }

        fun removeAds(vararg removeAds: String) = apply {
            mRemoveAds.clear()
            mRemoveAds.addAll(removeAds)
        }


        fun build() {
            addSubscriptionAndProduct(
                mSubscriptions,
                mOneTimeProducts,
                mConsumableProducts
            )
            setListRemoveAdsId(mRemoveAds)
        }
    }
}