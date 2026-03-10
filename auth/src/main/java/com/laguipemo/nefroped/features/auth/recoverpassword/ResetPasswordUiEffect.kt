package com.laguipemo.nefroped.features.auth.recoverpassword

import com.laguipemo.nefroped.core.domain.model.auth.AuthError

sealed interface ResetPasswordUiEffect {
    data object ResetPasswordSuccess : ResetPasswordUiEffect
    data class ShowError(val error: AuthError) : ResetPasswordUiEffect
}