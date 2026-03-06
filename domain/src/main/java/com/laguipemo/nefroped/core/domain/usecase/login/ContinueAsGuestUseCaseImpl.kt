package com.laguipemo.nefroped.core.domain.usecase.login

import com.laguipemo.nefroped.core.domain.model.auth.AuthError
import com.laguipemo.nefroped.core.domain.model.result.NefroResult
import com.laguipemo.nefroped.core.domain.repository.auth.AuthRepository

class ContinueAsGuestUseCaseImpl(
    private val repository: AuthRepository,
) : ContinueAsGuestUseCase {
    override suspend fun invoke(): NefroResult<Unit, AuthError> {
        return repository.anonymous()
    }

}