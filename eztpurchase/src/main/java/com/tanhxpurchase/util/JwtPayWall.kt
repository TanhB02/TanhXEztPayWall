package com.tanhxpurchase.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Base64
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.orhanobut.hawk.Hawk
import com.tanhxpurchase.AUTHEN_PAYWALL
import java.util.Date
import java.util.UUID

object JwtPayWall {
    @Throws(Exception::class)

    fun jwtToken(context: Context) {
        val currentToken = Hawk.get<String>(AUTHEN_PAYWALL, null)

        if (currentToken != null) {
            try {
                val payload = currentToken.split(".")[1]
                val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP)
                val jsonString = String(decodedBytes)
                val jsonObject = JsonParser.parseString(jsonString).asJsonObject
                val exp = jsonObject.get("exp")?.asLong ?: 0L
                val currentTime = System.currentTimeMillis() / 1000
                if (currentTime < exp) {
                    return
                }
            } catch (e: Exception) {
                logD("TANHXXXX =>>>>> Token decode failed: ${e.message}")
            }
        }

        val deviceInfo = getDeviceInfo(context)
        val deviceId = deviceInfo["device_id"] ?: "unknown"
        val deviceName = deviceInfo["device_name"] ?: "unknown"

        val now = System.currentTimeMillis() / 1000

        val dataObject = JsonObject().apply {
            addProperty("user_id", 0)
            addProperty("device_id", 0)
            addProperty("type", "1")
            addProperty("name", deviceName)
            addProperty("client_id", deviceId)
        }

        val jwtPayload = JsonObject().apply {
            addProperty("iat", now)
            add("data", dataObject)
        }
        val newToken = generateJWT(jwtPayload.toString())
        logD("TANHXXXX =>>>>> AUTHEN_PAYWALL newtoken: $newToken")
        Hawk.put(AUTHEN_PAYWALL, newToken)
    }


    fun generateJWT(json: String): String? {
        val expirationTimeMillis = System.currentTimeMillis() + (30 * 60 * 1000) // 30 phút
        return try {
            val algorithm = Algorithm.HMAC256("39f2031e74d36b1c4e2cc7ce0aad30a3")
            JWT.create()
                .withPayload(json)
                .withIssuedAt(Date(System.currentTimeMillis()))
                .withExpiresAt(Date(expirationTimeMillis))
                .sign(algorithm)
        } catch (e: JWTCreationException) {
            null
        }
    }


    @SuppressLint("HardwareIds")
    fun getDeviceInfo(context: Context): Map<String, String> {
        val deviceId = try {
            val androidId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )

            if (androidId.isNullOrBlank() || androidId == "9774d56d682e549c") {
                val prefs = context.getSharedPreferences("device_info", Context.MODE_PRIVATE)
                var uuid = prefs.getString("device_uuid", null)

                if (uuid == null) {
                    uuid = UUID.randomUUID().toString()
                    prefs.edit().putString("device_uuid", uuid).apply()
                }
                uuid ?: UUID.randomUUID().toString()
            } else {
                androidId
            }
        } catch (e: Exception) {
            UUID.randomUUID().toString()
        }

        val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"

        return mapOf(
            "device_id" to deviceId,
            "device_name" to deviceName
        )
    }
}