package com.laguipemo.nefroped.features.course.lessons.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laguipemo.nefroped.core.domain.usecase.course.ObserveLessonUseCase
import com.laguipemo.nefroped.core.domain.usecase.course.MarkLessonAsCompletedUseCase
import com.laguipemo.nefroped.core.domain.usecase.course.SyncLessonsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LessonDetailViewModel(
    private val lessonId: String,
    private val observeLessonUseCase: ObserveLessonUseCase,
    private val markLessonAsCompletedUseCase: MarkLessonAsCompletedUseCase,
    private val syncLessonsUseCase: SyncLessonsUseCase
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    private val _uiEffect = MutableSharedFlow<LessonDetailUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    val uiState: StateFlow<LessonDetailUiState> = observeLessonUseCase(lessonId)
        .combine(_isSyncing) { lesson, syncing ->
            when {
                lesson == null -> LessonDetailUiState.Error("Lección no encontrada")
                
                // Si el contenido está vacío y no estamos sincronizando, disparamos la carga
                lesson.content.isBlank() && !syncing -> {
                    triggerEmergencySync(lesson.topicId)
                    LessonDetailUiState.Loading
                }
                
                syncing -> LessonDetailUiState.Loading
                
                else -> {
                    LessonDetailUiState.Content(
                        lesson = lesson,
                        markdownContent = lesson.content,
                        isMarkdownLoading = false,
                        isCompleted = lesson.isCompleted
                    )
                }
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

    private fun triggerEmergencySync(topicId: String) {
        viewModelScope.launch {
            _isSyncing.value = true
            syncLessonsUseCase(topicId)
            _isSyncing.value = false
        }
    }

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
