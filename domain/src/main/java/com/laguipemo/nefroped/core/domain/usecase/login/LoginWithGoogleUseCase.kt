package com.laguipemo.nefroped.core.domain.usecase.login

import com.laguipemo.nefroped.core.domain.model.auth.AuthError
import com.laguipemo.nefroped.core.domain.model.result.NefroResult
import com.laguipemo.nefroped.core.domain.repository.auth.AuthRepository

interface LoginWithGoogleUseCase {
    suspend operator fun invoke(idToken: String): NefroResult<Unit, AuthError>
}

class LoginWithGoogleUseCaseImpl(
    private val repository: AuthRepository
) : LoginWithGoogleUseCase {
    override suspend fun invoke(idToken: String): NefroResult<Unit, AuthError> {
        return repository.loginWithGoogle(idToken)
    }
}