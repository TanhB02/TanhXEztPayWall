package com.tanhxpurchase.activity

import android.content.Context
import androidx.lifecycle.ViewModel
import com.tanhxpurchase.PurchaseUtils
import java.io.BufferedReader
import java.io.InputStreamReader

class IAPWebViewViewModel : ViewModel() {

    fun createInjectionScript(context: Context): String? {
        val template = loadJSFile(context, "paywall_injection.js") ?: return null
        return template.replace("PAYLOAD_DATA", PurchaseUtils.getPayload())
    }

    private fun loadJSFile(context: Context, fileName: String): String? {
        return try {
            context.assets.open(fileName).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
            }
        } catch (e: Exception) {
            null
        }
    }
}


