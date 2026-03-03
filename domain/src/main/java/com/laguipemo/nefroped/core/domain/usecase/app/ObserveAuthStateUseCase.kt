package com.laguipemo.nefroped.core.domain.usecase.app

import com.laguipemo.nefroped.core.domain.model.auth.AuthState
import com.laguipemo.nefroped.core.domain.repository.auth.AuthRepository
import kotlinx.coroutines.flow.Flow

class ObserveAuthStateUseCase(
    private val repository: AuthRepository
) {

    operator fun invoke(): Flow<AuthState> = repository.observeAuthState()
}