package com.laguipemo.nefroped.features.auth.recoverpassword

import com.laguipemo.nefroped.core.domain.model.util.ValidationError

data class ResetPasswordUiState(
    val password: String = "",
    val confirmPassword: String = "",
    val passwordError: ValidationError? = null,
    val confirmPasswordError: ValidationError? = null,
    val isLoading: Boolean = false
)