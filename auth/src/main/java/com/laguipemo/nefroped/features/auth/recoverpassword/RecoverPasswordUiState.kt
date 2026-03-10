package com.laguipemo.nefroped.features.auth.recoverpassword

import com.laguipemo.nefroped.core.domain.model.util.ValidationError

data class RecoverPasswordUiState(
    val email: String = "",
    val emailError: ValidationError? = null,
    val isLoading: Boolean = false
)
