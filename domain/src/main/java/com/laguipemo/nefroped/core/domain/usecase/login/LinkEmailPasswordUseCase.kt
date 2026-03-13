package com.laguipemo.nefroped.core.domain.usecase.login

import com.laguipemo.nefroped.core.domain.model.auth.AuthError
import com.laguipemo.nefroped.core.domain.model.result.NefroResult
import com.laguipemo.nefroped.core.domain.repository.auth.AuthRepository

interface LinkEmailPasswordUseCase {
    suspend operator fun invoke(email: String, password: String): NefroResult<Unit, AuthError>
}

class LinkEmailPasswordUseCaseImpl(
    private val repository: AuthRepository
) : LinkEmailPasswordUseCase {
    override suspend fun invoke(email: String, password: String): NefroResult<Unit, AuthError> {
        return repository.linkEmailPassword(email, password)
    }
}