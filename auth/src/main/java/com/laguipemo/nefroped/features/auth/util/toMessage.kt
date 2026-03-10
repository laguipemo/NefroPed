package com.laguipemo.nefroped.features.auth.util

import com.laguipemo.nefroped.core.domain.model.auth.AuthError

fun AuthError.toMessage(): String = when (this) {
    AuthError.InvalidCredentials -> "Email o contraseña incorrectos"
    AuthError.Network -> "Problemas de conexión"
    AuthError.SessionExpired -> "Su sesión ha expirado"
    AuthError.UserNotFound -> "El usuario no se ha encontrado"
    AuthError.SamePassword -> "La nueva contraseña debe ser diferente a la anterior"
    is AuthError.Unknown -> "Error inesperado"
}