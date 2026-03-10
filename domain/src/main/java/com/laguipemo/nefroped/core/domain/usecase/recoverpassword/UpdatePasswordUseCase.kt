package com.laguipemo.nefroped.core.domain.usecase.recoverpassword

import com.laguipemo.nefroped.core.domain.model.auth.AuthError
import com.laguipemo.nefroped.core.domain.model.result.NefroResult
import com.laguipemo.nefroped.core.domain.repository.auth.AuthRepository

interface UpdatePasswordUseCase {
    suspend operator fun invoke(newPassword: String): NefroResult<Unit, AuthError>
}

class UpdatePasswordUseCaseImpl(
    private val repository: AuthRepository
) : UpdatePasswordUseCase {
    override suspend fun invoke(newPassword: String): NefroResult<Unit, AuthError> {
        return repository.updatePassword(newPassword)
    }
}