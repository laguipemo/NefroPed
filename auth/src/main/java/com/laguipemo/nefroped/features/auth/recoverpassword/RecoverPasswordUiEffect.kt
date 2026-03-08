package com.laguipemo.nefroped.features.auth.recoverpassword

import com.laguipemo.nefroped.core.domain.model.auth.AuthError

sealed interface RecoverPasswordUiEffect {
    data class ShowError(val error: AuthError): RecoverPasswordUiEffect
    data object RecoverPasswordSuccess: RecoverPasswordUiEffect
}