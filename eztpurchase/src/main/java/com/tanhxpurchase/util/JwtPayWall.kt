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
import com.tanhxpurchase.AUTHEN_TRACKING
import com.tanhxpurchase.TokenPayWall
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Date
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object JwtPayWall {
    private const val DEVICE_SECRET_KEY = "dsgfRTeiajauhqhshfidhe"
    private const val AES_TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private const val AES_ALGORITHM = "AES"
    
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
                logd("Token decode failed: ${e.message}", TokenPayWall)
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
        logd("newtoken: $newToken", TokenPayWall)
        Hawk.put(AUTHEN_PAYWALL, newToken)
    }

    fun generateTrackingToken(context: Context, packageId: String){
        val currentToken = Hawk.get<String>(AUTHEN_TRACKING, null)
        val currentTime = System.currentTimeMillis() / 1000
        val tokenExpiration = 24 * 60 * 60 // 1 day in seconds

        if (currentToken != null) {
            logD("TokenPayWall : ${currentToken}")
            try {
                val decryptedPayload = decodeOpenSsl(currentToken)
                val jsonObject = JsonParser.parseString(decryptedPayload).asJsonObject
                val tokenTime = jsonObject.get("time")?.asLong ?: 0L
                val expirationTime = tokenTime + tokenExpiration
                
                logd("Current time: $currentTime", TokenPayWall)
                logd("Token remaining: ${expirationTime - currentTime} seconds", TokenPayWall)
                
                if (currentTime < expirationTime) {
                    logd("Token still valid, using existing token", TokenPayWall)
                    return
                }
            } catch (e: Exception) {
                logd("Token decode failed: ${e.message}, generating new token", TokenPayWall)
            }
        }

        val deviceInfo = getDeviceInfo(context)
        val clientId = deviceInfo["device_id"] ?: "unknown"

        val payload = JsonObject().apply {
            addProperty("client_id", clientId)
            addProperty("platform", 0)
            addProperty("time", currentTime)
            addProperty("package_id", packageId)
            addProperty("id_db", 0)
        }

        val payloadString = payload.toString()
        logd("New tracking payload: $payloadString", TokenPayWall)

        val encryptedToken = encodeOpenSsl(payloadString)
        logd("New tracking token generated and saved", TokenPayWall)
        Hawk.put(AUTHEN_TRACKING, encryptedToken)
    }
    

    fun isTrackingTokenExpired(): Boolean {
        return try {
            val trackingToken = Hawk.get<String>(AUTHEN_TRACKING, null)
            if (trackingToken.isNullOrEmpty()) {
                logd("No tracking token found", TokenPayWall)
                return true
            }
            
            val decryptedPayload = decodeOpenSsl(trackingToken)
            val jsonObject = JsonParser.parseString(decryptedPayload).asJsonObject
            val tokenTime = jsonObject.get("time")?.asLong ?: 0L
            val currentTime = System.currentTimeMillis() / 1000
            val tokenExpiration = 24 * 60 * 60
            val expirationTime = tokenTime + tokenExpiration
            
            val isExpired = currentTime >= expirationTime
            
            logd("Tracking token expired: $isExpired (remaining: ${expirationTime - currentTime} seconds)", TokenPayWall)
            
            isExpired
        } catch (e: Exception) {
            logd("Error checking tracking token expiry: ${e.message}", TokenPayWall)
            true
        }
    }

    @Throws(Exception::class)
    private fun encodeOpenSsl(data: String, key: String = DEVICE_SECRET_KEY): String {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        
        // Normalize key to 32 bytes using SHA-256 (similar to PHP)
        val keyBytes = normalizeKey(key)
        val secretKeySpec = SecretKeySpec(keyBytes, AES_ALGORITHM)
        
        // Generate random IV
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        val ivParameterSpec = IvParameterSpec(iv)
        
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
        val encrypted = cipher.doFinal(data.toByteArray())
        
        // Combine IV + encrypted data and encode to Base64
        val combined = iv + encrypted
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }
    

    @Throws(Exception::class)
    private fun decodeOpenSsl(encryptedData: String, key: String = DEVICE_SECRET_KEY): String {
        val decoded = Base64.decode(encryptedData, Base64.NO_WRAP)
        
        // Extract IV (first 16 bytes) and encrypted data
        val iv = decoded.sliceArray(0..15)
        val encrypted = decoded.sliceArray(16 until decoded.size)
        
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        
        // Normalize key to 32 bytes using SHA-256 (similar to PHP)
        val keyBytes = normalizeKey(key)
        val secretKeySpec = SecretKeySpec(keyBytes, AES_ALGORITHM)
        val ivParameterSpec = IvParameterSpec(iv)
        
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
        val decrypted = cipher.doFinal(encrypted)
        
        return String(decrypted)
    }
    

    private fun normalizeKey(key: String): ByteArray {
        val keyBytes = key.toByteArray()
        return when {
            keyBytes.size == 32 -> keyBytes
            keyBytes.size < 32 -> {
                // Pad with zeros to reach 32 bytes
                val paddedKey = ByteArray(32)
                System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.size)
                paddedKey
            }
            else -> {
                // Use SHA-256 hash to get exactly 32 bytes
                val digest = MessageDigest.getInstance("SHA-256")
                digest.digest(keyBytes)
            }
        }
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