package com.tanhxpurchase.listeners

import android.webkit.JavascriptInterface
import java.lang.ref.WeakReference

class IAPWebInterface(callback: IAPWebViewCallback) {
    private val callbackRef = WeakReference(callback)

    @JavascriptInterface
    fun onUserClickListener(data: String) {
        callbackRef.get()?.onUserClickListener(data)
    }

}