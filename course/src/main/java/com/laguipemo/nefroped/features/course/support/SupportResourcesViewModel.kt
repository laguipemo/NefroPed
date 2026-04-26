package com.laguipemo.nefroped.features.course.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laguipemo.nefroped.core.domain.model.course.ExternalLink
import com.laguipemo.nefroped.core.domain.usecase.course.ObserveExternalLinksUseCase
import com.laguipemo.nefroped.core.domain.usecase.course.SyncExternalLinksUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SupportResourcesUiState(
    val links: List<ExternalLink> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class SupportResourcesViewModel(
    private val topicId: String,
    private val observeExternalLinks: ObserveExternalLinksUseCase,
    private val syncExternalLinks: SyncExternalLinksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupportResourcesUiState())
    val uiState: StateFlow<SupportResourcesUiState> = _uiState.asStateFlow()

    init {
        loadResources()
    }

    private fun loadResources() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Sincronizar con Supabase
            syncExternalLinks(topicId)
            
            // Observar cambios locales
            observeExternalLinks(topicId).collect { links ->
                _uiState.update { it.copy(
                    links = links.sortedBy { l -> l.order },
                    isLoading = false
                ) }
            }
        }
    }
}
