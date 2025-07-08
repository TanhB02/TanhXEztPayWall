package com.tanhxpurchase.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.webkit.WebView
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.lib.tanhx_purchase.R
import com.lib.tanhx_purchase.databinding.ActivityIapWebViewBinding
import com.tanhxpurchase.Android
import com.tanhxpurchase.Base_Plan_Id_1Monthly
import com.tanhxpurchase.Base_Plan_Id_6Monthly
import com.tanhxpurchase.Base_Plan_Id_Yearly
import com.tanhxpurchase.Base_Plan_Id_Yearly_Trial
import com.tanhxpurchase.CLOSE
import com.tanhxpurchase.PAYLOAD_RECEIVED
import com.tanhxpurchase.POLICY
import com.tanhxpurchase.Privacy_Policy
import com.tanhxpurchase.PurchaseUtils
import com.tanhxpurchase.RESTORE
import com.tanhxpurchase.Restore
import com.tanhxpurchase.TERMS
import com.tanhxpurchase.Terms
import com.tanhxpurchase.TimeOut_PayWall_No_Price
import com.tanhxpurchase.base.BaseActivity
import com.tanhxpurchase.customview.ItemIAPView
import com.tanhxpurchase.listeners.IAPWebInterface
import com.tanhxpurchase.listeners.IAPWebViewCallback
import com.tanhxpurchase.sharepreference.EzTechPreferences.isFreeTrial
import com.tanhxpurchase.util.clickeffect.setOnClickShrinkEffectListener
import com.tanhxpurchase.util.configureWebViewSettings
import com.tanhxpurchase.util.logD
import com.tanhxpurchase.util.logd
import com.tanhxpurchase.util.openLink
import com.tanhxpurchase.util.setTextHtml
import com.tanhxpurchase.util.setTextRes
import com.tanhxpurchase.util.setupWebViewClientWithTimeout
import com.tanhxpurchase.util.shineAnimation
import com.tanhxpurchase.util.toGone
import com.tanhxpurchase.util.toVisible
import com.tanhxpurchase.worker.paydone.IAPLoggingManager
import com.tanhxpurchase.worker.registerdevice.DeviceRegistrationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class IAPWebViewActivity : BaseActivity<ActivityIapWebViewBinding>(), IAPWebViewCallback {
    private val viewModel: IAPWebViewViewModel by viewModels()
    private var currentWebView: WebView? = null
    private var injectionScript: String? = null
    private var productIDSelect: String = Base_Plan_Id_Yearly
    private var urlWeb: String = ""
    private var jobTimeOut: Job? = null

    interface IAPCallback {
        fun onPurchaseSuccess()
        fun onCloseClicked()
        fun onReceivedError()
    }

    companion object {
        private var iapCallback: IAPCallback? = null
        const val URL = "URL"
        fun start(
            activity: Activity,
            baseUrl: String,
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
                putExtra(URL, baseUrl)
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
            delay(TimeOut_PayWall_No_Price)
            if (!isDestroyed && !isFinishing) {
                onReceivedError()
            }
        }
    }


    override fun initView() {
        urlWeb = intent.getStringExtra(URL).toString()
        if (urlWeb.isBlank() || urlWeb == "null") {
            onReceivedError()
            return
        }
        injectionScript = viewModel.createInjectionScript(this@IAPWebViewActivity)
        setupWebView()
        shineAnimation(binding.shine)
        try {
            currentWebView?.loadUrl(urlWeb)
        } catch (e: Exception) {
            onReceivedError()
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
                            logd("TANHXXXX => Error evaluating JavaScript: ${e.message}")
                        }
                    }, onReceivedError = {
                        onReceivedError()
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

    private fun onReceivedError() {
        onFailureCallbackWithError()
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
                POLICY -> { openLink(Privacy_Policy) }
                TERMS -> { openLink(Terms) }
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
                    buyProduct(productIDSelect)
                }
            }
        }
    }
    // @formatter:on

    override fun onDestroy() {
        super.onDestroy()
        logD("TANHXXXX =>>>>> ondestroy")
        jobTimeOut?.cancel()
    }

    private fun buyProduct(producID: String) {
        if (isDestroyed || isFinishing) return
        PurchaseUtils.buy(
            this,
            producID,
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
                openLink(Privacy_Policy)
            }

            tvTermsOfUse.setOnClickShrinkEffectListener {
                openLink(Terms)
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
//        DeviceRegistrationManager.enqueueDeviceRegistration(applicationContext)
        finish()
    }

}