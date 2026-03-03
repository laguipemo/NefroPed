package com.laguipemo.nefroped.core.domain.usecase.register

import com.laguipemo.nefroped.core.domain.model.auth.AuthError

interface RegisterUseCase {
    suspend operator fun invoke(email: String, password: String): Result<Unit, AuthError>
}