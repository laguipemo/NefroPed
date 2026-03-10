package com.laguipemo.nefroped.features.auth.recoverpassword

sealed interface ResetPasswordUserEvent {
    data class PasswordChanged(val value: String) : ResetPasswordUserEvent
    data class ConfirmPasswordChanged(val value: String) : ResetPasswordUserEvent
    data object Submit : ResetPasswordUserEvent
}