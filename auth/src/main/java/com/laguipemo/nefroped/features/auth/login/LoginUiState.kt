package com.laguipemo.nefroped.features.auth.login

import com.laguipemo.nefroped.core.domain.model.util.ValidationError

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: ValidationError? = null,
    val passwordError: ValidationError? = null,
    val isLoading: Boolean = false,
)
