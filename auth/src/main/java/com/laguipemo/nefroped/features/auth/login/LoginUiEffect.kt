package com.laguipemo.nefroped.features.auth.login

import com.laguipemo.nefroped.core.domain.model.auth.AuthError

sealed interface LoginUiEffect {
    data class ShowError(val error: AuthError): LoginUiEffect
    data object LoginSuccess: LoginUiEffect
}