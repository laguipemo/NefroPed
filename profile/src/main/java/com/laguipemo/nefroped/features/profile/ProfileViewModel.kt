package com.laguipemo.nefroped.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laguipemo.nefroped.core.domain.model.session.SessionState
import com.laguipemo.nefroped.core.domain.model.result.NefroResult
import com.laguipemo.nefroped.core.domain.usecase.logout.LogoutUseCase
import com.laguipemo.nefroped.core.domain.usecase.session.ObserveSessionStateUseCase
import com.laguipemo.nefroped.core.domain.usecase.login.LoginWithGoogleUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val observeSessionState: ObserveSessionStateUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)

    val uiState: StateFlow<ProfileUiState> =
        combine(observeSessionState(), _isLoading) { sessionState, isLoading ->
            when(sessionState) {
                SessionState.Initializing -> ProfileUiState.Loading
                is SessionState.User ->
                    ProfileUiState.Content(
                        greeting = "Hola ${if(sessionState.isAnonymous) "Invitado" else sessionState.user.email}",
                        isGuest = sessionState.isAnonymous,
                        isLoading = isLoading
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

    fun onLinkWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.update { true }
            when (val result = loginWithGoogleUseCase(idToken)) {
                is NefroResult.Success -> {
                    // El estado cambiará automáticamente a través de observeSessionState
                }
                is NefroResult.Error -> {
                    // Manejar error (podríamos añadir un efecto de error si fuera necesario)
                }
            }
            _isLoading.update { false }
        }
    }

}