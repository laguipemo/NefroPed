package com.laguipemo.nefroped.features.auth.register

import com.laguipemo.nefroped.core.domain.model.util.ValidationError

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val emailError: ValidationError? = null,
    val passwordError: ValidationError? = null,
    val confirmPasswordError: ValidationError? = null,
    val isLoading: Boolean = false
)
