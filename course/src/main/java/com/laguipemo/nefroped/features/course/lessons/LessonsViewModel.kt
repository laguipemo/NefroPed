package com.laguipemo.nefroped.features.course.lessons

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laguipemo.nefroped.core.domain.usecase.course.GetLessonsUseCase
import com.laguipemo.nefroped.core.domain.usecase.course.SyncLessonsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LessonsViewModel(
    private val topicId: String,
    private val getLessonsUseCase: GetLessonsUseCase,
    private val syncLessonsUseCase: SyncLessonsUseCase
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)

    val uiState: StateFlow<LessonsUiState> = getLessonsUseCase(topicId)
        .combine(_isRefreshing) { lessons, refreshing ->
            Log.d("LessonsVM", "Topic: $topicId, Lessons found: ${lessons.size}, Refreshing: $refreshing")
            
            // Si no hay lecciones y no estamos refrescando, mostramos una lista vacía en lugar de Loading infinito
            if (lessons.isEmpty() && refreshing) {
                LessonsUiState.Loading
            } else {
                LessonsUiState.Content(
                    lessons = lessons,
                    isRefreshing = refreshing
                )
            }
        }
        .catch { e ->
            Log.e("LessonsVM", "Error in Flow for topic $topicId", e)
            emit(LessonsUiState.Error(e.message ?: "Error al cargar las lecciones"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LessonsUiState.Loading
        )

    init {
        refreshLessons()
    }

    fun refreshLessons() {
        viewModelScope.launch {
            _isRefreshing.value = true
            Log.d("LessonsVM", "Starting sync for topic: $topicId")
            try {
                val result = syncLessonsUseCase(topicId)
                if (result.isFailure) {
                    Log.e("LessonsVM", "Sync failed for topic $topicId: ${result.exceptionOrNull()?.message}")
                } else {
                    Log.d("LessonsVM", "Sync success for topic $topicId")
                }
            } catch (e: Exception) {
                Log.e("LessonsVM", "Unexpected error syncing topic $topicId", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
