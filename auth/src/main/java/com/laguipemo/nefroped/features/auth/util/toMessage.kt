package com.laguipemo.nefroped.features.auth.util

import com.laguipemo.nefroped.core.domain.model.auth.AuthError

fun AuthError.toMessage(): String = when (this) {
    AuthError.InvalidCredentials -> "Email o contraseña incorrectos"
    AuthError.Network -> "Problemas de conexión"
    AuthError.SessionExpired -> "Su sesión ha expirado"
    AuthError.UserNotFound -> "El usuario no se ha encontrado"
    is AuthError.Unknown -> "Error inesperado"
}