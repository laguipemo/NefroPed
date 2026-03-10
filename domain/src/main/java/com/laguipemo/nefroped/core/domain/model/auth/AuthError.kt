package com.laguipemo.nefroped.core.domain.model.auth

sealed interface AuthError {
    data object InvalidCredentials: AuthError
    data object UserNotFound: AuthError
    data object Network: AuthError
    data object SessionExpired: AuthError
    data object SamePassword: AuthError

    data class Unknown(val cause: Throwable? = null): AuthError
}