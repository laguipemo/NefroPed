package com.laguipemo.nefroped.core.domain.usecase.login

import com.laguipemo.nefroped.core.domain.model.auth.AuthError
import com.laguipemo.nefroped.core.domain.repository.auth.AuthRepository

class LoginUseCaseImpl(
    private val repository: AuthRepository
) : LoginUseCase {
    override suspend fun invoke(
        email: String,
        password: String
    ): Result<Unit, AuthError> {
        return repository.login(email, password)
    }

}