package com.laguipemo.nefroped.core.domain.model.auth

import com.laguipemo.nefroped.core.domain.model.user.User

sealed interface AuthState {
    data object LoggedOut : AuthState
    data object Loading : AuthState
    data class LoggedIn(val user: User) : AuthState
    data class Error(val message: String) : AuthState
}