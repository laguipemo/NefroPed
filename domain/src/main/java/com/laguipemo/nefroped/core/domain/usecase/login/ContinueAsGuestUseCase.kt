package com.laguipemo.nefroped.core.domain.usecase.login

import com.laguipemo.nefroped.core.domain.model.auth.AuthError

interface ContinueAsGuestUseCase {
    suspend operator fun invoke(): Result<Unit, AuthError>
}