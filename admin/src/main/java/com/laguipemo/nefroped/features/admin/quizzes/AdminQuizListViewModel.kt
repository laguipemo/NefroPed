package com.laguipemo.nefroped.features.admin.quizzes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laguipemo.nefroped.core.domain.model.course.Topic
import com.laguipemo.nefroped.core.domain.usecase.course.ObserveTopicsUseCase
import com.laguipemo.nefroped.core.domain.usecase.course.SyncTopicsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface AdminQuizListUiState {
    data object Loading : AdminQuizListUiState
    data class Content(val topics: List<Topic>) : AdminQuizListUiState
    data class Error(val message: String) : AdminQuizListUiState
}

class AdminQuizListViewModel(
    private val observeTopicsUseCase: ObserveTopicsUseCase,
    private val syncTopicsUseCase: SyncTopicsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminQuizListUiState>(AdminQuizListUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadTopics()
    }

    private fun loadTopics() {
        viewModelScope.launch {
            syncTopicsUseCase()
            observeTopicsUseCase().collect { topics ->
                _uiState.value = AdminQuizListUiState.Content(topics)
            }
        }
    }
}
