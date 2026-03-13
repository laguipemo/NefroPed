package com.laguipemo.nefroped.features.auth.register

sealed interface RegisterUserEvent {
    data class FullNameChanged(val value: String): RegisterUserEvent
    data class EmailChanged(val value: String): RegisterUserEvent
    data class PasswordChanged(val value: String): RegisterUserEvent
    data class ConfirmPasswordChanged(val value: String): RegisterUserEvent
    data object Submit: RegisterUserEvent
}