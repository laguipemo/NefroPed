package com.laguipemo.nefroped.features.course.clinical

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.laguipemo.nefroped.core.domain.usecase.course.ObserveClinicalCasesUseCase
import com.laguipemo.nefroped.core.domain.usecase.course.ObserveComplementaryResourcesUseCase
import com.laguipemo.nefroped.core.domain.usecase.course.SyncClinicalDataUseCase
import com.laguipemo.nefroped.navigation.AuthenticatedRoute
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ClinicalCaseListViewModel(
    savedStateHandle: SavedStateHandle,
    private val observeClinicalCases: ObserveClinicalCasesUseCase,
    private val observeComplementaryResources: ObserveComplementaryResourcesUseCase,
    private val syncClinicalData: SyncClinicalDataUseCase
) : ViewModel() {

    private val topicId: String = savedStateHandle.toRoute<AuthenticatedRoute.ClinicalCaseList>().topicId

    private val _isRefreshing = MutableStateFlow(false)

    val uiState: StateFlow<ClinicalCaseListUiState> = combine(
        observeClinicalCases(topicId),
        observeComplementaryResources(topicId),
        _isRefreshing
    ) { cases, resources, refreshing ->
        if (cases.isEmpty() && resources.isEmpty() && refreshing) {
            ClinicalCaseListUiState.Loading
        } else {
            ClinicalCaseListUiState.Content(
                cases = cases,
                resources = resources,
                isRefreshing = refreshing
            )
        }
    }
    .catch { e ->
        emit(ClinicalCaseListUiState.Error(e.message ?: "Error al cargar los casos clínicos"))
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ClinicalCaseListUiState.Loading
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            syncClinicalData(topicId)
            _isRefreshing.value = false
        }
    }
}
