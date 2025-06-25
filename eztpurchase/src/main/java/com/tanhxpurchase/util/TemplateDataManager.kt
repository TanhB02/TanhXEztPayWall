package com.tanhxpurchase.util

import com.tanhxpurchase.model.template.Template
import com.tanhxpurchase.model.template.TemplateData
import javax.inject.Singleton

object TemplateDataManager {

    private var templateDataMap = mutableMapOf<String, List<TemplateData>>()

    fun saveTemplateData(key: String, data: List<TemplateData>) {
        templateDataMap[key] = data
    }

    fun getAllTemplateUrls(key: String): List<String> {
        return templateDataMap[key]?.flatMap { templateData ->
            templateData.templates.map { it.url }
        } ?: emptyList()
    }

    fun getTemplatesByValue(key: String, value: String): List<Template> {
        return templateDataMap[key]?.find { it.value == value }?.templates ?: emptyList()
    }

} 