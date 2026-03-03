package com.laguipemo.nefroped.core.domain.usecase.login

import com.laguipemo.nefroped.core.domain.model.auth.AuthError

interface LoginUseCase {
    suspend operator fun invoke( email: String, password: String): Result<Unit, AuthError>
}