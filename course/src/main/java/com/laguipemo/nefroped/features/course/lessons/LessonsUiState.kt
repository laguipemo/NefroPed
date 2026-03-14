package com.laguipemo.nefroped.features.course.lessons

import com.laguipemo.nefroped.core.domain.model.course.Lesson

sealed interface LessonsUiState {
    data object Loading : LessonsUiState
    data class Content(
        val lessons: List<Lesson> = emptyList(),
        val isRefreshing: Boolean = false
    ) : LessonsUiState
    data class Error(val message: String) : LessonsUiState
}
