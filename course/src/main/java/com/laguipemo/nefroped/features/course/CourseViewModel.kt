package com.laguipemo.nefroped.features.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laguipemo.nefroped.core.domain.usecase.course.GetTopicsUseCase
import com.laguipemo.nefroped.core.domain.usecase.course.SyncTopicsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CourseViewModel(
    private val getTopicsUseCase: GetTopicsUseCase,
    private val syncTopicsUseCase: SyncTopicsUseCase
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)

    val uiState: StateFlow<CourseUiState> = getTopicsUseCase()
        .combine(_isRefreshing) { topics, refreshing ->
            if (topics.isEmpty() && !refreshing) {
                CourseUiState.Loading
            } else {
                CourseUiState.Content(
                    topics = topics,
                    isRefreshing = refreshing
                )
            }
        }
        .catch { e ->
            emit(CourseUiState.Error(e.message ?: "Error al cargar los temas"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CourseUiState.Loading
        )

    init {
        refreshTopics()
    }

    fun refreshTopics() {
        viewModelScope.launch {
            _isRefreshing.value = true
            syncTopicsUseCase()
            _isRefreshing.value = false
        }
    }
}
