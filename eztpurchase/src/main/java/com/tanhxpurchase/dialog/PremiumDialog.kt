package com.tanhxpurchase.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.webkit.WebView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.lib.tanhx_purchase.R
import com.lib.tanhx_purchase.databinding.DialogPremiumBinding
import com.tanhxpurchase.ConstantsPurchase.Android
import com.tanhxpurchase.ConstantsPurchase.CLOSE
import com.tanhxpurchase.ConstantsPurchase.PAYLOAD_RECEIVED
import com.tanhxpurchase.PurchaseUtils
import com.tanhxpurchase.PurchaseUtils.getPayWall
import com.tanhxpurchase.TrackingUtils.trackingBuy
import com.tanhxpurchase.TrackingUtils.trackingCloseScreen
import com.tanhxpurchase.TrackingUtils.trackingShowScreen
import com.tanhxpurchase.TrackingUtils.trackingUpgrade
import com.tanhxpurchase.TrackingUtils.trackingWatchAds
import com.tanhxpurchase.ConstantsPurchase.UPGRADE
import com.tanhxpurchase.ConstantsPurchase.WATCH_ADS
import com.tanhxpurchase.activity.IAPWebViewViewModel
import com.tanhxpurchase.base.BaseDialog
import com.tanhxpurchase.hawk.EzTechHawk.timeOutPayWall
import com.tanhxpurchase.listeners.IAPWebInterface
import com.tanhxpurchase.listeners.IAPWebViewCallback
import com.tanhxpurchase.model.iap.InfoScreen
import com.tanhxpurchase.util.TemplateDataManager.createTrackingEventFromValue
import com.tanhxpurchase.util.configureWebViewSettings
import com.tanhxpurchase.util.logd
import com.tanhxpurchase.util.loge
import com.tanhxpurchase.util.setupWebViewClientWithTimeout
import com.tanhxpurchase.util.toGone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PremiumDialog(
    var context: Activity,
    var lifecycles: LifecycleCoroutineScope,
    var screenName: String,
    var isFromTo: String,
    val onUpgradeCallback: () -> Unit,
    val watchAdsCallBack: () -> Unit,
    val onFailureCallback: () -> Unit
) : BaseDialog<DialogPremiumBinding>(
    context,
    R.style.DialogStyle,
    DialogPremiumBinding::inflate
), IAPWebViewCallback {
    private lateinit var url : String
    private var jobTimeOut: Job? = null
    private var injectionScript: String? = null
    private var infoScreen : InfoScreen = InfoScreen()
    private val viewModel: IAPWebViewViewModel =
        ViewModelProvider(context as ViewModelStoreOwner)[IAPWebViewViewModel::class.java]
    private var currentWebView: WebView? = null
    override fun initViews(binding: DialogPremiumBinding) {
        setCancelable(true)
        url = getPayWall(context.packageName,keyConfig = screenName)
        if (url.isBlank()) {
            dissMiss()
            onFailureCallback()
            return
        }
        
        injectionScript = viewModel.createInjectionScript(context)
        setupWebView()
        
        try {
            currentWebView?.loadUrl(url)
        } catch (e: Exception) {
            if (isShowing) {
                dissMiss()
                onFailureCallback()
            }
            return
        }
        trackingShowScreen(context,screenName,isFromTo)
        TimeOutWithNoPrice()
        createTrackingEventFromValue(screenName, isFromTo, 0)?.let {
            infoScreen.templateId = it.templateId
            infoScreen.paywallConfigId = it.paywallConfigId
            infoScreen.storeId = it.storeId
        }
    }

    private fun dissMiss(){
        lifecycles.launch {
            delay(100)
            dismiss()
        }
    }


    override fun dismiss() {
        super.dismiss()
        trackingCloseScreen(context, screenName,isFromTo)
    }

    private fun TimeOutWithNoPrice() {
        jobTimeOut = lifecycles.launch {
            delay(timeOutPayWall)
            if (isShowing) {
                dissMiss()
                onFailureCallback()
            }
        }
    }

    override fun initActions(binding: DialogPremiumBinding) {

    }

    private fun setupWebView() {
        currentWebView = createWebView()
            .apply {
                setupWebViewClientWithTimeout(lifecycles, onPageFinished = { webView, url ->
                    try {
                        if (injectionScript != null && isShowing) {
                            evaluateJavascript(injectionScript!!, null)
                        }
                    } catch (e: Exception) {
                        loge("TANHXXXX => Error evaluating JavaScript in DialogPremium: ${e.message}")
                    }
                }, onReceivedError = {
                    if (isShowing) {
                        onFailureCallback.invoke()
                        jobTimeOut?.cancel()
                        dissMiss()
                    }
                })
            }
        binding.webViewContainer.addView(currentWebView)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView(): WebView {
        return try {
            WebView(context).apply {
                configureWebViewSettings(this)
                addJavascriptInterface(IAPWebInterface(this@PremiumDialog), Android)
            }
        } catch (e: Exception) {
            loge("TANHXXXX => Error creating WebView in DialogPremium: ${e.message}")
            if (isShowing) {
                onFailureCallback.invoke()
                jobTimeOut?.cancel()
                dissMiss()
            }
            throw e
        }
    }


    override fun onUserClickListener(data: String) {
        if (!isShowing) return
        lifecycles.launch(Dispatchers.Main) {
            when (data.replace("\"", "")) {
                UPGRADE -> {
                    trackingUpgrade(context, screenName,isFromTo)
                    dismiss()
                    onUpgradeCallback()
                }

                WATCH_ADS -> {
                    trackingWatchAds(context, screenName,isFromTo)
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

                else -> {
                    trackingBuy(context,screenName,data,isFromTo)
                    buyProduct(data)
                    dismiss()
                }
            }
        }
    }

    private fun buyProduct(producID: String) {
        PurchaseUtils.buy(
            context,
            producID,
            infoScreen,
            onPurchaseSuccess = { purchase ->
                dismiss()
            },
            onPurchaseFailure = { code, errorMsg ->

            }
        )
    }



}