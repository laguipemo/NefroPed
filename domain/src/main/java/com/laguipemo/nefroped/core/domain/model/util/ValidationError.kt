package com.laguipemo.nefroped.core.domain.model.util

sealed interface ValidationError {
    data object EmptyEmail: ValidationError
    data object EmptyPassword: ValidationError
    data object InvalidEmailFormat: ValidationError
    data object PasswordsDoNotMatch: ValidationError
    data class PasswordTooShort(val minLength: Int): ValidationError
    data object InvalidPasswordFormat: ValidationError
}