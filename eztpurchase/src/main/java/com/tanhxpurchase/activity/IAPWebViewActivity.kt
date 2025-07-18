package com.tanhxpurchase.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.webkit.WebView
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.lib.tanhx_purchase.R
import com.lib.tanhx_purchase.databinding.ActivityIapWebViewBinding
import com.tanhxpurchase.ConstantsPurchase.Android
import com.tanhxpurchase.ConstantsPurchase.Base_Plan_Id_1Monthly
import com.tanhxpurchase.ConstantsPurchase.Base_Plan_Id_6Monthly
import com.tanhxpurchase.ConstantsPurchase.Base_Plan_Id_Yearly
import com.tanhxpurchase.ConstantsPurchase.Base_Plan_Id_Yearly_Trial
import com.tanhxpurchase.ConstantsPurchase.CLOSE
import com.tanhxpurchase.ConstantsPurchase.PAYLOAD_RECEIVED
import com.tanhxpurchase.ConstantsPurchase.POLICY
import com.tanhxpurchase.ConstantsPurchase.RESTORE
import com.tanhxpurchase.ConstantsPurchase.Restore
import com.tanhxpurchase.ConstantsPurchase.TERMS
import com.tanhxpurchase.PurchaseUtils
import com.tanhxpurchase.PurchaseUtils.getPayWall
import com.tanhxpurchase.TrackingUtils.trackingBuy
import com.tanhxpurchase.TrackingUtils.trackingCloseScreen
import com.tanhxpurchase.TrackingUtils.trackingShowScreen
import com.tanhxpurchase.base.BaseActivity
import com.tanhxpurchase.customview.ItemIAPView
import com.tanhxpurchase.hawk.EzTechHawk.isFreeTrial
import com.tanhxpurchase.hawk.EzTechHawk.privacyPolicy
import com.tanhxpurchase.hawk.EzTechHawk.termsOfService
import com.tanhxpurchase.hawk.EzTechHawk.timeOutPayWall
import com.tanhxpurchase.listeners.IAPWebInterface
import com.tanhxpurchase.listeners.IAPWebViewCallback
import com.tanhxpurchase.model.iap.InfoScreen
import com.tanhxpurchase.util.TemplateDataManager.createTrackingEventFromValue
import com.tanhxpurchase.util.clickeffect.setOnClickShrinkEffectListener
import com.tanhxpurchase.util.configureWebViewSettings
import com.tanhxpurchase.util.logD
import com.tanhxpurchase.util.logd
import com.tanhxpurchase.util.loge
import com.tanhxpurchase.util.openLink
import com.tanhxpurchase.util.setTextHtml
import com.tanhxpurchase.util.setTextRes
import com.tanhxpurchase.util.setupWebViewClientWithTimeout
import com.tanhxpurchase.util.shineAnimation
import com.tanhxpurchase.util.toGone
import com.tanhxpurchase.util.toVisible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class IAPWebViewActivity : BaseActivity<ActivityIapWebViewBinding>(), IAPWebViewCallback {
    private val viewModel: IAPWebViewViewModel by viewModels()
    private var currentWebView: WebView? = null
    private var injectionScript: String? = null
    private var productIDSelect: String = Base_Plan_Id_Yearly
    private lateinit var urlWeb: String
    private lateinit var screenName: String
    private lateinit var isFromTo: String
    private var jobTimeOut: Job? = null
    private var infoScreen : InfoScreen = InfoScreen()

    interface IAPCallback {
        fun onPurchaseSuccess()
        fun onCloseClicked()
        fun onReceivedError()
    }

    companion object {
        private var iapCallback: IAPCallback? = null
        private const val SCREEN_NAME = "screen_name"
        private const val IS_FROM_TO = "is_from_to"
        fun start(
            activity: Activity,
            screenName: String,
            isFromTo: String,
            onPurchaseSuccess: (() -> Unit)? = null,
            onReceivedError: (() -> Unit)? = null,
            onCloseClicked: (() -> Unit)? = null,
        ) {
            iapCallback = object : IAPCallback {

                override fun onPurchaseSuccess() {
                    onPurchaseSuccess?.invoke()
                }

                override fun onCloseClicked() {
                    onCloseClicked?.invoke()
                }

                override fun onReceivedError() {
                    onReceivedError?.invoke()
                }
            }

            Intent(activity, IAPWebViewActivity::class.java).apply {
                putExtra(SCREEN_NAME, screenName)
                putExtra(IS_FROM_TO, isFromTo)
                activity.startActivity(this)
            }
        }
    }

    override fun getDataBinding() = ActivityIapWebViewBinding.inflate(layoutInflater)

    override fun observeViewModel() {}

    override fun onBackPressed() {
        super.onBackPressed()
        handleCloseAction()
    }

    private fun TimeOutWithNoPrice() {
        jobTimeOut = lifecycleScope.launch {
            delay(timeOutPayWall)
            if (!isDestroyed && !isFinishing) {
                onFailureCallbackWithError()
            }
        }
    }


    override fun initView() {
        shineAnimation(binding.shine)
        screenName = intent.getStringExtra(SCREEN_NAME).toString()
        isFromTo = intent.getStringExtra(IS_FROM_TO).toString()
        urlWeb = getPayWall(packageName, screenName)
        if (urlWeb.isBlank() || urlWeb == "null") {
            onFailureCallbackWithError()
            return
        }
        injectionScript = viewModel.createInjectionScript(this@IAPWebViewActivity)
        setupWebView()
        try {
            currentWebView?.loadUrl(urlWeb)
        } catch (e: Exception) {
            onFailureCallbackWithError()
            return
        }

        if (PurchaseUtils.checkFreeTrial()) {
            productIDSelect = Base_Plan_Id_Yearly_Trial
            binding.tvContinue.setTextRes(R.string.start_7_day_free_trial)
            binding.tvTitlePackage.setTextHtml(
                R.string.seven_day_free_trial,
                PurchaseUtils.getPrice(Base_Plan_Id_Yearly_Trial)
            )
        }
        TimeOutWithNoPrice()
        trackingShowScreen(this@IAPWebViewActivity, screenName, isFromTo)
        createTrackingEventFromValue(screenName, isFromTo, 0)?.let {
            infoScreen.templateId = it.templateId
            infoScreen.paywallConfigId = it.paywallConfigId
            infoScreen.storeId = it.storeId
        }
        logD("TANHXXXX =>>>>> infoScreen:${infoScreen}")
    }

    override fun onResume() {
        super.onResume()
    }

    private fun setupWebView() {
        try {
            currentWebView = createWebView()
                .apply {
                    setupWebViewClientWithTimeout(lifecycleScope, onPageFinished = { webView, url ->
                        try {
                            if (injectionScript != null && !isDestroyed && !isFinishing) {
                                evaluateJavascript(injectionScript!!, null)
                            }
                        } catch (e: Exception) {
                            loge("TANHXXXX => Error evaluating JavaScript: ${e.message}")
                        }
                    }, onReceivedError = {
                        onFailureCallbackWithError()
                    })
                }
            binding.webViewContainer.addView(currentWebView)
        } catch (e: Exception) {
            onFailureCallbackWithError()
            binding.webViewContainer.toGone()
            binding.ctBackUp.toVisible()
        }
    }

    /**
     * TanhX - UI backup đang là của VPN nên em hash code nhé ^^
     */
    private fun onFailureCallback(): Boolean {
        return packageName == "com.ezt.vpn2"
    }

    private fun onFailureCallbackWithError() {
        if (isDestroyed || isFinishing) return
        iapCallback?.onReceivedError()
        if (onFailureCallback()) {
            binding.webViewContainer.toGone()
            binding.ctBackUp.toVisible()
            binding.ctLoadding.toGone()
        } else {
            finish()
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView(): WebView {
        return try {
            val webView = WebView(this)
            webView.apply {
                configureWebViewSettings(this)
                addJavascriptInterface(IAPWebInterface(this@IAPWebViewActivity), Android)
            }
        } catch (e: Exception) {
            onFailureCallbackWithError()
            binding.webViewContainer.toGone()
            binding.ctBackUp.toVisible()
            throw e
        }
    }

    // @formatter:off
    override fun onUserClickListener(data: String) {
        if (isDestroyed || isFinishing) return
        lifecycleScope.launch(Dispatchers.Main) {
            when (data.replace("\"", "")) {
                CLOSE -> {
                    handleCloseAction()
                }
                POLICY -> { openLink(privacyPolicy) }
                TERMS -> { openLink(termsOfService) }
                RESTORE -> { openLink(Restore) }
                PAYLOAD_RECEIVED ->{
                    lifecycleScope.launch(Dispatchers.Main) {
                            jobTimeOut?.cancel()
                            binding.webViewContainer.toVisible()
                            binding.ctBackUp.toGone()
                            binding.ctLoadding.toGone()
                    }
                }
                else -> {
                    productIDSelect = data
                    trackingBuy(this@IAPWebViewActivity,screenName,productIDSelect,isFromTo)
                    buyProduct(productIDSelect)
                }
            }
        }
    }
    // @formatter:on

    override fun onDestroy() {
        super.onDestroy()
        jobTimeOut?.cancel()
    }

    private fun buyProduct(producID: String) {
        if (isDestroyed || isFinishing) return
        PurchaseUtils.buy(
            this,
            producID,
            infoScreen,
            onPurchaseSuccess = { purchase ->
                if (!isDestroyed && !isFinishing) {
                    iapCallback?.onPurchaseSuccess()
                    finish()
                }
            },
            onPurchaseFailure = { code, errorMsg ->

            }
        )
    }

    private fun selectPlan(selectedView: ItemIAPView, planId: String) {
        binding.apply {
            listOf(view1Month, view6Month, viewYear).forEach { it.selectView(it == selectedView) }
            if (planId == Base_Plan_Id_1Monthly || planId == Base_Plan_Id_6Monthly || planId == Base_Plan_Id_Yearly) {
                binding.tvTitlePackage.setTextRes(R.string.no_comitent)
                binding.tvContinue.setTextRes(R.string.continue_)
            } else {
                binding.tvTitlePackage.setTextHtml(
                    R.string.seven_day_free_trial,
                    PurchaseUtils.getPrice(Base_Plan_Id_Yearly_Trial)
                )
                binding.tvContinue.setTextRes(R.string.start_7_day_free_trial)
            }
        }
        productIDSelect = planId
    }

    override fun addEvent() {
        binding.apply {
            ivClose.setOnClickShrinkEffectListener {
                handleCloseAction()
            }

            tvPrivacyPolicy.setOnClickShrinkEffectListener {
                openLink(privacyPolicy)
            }

            tvTermsOfUse.setOnClickShrinkEffectListener {
                openLink(termsOfService)
            }

            tvRestore.setOnClickShrinkEffectListener {
                openLink(Restore)
            }

            view1Month.setOnClickShrinkEffectListener {
                selectPlan(view1Month, Base_Plan_Id_1Monthly)
            }

            view6Month.setOnClickShrinkEffectListener {
                selectPlan(view6Month, Base_Plan_Id_6Monthly)
            }

            viewYear.setOnClickShrinkEffectListener {
                selectPlan(
                    viewYear,
                    if (isFreeTrial) Base_Plan_Id_Yearly_Trial else Base_Plan_Id_Yearly
                )
            }

            clContinue.setOnClickShrinkEffectListener {
                buyProduct(productIDSelect)
            }
        }
    }

    private fun handleCloseAction() {
        if (!isDestroyed && !isFinishing) {
            iapCallback?.onCloseClicked()
        }
        trackingCloseScreen(this, screenName, isFromTo)
        finish()
    }

}