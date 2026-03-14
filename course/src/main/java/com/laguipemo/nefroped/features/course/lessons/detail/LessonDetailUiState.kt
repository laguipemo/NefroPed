package com.laguipemo.nefroped.features.course.lessons.detail

import com.laguipemo.nefroped.core.domain.model.course.Lesson

sealed interface LessonDetailUiState {
    data object Loading : LessonDetailUiState
    data class Content(
        val lesson: Lesson,
        val markdownContent: String = "",
        val isMarkdownLoading: Boolean = false,
        val isCompleted: Boolean = false
    ) : LessonDetailUiState
    data class Error(val message: String) : LessonDetailUiState
}
