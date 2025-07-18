package com.tanhxpurchase.dialog

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.webkit.WebView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.lib.tanhx_purchase.R
import com.lib.tanhx_purchase.databinding.BottomSheetPremiumBinding
import com.tanhxpurchase.ConstantsPurchase.Android
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
import com.tanhxpurchase.base.BaseBottomSheetDialogFragment
import com.tanhxpurchase.hawk.EzTechHawk.timeOutPayWall
import com.tanhxpurchase.listeners.IAPWebInterface
import com.tanhxpurchase.listeners.IAPWebViewCallback
import com.tanhxpurchase.model.iap.InfoScreen
import com.tanhxpurchase.util.TemplateDataManager.createTrackingEventFromValue
import com.tanhxpurchase.util.configureWebViewSettings
import com.tanhxpurchase.util.logD
import com.tanhxpurchase.util.logd
import com.tanhxpurchase.util.loge
import com.tanhxpurchase.util.setupWebViewClientWithTimeout
import com.tanhxpurchase.util.toGone
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
    private lateinit var screenName: String
    private lateinit var url: String
    private lateinit var isFromTo: String
    private var onUpgradeCallback: (() -> Unit)? = null
    private var watchAdsCallBack: (() -> Unit)? = null
    private var onFailureCallback: (() -> Unit)? = null
    private var jobTimeOut: Job? = null
    private var infoScreen : InfoScreen = InfoScreen()

    companion object {
        private const val SCREEEN_NAME = "screen_name"
        private const val IS_FROM_TO = "is_from_to"

        fun newInstance(
            screenName: String,
            isFromTo: String,
            onUpgradeCallback: () -> Unit,
            onFailureCallback: () -> Unit,
            watchAdsCallBack: (() -> Unit)? = null
        ): PremiumBottomSheet {
            return PremiumBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(SCREEEN_NAME, screenName)
                    putString(IS_FROM_TO, isFromTo)
                }
                this.onUpgradeCallback = onUpgradeCallback
                this.onFailureCallback = onFailureCallback
                this.watchAdsCallBack = watchAdsCallBack
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screenName = arguments?.getString(SCREEEN_NAME) ?: ""
        isFromTo = arguments?.getString(IS_FROM_TO) ?: ""
        url = getPayWall(requireActivity().packageName,screenName)
    }

    override fun initViewModel() {
        binding.viewModel = viewModel
        injectionScript = viewModel.createInjectionScript(requireContext())
        createTrackingEventFromValue(screenName, isFromTo, 0)?.let {
            infoScreen.templateId = it.templateId
            infoScreen.paywallConfigId = it.paywallConfigId
            infoScreen.storeId = it.storeId
        }
    }

    private fun TimeOutWithNoPrice() {
        jobTimeOut = lifecycleScope.launch {
            delay(timeOutPayWall)
            if (isVisible && !isDetached) {
                onFailureCallback?.invoke()
            }
            jobTimeOut?.cancel()
        }
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        trackingCloseScreen(requireActivity(), screenName, isFromTo)
    }

    override fun initView() {
        if (url.isBlank()) {
            onFailureCallback?.invoke()
            dismiss()
            return
        }
        setupWebView()
        loge("TANHXXXX =>>>>> url:${url}")
        try {
            currentWebView?.loadUrl(url)
        } catch (e: Exception) {
            if (isVisible && !isDetached) {
                onFailureCallback?.invoke()
                dismiss()
            }
            return
        }
        trackingShowScreen(requireActivity(), screenName,isFromTo)
        TimeOutWithNoPrice()
    }

    override fun addEvent() {

    }

    private fun setupWebView() {
        currentWebView = createWebView()
            .apply {
                setupWebViewClientWithTimeout(lifecycleScope, onPageFinished = { webView, url ->
                    try {
                        if (injectionScript != null && isVisible && !isDetached) {
                            evaluateJavascript(injectionScript!!, null)
                        }
                    } catch (e: Exception) {
                        loge("TANHXXXX => Error evaluating JavaScript in PremiumBottomSheet: ${e.message}")
                    }
                }, onReceivedError = {
                    if (isVisible && !isDetached) {
                        onFailureCallback?.invoke()
                        jobTimeOut?.cancel()
                        dismiss()
                    }
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
            loge("TANHXXXX => Error creating WebView in PremiumBottomSheet: ${e.message}")
            if (isVisible && !isDetached) {
                onFailureCallback?.invoke()
                jobTimeOut?.cancel()
                dismiss()
            }
            throw e
        }
    }

    private fun buyProduct(producID: String) {
        PurchaseUtils.buy(
            requireActivity(),
            producID,
            infoScreen,
            onPurchaseSuccess = { purchase ->
                dismiss()
            },
            onPurchaseFailure = { code, errorMsg ->

            }
        )
    }

    override fun onUserClickListener(data: String) {
        if (!isVisible || isDetached) return
        lifecycleScope.launch(Dispatchers.Main) {
            when (data.replace("\"", "")) {
                UPGRADE -> {
                    trackingUpgrade(requireActivity(), screenName,isFromTo)
                    onUpgradeCallback?.invoke()
                    dismiss()
                }

                WATCH_ADS -> {
                    trackingWatchAds(requireActivity(), screenName,isFromTo)
                    watchAdsCallBack?.invoke()
                    dismiss()
                }

                PAYLOAD_RECEIVED -> {
                    binding.frLoadding.toGone()
                    jobTimeOut?.cancel()
                }

                else -> {
                    trackingBuy(requireActivity(),screenName,data,isFromTo)
                    buyProduct(data)
                    dismiss()
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