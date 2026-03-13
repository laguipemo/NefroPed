package com.laguipemo.nefroped.features.profile

import com.laguipemo.nefroped.core.domain.model.util.ValidationError

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Content(
        val userDisplayName: String = "",
        val userEmail: String = "",
        val avatarUrl: String? = null,
        val isGuest: Boolean = false,
        val isLoading: Boolean = false,
        // Campos del formulario de vinculación
        val formEmail: String = "",
        val formPassword: String = "",
        val emailError: ValidationError? = null,
        val passwordError: ValidationError? = null,
        val showBottomSheet: Boolean = false
    ) : ProfileUiState
}