package com.laguipemo.nefroped.features.course

import com.laguipemo.nefroped.core.domain.model.course.Topic

sealed interface CourseUiState {
    data object Loading : CourseUiState
    data class Content(
        val topics: List<Topic> = emptyList(),
        val isRefreshing: Boolean = false
    ) : CourseUiState
    data class Error(val message: String) : CourseUiState
}
