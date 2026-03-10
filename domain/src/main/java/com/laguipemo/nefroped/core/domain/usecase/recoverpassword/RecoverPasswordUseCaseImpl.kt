package com.laguipemo.nefroped.core.domain.usecase.recoverpassword

import com.laguipemo.nefroped.core.domain.model.auth.AuthError
import com.laguipemo.nefroped.core.domain.model.result.NefroResult
import com.laguipemo.nefroped.core.domain.repository.auth.AuthRepository

class RecoverPasswordUseCaseImpl(
    private val repository: AuthRepository
): RecoverPasswordUseCase {
    override suspend fun invoke(email: String): NefroResult<Unit, AuthError> {
        return repository.recoverPassword(email)
    }

}