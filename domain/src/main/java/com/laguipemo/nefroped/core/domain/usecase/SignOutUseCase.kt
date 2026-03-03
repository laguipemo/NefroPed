package com.laguipemo.nefroped.core.domain.usecase

import com.laguipemo.nefroped.core.domain.repository.auth.AuthRepository

class SignOutUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke() {
        repository.logout()
    }
}