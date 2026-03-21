package com.laguipemo.nefroped.features.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laguipemo.nefroped.core.domain.usecase.course.ObserveTopicsUseCase
import com.laguipemo.nefroped.core.domain.usecase.course.SyncTopicsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CourseViewModel(
    private val observeTopics: ObserveTopicsUseCase,
    private val syncTopics: SyncTopicsUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    val uiState: StateFlow<CourseUiState> = combine(
        observeTopics(),
        _searchQuery
    ) { topics, query ->
        val filteredTopics = if (query.isBlank()) {
            topics
        } else {
            topics.filter { 
                it.title.contains(query, ignoreCase = true) || 
                it.description.contains(query, ignoreCase = true) 
            }
        }
        CourseUiState.Content(topics = filteredTopics)
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CourseUiState.Loading
    )

    init {
        viewModelScope.launch {
            syncTopics()
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSearchActiveChange(active: Boolean) {
        _isSearchActive.value = active
        if (!active) _searchQuery.value = ""
    }
}
