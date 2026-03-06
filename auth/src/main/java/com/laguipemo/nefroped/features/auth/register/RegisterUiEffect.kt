package com.laguipemo.nefroped.features.auth.register

import com.laguipemo.nefroped.core.domain.model.auth.AuthError

sealed interface RegisterUiEffect {
    data class ShowError(val error: AuthError): RegisterUiEffect
    data object RegisterSuccess: RegisterUiEffect
}