package com.laguipemo.nefroped.features.course.lessons

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laguipemo.nefroped.core.domain.usecase.course.ObserveLessonsUseCase
import com.laguipemo.nefroped.core.domain.usecase.course.SyncLessonsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LessonsViewModel(
    private val topicId: String,
    private val observeLessons: ObserveLessonsUseCase,
    private val syncLessons: SyncLessonsUseCase
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)

    val uiState: StateFlow<LessonsUiState> = observeLessons(topicId)
        .combine(_isRefreshing) { lessons, refreshing ->
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
            syncLessons(topicId)
            _isRefreshing.value = false
        }
    }
}
