package com.tanhxpurchase.usecase

import com.tanhxpurchase.model.template.GetTemplateResponse
import com.tanhxpurchase.repository.TemplateRepository
import com.tanhxpurchase.util.ApiResult
import kotlinx.coroutines.flow.Flow

class GetTemplatesUseCase(
    private val templateRepository: TemplateRepository = TemplateRepository()
) {
    
    operator fun invoke(keys: List<String>): Flow<ApiResult<GetTemplateResponse>> {
        return templateRepository.getTemplatesByKeys(keys)
    }
} 