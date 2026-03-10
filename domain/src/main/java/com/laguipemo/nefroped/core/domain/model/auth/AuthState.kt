package com.laguipemo.nefroped.core.domain.model.auth

import com.laguipemo.nefroped.core.domain.model.session.SessionFailure
import com.laguipemo.nefroped.core.domain.model.user.User

sealed interface AuthState {

    // estado técnico (solo en el arranque)
    data object Initializing : AuthState

    // estados funcionales
    data object Unauthenticated : AuthState
    data class Authenticated(
        val user: User,
        val isAnonymous: Boolean,
        val isResetPasswordFlow: Boolean = false
    ) : AuthState

    // estado de fallo del sistema
    data class Error(val filure: SessionFailure) : AuthState
}