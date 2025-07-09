package com.tanhxpurchase.network

import android.util.Log
import com.tanhxpurchase.ConstantsPurchase.EZT_Purchase
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.io.IOException

class CurlLoggingInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        val curlCommand = createCurlCommand(request)
        Log.d(EZT_Purchase, "CURL Command:")
        Log.d(EZT_Purchase, curlCommand)
        
        return chain.proceed(request)
    }

    private fun createCurlCommand(request: Request): String {
        val curlBuilder = StringBuilder("curl -X ${request.method}")
        curlBuilder.append(" '${request.url}'")
        request.headers.forEach { (name, value) ->
            curlBuilder.append(" \\\n--header '$name: $value'")
        }
        request.body?.let { requestBody ->
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            val body = buffer.readUtf8()
            
            if (body.isNotEmpty()) {
                curlBuilder.append(" \\\n--data '$body'")
            }
        }
        
        return curlBuilder.toString()
    }
} 