package com.laguipemo.nefroped.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laguipemo.nefroped.core.domain.model.app.AppEntryState
import com.laguipemo.nefroped.core.domain.usecase.app.ResolveAppEntryStateUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AppEntryViewModel(
    private val resolveAppEntryState: ResolveAppEntryStateUseCase
) : ViewModel() {

    val appEntryState: StateFlow<AppEntryState> =
        resolveAppEntryState()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                AppEntryState.Loading
            )
}