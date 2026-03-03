package com.laguipemo.nefroped.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow

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