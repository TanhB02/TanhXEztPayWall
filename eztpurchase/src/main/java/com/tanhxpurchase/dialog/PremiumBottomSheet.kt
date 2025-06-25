package com.tanhxpurchase.dialog

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.lib.tanhx_purchase.R
import com.lib.tanhx_purchase.databinding.BottomSheetPremiumBinding
import com.tanhxpurchase.Android
import com.tanhxpurchase.PAYLOAD_RECEIVED
import com.tanhxpurchase.TimeOut_PayWall_No_Price
import com.tanhxpurchase.UPGRADE
import com.tanhxpurchase.WATCH_ADS
import com.tanhxpurchase.activity.IAPWebViewViewModel
import com.tanhxpurchase.base.BaseBottomSheetDialogFragment
import com.tanhxpurchase.listeners.IAPWebInterface
import com.tanhxpurchase.listeners.IAPWebViewCallback
import com.tanhxpurchase.util.configureWebViewSettings
import com.tanhxpurchase.util.logD
import com.tanhxpurchase.util.logFirebaseEvent
import com.tanhxpurchase.util.setupWebViewClientWithTimeout
import com.tanhxpurchase.util.toGone
import com.tanhxpurchase.util.toVisible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PremiumBottomSheet :
    BaseBottomSheetDialogFragment<BottomSheetPremiumBinding>(R.layout.bottom_sheet_premium),
    IAPWebViewCallback {

    private val viewModel: IAPWebViewViewModel by viewModels()
    private var currentWebView: WebView? = null
    private var injectionScript: String? = null
    private var url: String = ""
    private var onUpgradeCallback: (() -> Unit)? = null
    private var watchAdsCallBack: (() -> Unit)? = null
    private var onFailureCallback: (() -> Unit)? = null
    private var jobTimeOut : Job? = null
    companion object {
        private const val ARG_URL = "url"

        fun newInstance(
            url: String,
            onUpgradeCallback: () -> Unit,
            onFailureCallback: () -> Unit,
            watchAdsCallBack: (() -> Unit)? = null
        ): PremiumBottomSheet {
            return PremiumBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_URL, url)
                }
                this.onUpgradeCallback = onUpgradeCallback
                this.onFailureCallback = onFailureCallback
                this.watchAdsCallBack = watchAdsCallBack
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        url = arguments?.getString(ARG_URL) ?: ""
    }

    override fun initViewModel() {
        binding.viewModel = viewModel
        injectionScript = viewModel.createInjectionScript(requireContext())
    }

    private fun TimeOutWithNoPrice(){
        jobTimeOut = lifecycleScope.launch {
            delay(TimeOut_PayWall_No_Price)
            onFailureCallback?.invoke()
            jobTimeOut?.cancel()
        }
    }

    override fun initView() {
        setupWebView()
        logD("TANHXXXX =>>>>> url:${url}")
        currentWebView?.loadUrl(url)
        TimeOutWithNoPrice()
    }

    override fun addEvent() {

    }

    private fun setupWebView() {
        currentWebView = createWebView()
            .apply {
                setupWebViewClientWithTimeout(lifecycleScope, onPageFinished = { webView, url ->
                    injectionScript?.let {
                        evaluateJavascript(it, null)
                    }
                }, onReceivedError = {
                    dismiss()
                    onFailureCallback?.invoke()
                    jobTimeOut?.cancel()
                })
            }
        binding.webViewContainer.addView(currentWebView)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView(): WebView {
        return try {
            WebView(requireContext()).apply {
                configureWebViewSettings(this)
                addJavascriptInterface(IAPWebInterface(this@PremiumBottomSheet), Android)
            }
        } catch (e: Exception) {
            onFailureCallback?.invoke()
            jobTimeOut?.cancel()
            dismiss()
            throw e
        }
    }

    override fun onUserClickListener(data: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            when (data.replace("\"", "")) {
                UPGRADE -> {
                    logFirebaseEvent("action_click_upgrade_premium")
                    dismiss()
                    onUpgradeCallback?.invoke()
                }
                WATCH_ADS ->{
                    logFirebaseEvent("action_watch_reward_ads_dialog")
                    watchAdsCallBack?.invoke()
                    dismiss()
                }
                PAYLOAD_RECEIVED ->{
                    binding.frLoadding.toGone()
                    jobTimeOut?.cancel()
                }
            }
        }
    }

    override fun onDestroyView() {
        currentWebView?.destroy()
        currentWebView = null
        jobTimeOut?.cancel()
        super.onDestroyView()
    }
}