package com.laguipemo.nefroped.core.domain.usecase.profile

import com.laguipemo.nefroped.core.domain.model.auth.AuthError
import com.laguipemo.nefroped.core.domain.model.result.NefroResult
import com.laguipemo.nefroped.core.domain.repository.auth.AuthRepository

interface UpdateAvatarUseCase {
    suspend operator fun invoke(byteArray: ByteArray, fileName: String): NefroResult<String, AuthError>
}

class UpdateAvatarUseCaseImpl(
    private val repository: AuthRepository
) : UpdateAvatarUseCase {
    override suspend fun invoke(byteArray: ByteArray, fileName: String): NefroResult<String, AuthError> {
        return repository.updateAvatar(byteArray, fileName)
    }
}