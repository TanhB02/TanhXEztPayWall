package com.tanhxpurchase.util

import com.tanhxpurchase.model.template.TemplateDataAll
import com.tanhxpurchase.model.template.TrackingEventPayload
import com.tanhxpurchase.model.template.TrackingEventRequest

object TemplateDataManager {
    private var templateDataAllMap = mutableMapOf<String, TemplateDataAll>()

    fun saveTemplateDataAll(key: String, data: TemplateDataAll) {
        templateDataAllMap[key] = data
    }

    fun getTemplateUrlByName(key: String, templateName: String): String {
        return templateDataAllMap[key]?.params
            ?.flatMap { it.firebaseValues }
            ?.flatMap { it.templates }
            ?.find { it.name == templateName }
            ?.url ?: ""
    }

    fun createTrackingEventFromValue(
        value: String,
        isFromTo : String,
        type: Int,
        productId: String? = null
    ): TrackingEventRequest? {
        for ((_, templateDataAll) in templateDataAllMap) {
            val foundFirebaseValue = templateDataAll.params
                .flatMap { it.firebaseValues }
                .find { firebaseValue -> firebaseValue.value == value }

            if (foundFirebaseValue != null) {
                val foundTemplate = foundFirebaseValue.templates
                    .find { it.paywallConfig != null }

                if (foundTemplate?.paywallConfig != null) {
                    val paywallConfig = foundTemplate.paywallConfig

                    val result = TrackingEventRequest(
                        paywallConfigId = paywallConfig.id,
                        templateId = paywallConfig.templateId,
                        storeId = templateDataAll.id,
                        type = type,
                        payload = if (productId != null) {
                            TrackingEventPayload(productId = productId,isFromTo = isFromTo)
                        } else TrackingEventPayload(isFromTo = isFromTo)
                    )

                    return result
                }
            }
        }

        return null
    }

    fun createTrackingEventFromUrl(
        url: String,
        type: Int,
        productId: String? = null
    ): TrackingEventRequest? {
        for ((_, templateDataAll) in templateDataAllMap) {
            val foundTemplate = templateDataAll.params
                .flatMap { it.firebaseValues }
                .flatMap { it.templates }
                .find { template -> isUrlMatchFixed(url, template.url) }

            if (foundTemplate?.paywallConfig != null) {
                val paywallConfig = foundTemplate.paywallConfig

                val result = TrackingEventRequest(
                    paywallConfigId = paywallConfig.id,
                    templateId = paywallConfig.templateId,
                    storeId = templateDataAll.id,
                    type = type,
                    payload = if (productId != null) {
                        TrackingEventPayload(productId = productId)
                    } else null
                )

                return result
            }
        }

        return null
    }


    private fun isUrlMatchFixed(inputUrl: String, storedUrl: String): Boolean {
        val normalizedInput = normalizeUrl(inputUrl)
        val normalizedStored = normalizeUrl(storedUrl)
        if (normalizedInput == normalizedStored) {
            return true
        }

        val inputPath = extractUrlPath(inputUrl)
        val storedPath = extractUrlPath(storedUrl)
        if (inputPath == storedPath) {
            return true
        }
        return false
    }

    private fun normalizeUrl(url: String): String {
        return url
            .lowercase()
            .removeSuffix("/")
            .substringBefore("?")
            .substringBefore("#")
            .trim()
    }

    private fun extractUrlPath(url: String): String {
        return try {
            val cleanUrl = url.substringBefore("?").substringBefore("#")

            if (cleanUrl.contains("://")) {
                val afterProtocol = cleanUrl.substringAfter("://")
                if (afterProtocol.contains("/")) {
                    "/" + afterProtocol.substringAfter("/")
                } else {
                    "/"
                }
            } else {
                cleanUrl
            }
        } catch (e: Exception) {
            url
        }.lowercase().removeSuffix("/")
    }

    fun getAllUrls(): List<String> {
        return templateDataAllMap.values
            .flatMap { it.params }
            .flatMap { it.firebaseValues }
            .flatMap { it.templates }
            .map { it.url }
            .distinct()
    }
}