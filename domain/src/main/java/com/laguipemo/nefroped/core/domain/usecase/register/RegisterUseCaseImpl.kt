package com.laguipemo.nefroped.core.domain.usecase.register

import com.laguipemo.nefroped.core.domain.model.auth.AuthError
import com.laguipemo.nefroped.core.domain.model.result.NefroResult
import com.laguipemo.nefroped.core.domain.repository.auth.AuthRepository

class RegisterUseCaseImpl(
    private val repository: AuthRepository
): RegisterUseCase {
    override suspend fun invoke(
        email: String,
        password: String,
        fullName: String
    ): NefroResult<Unit, AuthError> {
        return repository.register(email, password, fullName)
    }
}