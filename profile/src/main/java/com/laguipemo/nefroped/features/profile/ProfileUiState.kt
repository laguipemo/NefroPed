package com.laguipemo.nefroped.features.profile

import com.laguipemo.nefroped.core.domain.model.util.ValidationError

sealed interface ProfileUiState {
    object Loading : ProfileUiState
    data class Content(
        val greeting: String,
        val isGuest: Boolean,
        val isLoading: Boolean = false,
        val email: String = "",
        val password: String = "",
        val emailError: ValidationError? = null,
        val passwordError: ValidationError? = null,
        val showBottomSheet: Boolean = false
    ) : ProfileUiState
}