package com.laguipemo.nefroped.features.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laguipemo.nefroped.core.domain.usecase.course.GetTopicsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CourseViewModel(
    private val getTopicsUseCase: GetTopicsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CourseUiState>(CourseUiState.Loading)
    val uiState: StateFlow<CourseUiState> = _uiState.asStateFlow()

    init {
        loadTopics()
    }

    fun loadTopics() {
        viewModelScope.launch {
            _uiState.update { 
                if (it is CourseUiState.Content) it.copy(isRefreshing = true) else CourseUiState.Loading 
            }
            try {
                val topics = getTopicsUseCase()
                _uiState.value = CourseUiState.Content(topics = topics)
            } catch (e: Exception) {
                _uiState.value = CourseUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}
