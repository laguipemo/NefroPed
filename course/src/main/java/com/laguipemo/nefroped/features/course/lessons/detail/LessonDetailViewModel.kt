package com.laguipemo.nefroped.features.course.lessons.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laguipemo.nefroped.core.domain.usecase.course.ObserveLessonUseCase
import com.laguipemo.nefroped.core.domain.usecase.course.MarkLessonAsCompletedUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LessonDetailViewModel(
    private val lessonId: String,
    private val observeLessonUseCase: ObserveLessonUseCase,
    private val markLessonAsCompletedUseCase: MarkLessonAsCompletedUseCase
) : ViewModel() {

    private val _uiEffect = MutableSharedFlow<LessonDetailUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    val uiState: StateFlow<LessonDetailUiState> = observeLessonUseCase(lessonId)
        .map { lesson ->
            if (lesson == null) {
                LessonDetailUiState.Error("Lección no encontrada")
            } else {
                // Ahora usamos 'content' que contiene el texto del Markdown descargado
                LessonDetailUiState.Content(
                    lesson = lesson,
                    markdownContent = lesson.content,
                    isMarkdownLoading = false,
                    isCompleted = lesson.isCompleted
                )
            }
        }
        .catch { e ->
            emit(LessonDetailUiState.Error(e.message ?: "Error al cargar la lección"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LessonDetailUiState.Loading
        )

    fun markAsCompleted() {
        viewModelScope.launch {
            val success = markLessonAsCompletedUseCase(lessonId)
            if (success) {
                _uiEffect.emit(LessonDetailUiEffect.NavigateBack)
            }
        }
    }
}

sealed interface LessonDetailUiEffect {
    data object NavigateBack : LessonDetailUiEffect
}
