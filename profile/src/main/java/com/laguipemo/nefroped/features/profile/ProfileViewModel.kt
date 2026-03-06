package com.laguipemo.nefroped.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laguipemo.nefroped.core.domain.model.session.SessionState
import com.laguipemo.nefroped.core.domain.usecase.logout.LogoutUseCase
import com.laguipemo.nefroped.core.domain.usecase.session.ObserveSessionStateUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val observeSessionState: ObserveSessionStateUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> =
        observeSessionState()
            .map { sessionState ->
                when(sessionState) {
                    SessionState.Initializing -> ProfileUiState.Loading
                    is SessionState.User ->
                        ProfileUiState.Content(
                            greeting = "Hola ${if(sessionState.isAnonymous) "Invitado" else sessionState.user.email}",
                            isGuest = false
                        )
                    else -> ProfileUiState.Loading
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                ProfileUiState.Loading
            )

    fun onLogoutClicked() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }

}