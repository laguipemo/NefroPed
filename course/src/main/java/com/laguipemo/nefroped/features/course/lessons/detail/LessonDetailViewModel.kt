package com.laguipemo.nefroped.features.course.lessons.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laguipemo.nefroped.core.domain.usecase.course.GetLessonUseCase
import com.laguipemo.nefroped.core.domain.usecase.course.MarkLessonAsCompletedUseCase
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LessonDetailViewModel(
    private val lessonId: String,
    private val getLessonUseCase: GetLessonUseCase,
    private val markLessonAsCompletedUseCase: MarkLessonAsCompletedUseCase,
    private val httpClient: HttpClient
) : ViewModel() {

    private val _markdownContent = MutableStateFlow("")
    private val _isMarkdownLoading = MutableStateFlow(false)
    
    private val _uiEffect = MutableSharedFlow<LessonDetailUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    val uiState: StateFlow<LessonDetailUiState> = getLessonUseCase(lessonId)
        .combine(_markdownContent) { lesson, markdown ->
            if (lesson == null) {
                LessonDetailUiState.Error("Lección no encontrada")
            } else {
                LessonDetailUiState.Content(
                    lesson = lesson,
                    markdownContent = markdown,
                    isMarkdownLoading = _isMarkdownLoading.value,
                    isCompleted = lesson.isCompleted
                )
            }
        }
        .combine(_isMarkdownLoading) { state, loading ->
            if (state is LessonDetailUiState.Content) {
                state.copy(isMarkdownLoading = loading)
            } else state
        }
        .catch { e ->
            emit(LessonDetailUiState.Error(e.message ?: "Error al cargar la lección"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LessonDetailUiState.Loading
        )

    init {
        loadMarkdown()
    }

    fun markAsCompleted() {
        viewModelScope.launch {
            val success = markLessonAsCompletedUseCase(lessonId)
            if (success) {
                _uiEffect.emit(LessonDetailUiEffect.NavigateBack)
            }
        }
    }

    private fun loadMarkdown() {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is LessonDetailUiState.Content) {
                val url = currentState.lesson.contentUrl
                if (url.isNotBlank()) {
                    _isMarkdownLoading.value = true
                    try {
                        val response = httpClient.get(url)
                        _markdownContent.value = response.bodyAsText()
                    } catch (e: Exception) {
                    } finally {
                        _isMarkdownLoading.value = false
                    }
                }
            } else {
                getLessonUseCase(lessonId).filterNotNull().firstOrNull()?.let { lesson ->
                    _isMarkdownLoading.value = true
                    try {
                        val response = httpClient.get(lesson.contentUrl)
                        _markdownContent.value = response.bodyAsText()
                    } catch (e: Exception) {
                    } finally {
                        _isMarkdownLoading.value = false
                    }
                }
            }
        }
    }
}

sealed interface LessonDetailUiEffect {
    data object NavigateBack : LessonDetailUiEffect
}
