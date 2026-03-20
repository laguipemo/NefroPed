package com.laguipemo.nefroped.features.course.clinical

import com.laguipemo.nefroped.core.domain.model.course.ClinicalCase
import com.laguipemo.nefroped.core.domain.model.course.ComplementaryResource

sealed interface ClinicalCaseListUiState {
    data object Loading : ClinicalCaseListUiState
    data class Content(
        val cases: List<ClinicalCase> = emptyList(),
        val resources: List<ComplementaryResource> = emptyList(),
        val isRefreshing: Boolean = false
    ) : ClinicalCaseListUiState
    data class Error(val message: String) : ClinicalCaseListUiState
}
