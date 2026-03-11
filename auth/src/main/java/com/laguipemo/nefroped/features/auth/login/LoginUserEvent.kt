package com.laguipemo.nefroped.features.auth.login

sealed interface LoginUserEvent {
    data class EmailChanged(val value: String): LoginUserEvent
    data class PasswordChanged(val value: String): LoginUserEvent
    data object Submit: LoginUserEvent
    data object ContinueAsGuest: LoginUserEvent
    data class LoginWithGoogle(val idToken: String): LoginUserEvent
}