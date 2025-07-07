package com.tanhxpurchase.util

import com.tanhxpurchase.model.template.TemplateDataAll

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
}