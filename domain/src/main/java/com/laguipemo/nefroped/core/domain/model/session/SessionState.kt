package com.laguipemo.nefroped.core.domain.model.session

sealed interface SessionState {

    // Estado técnico inicial
    data object Initializing : SessionState

    // No hay sesión activa
    data object LoggedOut : SessionState

    // Sesión autenticada
    data class User(
        val user: com.laguipemo.nefroped.core.domain.model.user.User,
        val isAnonymous: Boolean,
        val isResetPasswordFlow: Boolean = false
    ) : SessionState

    // Fallo grave del sistema
    data class Error(val failure: SessionFailure) : SessionState
}