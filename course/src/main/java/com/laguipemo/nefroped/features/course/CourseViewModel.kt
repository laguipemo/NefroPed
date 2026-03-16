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
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive = _isSearchActive.asStateFlow()

    val uiState: StateFlow<CourseUiState> = combine(
        getTopicsUseCase(),
        _isRefreshing,
        _searchQuery
    ) { topics, refreshing, query ->
        val filteredTopics = if (query.isBlank()) {
            topics
        } else {
            topics.filter { 
                it.title.contains(query, ignoreCase = true) || 
                it.description.contains(query, ignoreCase = true) 
            }
        }

        if (topics.isEmpty() && refreshing) {
            CourseUiState.Loading
        } else {
            CourseUiState.Content(
                topics = filteredTopics,
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

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onSearchActiveChange(active: Boolean) {
        _isSearchActive.value = active
        if (!active) _searchQuery.value = ""
    }

    fun refreshTopics() {
        viewModelScope.launch {
            _isRefreshing.value = true
            syncTopicsUseCase()
            _isRefreshing.value = false
        }
    }
}
