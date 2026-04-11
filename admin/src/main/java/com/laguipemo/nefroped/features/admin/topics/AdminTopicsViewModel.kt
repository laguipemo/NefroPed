package com.laguipemo.nefroped.features.admin.topics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laguipemo.nefroped.core.domain.model.course.Topic
import com.laguipemo.nefroped.core.domain.usecase.course.ObserveTopicsUseCase
import com.laguipemo.nefroped.core.domain.usecase.course.SyncTopicsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface AdminTopicsUiState {
    data object Loading : AdminTopicsUiState
    data class Content(
        val topics: List<Topic>,
        val isRefreshing: Boolean = false
    ) : AdminTopicsUiState
    data class Error(val message: String) : AdminTopicsUiState
}

class AdminTopicsViewModel(
    private val observeTopics: ObserveTopicsUseCase,
    private val syncTopics: SyncTopicsUseCase
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)

    val uiState: StateFlow<AdminTopicsUiState> = combine(
        observeTopics(),
        _isRefreshing
    ) { topics, refreshing ->
        AdminTopicsUiState.Content(
            topics = topics,
            isRefreshing = refreshing
        ) as AdminTopicsUiState
    }
    .catch { e ->
        emit(AdminTopicsUiState.Error(e.message ?: "Error desconocido"))
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AdminTopicsUiState.Loading
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            syncTopics()
            _isRefreshing.value = false
        }
    }
}
