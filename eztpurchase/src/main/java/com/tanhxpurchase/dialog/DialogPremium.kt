package com.tanhxpurchase.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.lib.tanhx_purchase.R
import com.lib.tanhx_purchase.databinding.DialogPremiumBinding
import com.tanhxpurchase.Android
import com.tanhxpurchase.CLOSE
import com.tanhxpurchase.PAYLOAD_RECEIVED
import com.tanhxpurchase.TimeOut_PayWall_No_Price
import com.tanhxpurchase.UPGRADE
import com.tanhxpurchase.WATCH_ADS
import com.tanhxpurchase.activity.IAPWebViewViewModel
import com.tanhxpurchase.base.BaseDialog
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

class DialogPremium(
    context: Context,
    var lifecycles: LifecycleCoroutineScope,
    var url: String,
    val onUpgradeCallback: () -> Unit,
    val watchAdsCallBack: () -> Unit,
    val onFailureCallback: () -> Unit
) : BaseDialog<DialogPremiumBinding>(
    context,
    R.style.DialogStyle,
    DialogPremiumBinding::inflate
), IAPWebViewCallback {
    private var jobTimeOut: Job? = null
    private var injectionScript: String? = null
    private val viewModel: IAPWebViewViewModel =
        ViewModelProvider(context as ViewModelStoreOwner)[IAPWebViewViewModel::class.java]
    private var currentWebView: WebView? = null
    override fun initViews(binding: DialogPremiumBinding) {
        setCancelable(true)
        injectionScript = viewModel.createInjectionScript(context)
        setupWebView()
        currentWebView?.loadUrl(url)
        TimeOutWithNoPrice()
    }

    private fun TimeOutWithNoPrice() {
        jobTimeOut = lifecycles.launch {
            delay(TimeOut_PayWall_No_Price)
            onFailureCallback()
        }
    }

    override fun initActions(binding: DialogPremiumBinding) {

    }

    private fun setupWebView() {
        currentWebView = createWebView()
            .apply {
                setupWebViewClientWithTimeout(lifecycles, onPageFinished = { webView, url ->
                    injectionScript?.let {
                        evaluateJavascript(it, null)
                    }
                }, onReceivedError = {
                    dismiss()
                    onFailureCallback.invoke()
                    jobTimeOut?.cancel()
                })
            }
        binding.webViewContainer.addView(currentWebView)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView(): WebView {
        return try {
            WebView(context).apply {
                configureWebViewSettings(this)
                addJavascriptInterface(IAPWebInterface(this@DialogPremium), Android)
            }
        } catch (e: Exception) {
            onFailureCallback.invoke()
            jobTimeOut?.cancel()
            dismiss()
            throw e
        }
    }


    override fun onUserClickListener(data: String) {
        lifecycles.launch(Dispatchers.Main) {
            when (data.replace("\"", "")) {
                UPGRADE -> {
                    logFirebaseEvent("action_click_upgrade_premium")
                    dismiss()
                    onUpgradeCallback()
                }

                WATCH_ADS -> {
                    logFirebaseEvent("action_click_watch_ads")
                    dismiss()
                    watchAdsCallBack()
                }

                PAYLOAD_RECEIVED -> {
                    jobTimeOut?.cancel()
                    binding.frLoadding.toGone()
                }

                CLOSE -> {
                    dismiss()
                }
            }
        }

    }


}