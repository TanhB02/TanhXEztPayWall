package com.tanhxpurchase.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.net.http.SslError
import android.text.Html
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleCoroutineScope
import com.android.billingclient.api.ProductDetails
import com.google.gson.Gson
import com.lib.tanhx_purchase.BuildConfig
import com.lib.tanhx_purchase.R
import com.tanhxpurchase.ConstantsPurchase.EZT_Purchase
import com.tanhxpurchase.hawk.EzTechHawk.countryCode
import com.tanhxpurchase.hawk.EzTechHawk.isDarkMode
import com.tanhxpurchase.hawk.EzTechHawk.isFreeTrial
import com.tanhxpurchase.hawk.EzTechHawk.timeOutPayWall
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


internal var enableLog = true

@SuppressLint("SetJavaScriptEnabled")
fun configureWebViewSettings(webView: WebView) {
    webView.settings.run {
        javaScriptEnabled = true
        domStorageEnabled = true
        databaseEnabled = true
        cacheMode = WebSettings.LOAD_DEFAULT
        setRenderPriority(WebSettings.RenderPriority.HIGH)
        useWideViewPort = true
        loadWithOverviewMode = true
        setGeolocationEnabled(false)
        allowFileAccess = false
        allowContentAccess = false
        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        javaScriptCanOpenWindowsAutomatically = false
        setSupportZoom(false)
        builtInZoomControls = false
        displayZoomControls = false
    }
    webView.webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?) = false
    }
    webView.webChromeClient = WebChromeClient()
    webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
}


fun Context.openLink(link: String) {
    val webIntent = Intent(
        Intent.ACTION_VIEW, Uri.parse(link)
    )
    this.startActivity(webIntent)
}

fun Any.logD(log: String) {
    if (BuildConfig.DEBUG) {
        Log.d(this::class.java.simpleName, log)
    }
}

fun Any.logeSelf() {
    if (enableLog) {
        Log.e(EZT_Purchase, toString())
    }
}

fun logd(message: String? = null, tag: String = EZT_Purchase) {
    if (enableLog) {
        Log.d(tag, message ?: "null")
    }
}

fun Context.dpToPx(dp: Int): Int {
    val density = resources.displayMetrics.density
    return (dp * density).toInt()
}

fun View.toVisible() {
    this.visibility = View.VISIBLE
}

fun View.toGone() {
    this.visibility = View.GONE
}

fun TextView.setTextRes(@StringRes resId: Int) {
    text = context.getString(resId)
}

fun TextView.setTextRes(@StringRes resId: Int, vararg formatArgs: Any) {
    text = context.getString(resId, *formatArgs)
}

fun View.toInvisible() {
    this.visibility = View.INVISIBLE
}

fun TextView.setTextHtml(@StringRes resId: Int, vararg formatArgs: Any) {
    val raw = context.getString(resId, *formatArgs)
    text = Html.fromHtml(raw, Html.FROM_HTML_MODE_LEGACY)
}

fun View.setMarginTopDp(dp: Int) {
    val px = (dp * Resources.getSystem().displayMetrics.density).toInt()
    val params = layoutParams as? ViewGroup.MarginLayoutParams
    params?.let {
        it.topMargin = px
        layoutParams = it
    }
}


fun Activity.shineAnimation(view: View) {
    val anim = AnimationUtils.loadAnimation(this, R.anim.left_right)
    view.startAnimation(anim)
    anim.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationEnd(p0: Animation?) {
            view.startAnimation(anim)
        }

        override fun onAnimationStart(p0: Animation?) {}
        override fun onAnimationRepeat(p0: Animation?) {}
    })
}


fun WebView.setupWebViewClientWithTimeout(
    lifecycleScope: LifecycleCoroutineScope,
    onPageFinished: (WebView, String) -> Unit,
    onReceivedError: () -> Unit
) {
    var loadJob: Job? = null

    webViewClient = object : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
            super.onPageStarted(view, url, favicon)
            loadJob?.cancel()
            loadJob = lifecycleScope.launch {
                delay(timeOutPayWall)
                if (this.isActive) {
                    this@setupWebViewClientWithTimeout.stopLoading()
                    onReceivedError()
                }
            }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            loadJob?.cancel()
            if (view != null && url != null) {
                loadJob?.cancel()
                onPageFinished(view, url)
            }
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            loadJob?.cancel()
            onReceivedError()
        }

        override fun onReceivedHttpError(
            view: WebView?,
            request: WebResourceRequest?,
            errorResponse: WebResourceResponse?
        ) {
            super.onReceivedHttpError(view, request, errorResponse)
            loadJob?.cancel()
            if (request?.url?.path?.contains("/cdn-cgi/rum") == true) return
            if (request?.isForMainFrame == true) {
                onReceivedError()
            }
        }

        override fun onReceivedSslError(
            view: WebView?,
            handler: SslErrorHandler?,
            error: SslError?
        ) {
            super.onReceivedSslError(view, handler, error)
            loadJob?.cancel()
            onReceivedError()
        }
    }
}

fun createStandardJsonPayload(productDetailsList: List<ProductDetails>?): String {
    if (productDetailsList.isNullOrEmpty()) return "{\"data\":{\"infoPurchase\":[],\"countryCode\":\"${countryCode}\",\"isFreeTrial\":${isFreeTrial},\"darkMode\":${isDarkMode}}}"
    return try {
        val payloadList = mutableListOf<Map<String, Any>>()

        productDetailsList.forEach { productDetail ->
            val payload = mutableMapOf<String, Any>()

            val rawJsonData = mutableMapOf<String, Any>()
            rawJsonData["productId"] = productDetail.productId
            rawJsonData["type"] = productDetail.productType
            rawJsonData["title"] = productDetail.title
            rawJsonData["name"] = productDetail.name
            rawJsonData["localizedIn"] = listOf("en-US")
            rawJsonData["skuDetailsToken"] = ""

            productDetail.subscriptionOfferDetails?.let { offers ->
                val subscriptionOfferDetails = offers.map { offer ->
                    val offerData = mutableMapOf<String, Any>()
                    offerData["offerIdToken"] = offer.offerToken
                    offerData["basePlanId"] = offer.basePlanId
                    offer.offerId?.let { offerData["offerId"] = it }
                    offerData["pricingPhases"] = offer.pricingPhases.pricingPhaseList.map { phase ->
                        mapOf(
                            "priceAmountMicros" to phase.priceAmountMicros,
                            "priceCurrencyCode" to phase.priceCurrencyCode,
                            "formattedPrice" to phase.formattedPrice,
                            "billingPeriod" to phase.billingPeriod,
                            "recurrenceMode" to phase.recurrenceMode,
                            "billingCycleCount" to phase.billingCycleCount
                        )
                    }
                    offerData["offerTags"] = offer.offerTags
                    offerData
                }
                rawJsonData["subscriptionOfferDetails"] = subscriptionOfferDetails
            }

            payload["rawProductJson"] = Gson().toJson(rawJsonData)

            val nameValuePairs = mutableMapOf<String, Any>()
            nameValuePairs["productId"] = productDetail.productId
            nameValuePairs["type"] = productDetail.productType
            nameValuePairs["title"] = productDetail.title
            nameValuePairs["name"] = productDetail.name
            nameValuePairs["localizedIn"] = mapOf("values" to listOf("en-US"))
            nameValuePairs["skuDetailsToken"] = ""

            productDetail.subscriptionOfferDetails?.let { offers ->
                val subscriptionOfferDetails = mapOf(
                    "values" to offers.map { offer ->
                        val offerNameValuePairs = mutableMapOf<String, Any>()
                        offerNameValuePairs["offerIdToken"] = offer.offerToken
                        offerNameValuePairs["basePlanId"] = offer.basePlanId
                        offer.offerId?.let { offerNameValuePairs["offerId"] = it }
                        offerNameValuePairs["pricingPhases"] = mapOf(
                            "values" to offer.pricingPhases.pricingPhaseList.map { phase ->
                                mapOf(
                                    "nameValuePairs" to mapOf(
                                        "priceAmountMicros" to phase.priceAmountMicros,
                                        "priceCurrencyCode" to phase.priceCurrencyCode,
                                        "formattedPrice" to phase.formattedPrice,
                                        "billingPeriod" to phase.billingPeriod,
                                        "recurrenceMode" to phase.recurrenceMode,
                                        "billingCycleCount" to phase.billingCycleCount
                                    )
                                )
                            }
                        )
                        offerNameValuePairs["offerTags"] = mapOf("values" to offer.offerTags)
                        mapOf("nameValuePairs" to offerNameValuePairs)
                    }
                )
                nameValuePairs["subscriptionOfferDetails"] = subscriptionOfferDetails
            }

            payload["skuDetailsToken"] = mapOf("nameValuePairs" to nameValuePairs)

            payload["productId"] = productDetail.productId
            payload["productType"] = productDetail.productType
            payload["title"] = productDetail.title
            payload["name"] = productDetail.name
            payload["description"] = productDetail.description
            payload["token"] = ""

            productDetail.subscriptionOfferDetails?.let { offers ->
                val subscriptionOffersOffers = offers.map { offer ->
                    val offerMap = mutableMapOf<String, Any>()
                    offerMap["offerTags"] = offer.offerTags
                    offerMap["offerToken"] = offer.offerToken
                    offerMap["pricingPhases"] = mapOf(
                        "pricingPhaseList" to offer.pricingPhases.pricingPhaseList.map { phase ->
                            mapOf(
                                "billingCycleCount" to phase.billingCycleCount,
                                "billingPeriod" to phase.billingPeriod,
                                "formattedPrice" to phase.formattedPrice,
                                "priceAmountMicros" to phase.priceAmountMicros,
                                "priceCurrencyCode" to phase.priceCurrencyCode,
                                "recurrenceMode" to phase.recurrenceMode
                            )
                        }
                    )
                    offerMap
                }
                payload["subscriptionOffers"] = subscriptionOffersOffers
            } ?: run {
                payload["subscriptionOffers"] = emptyList<Any>()
            }

            payloadList.add(payload)
        }

        val finalResult = mapOf(
            "data" to mapOf(
                "infoPurchase" to payloadList,
                "countryCode" to countryCode,
                "isFreeTrial" to isFreeTrial, 
                "darkMode" to isDarkMode
            )
        )

        Gson().toJson(finalResult)
    } catch (e: Exception) {
        logd("TANHXXXX =>>>>>❌ Error creating standard JSON payload: ${e.message}")
        "{\"data\":{\"infoPurchase\":[],\"countryCode\":\"${countryCode}\",\"isFreeTrial\":${isFreeTrial},\"darkMode\":${isDarkMode}}}"
    }
}


