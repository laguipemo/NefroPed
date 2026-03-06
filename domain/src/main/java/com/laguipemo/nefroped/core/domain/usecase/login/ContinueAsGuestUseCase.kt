package com.laguipemo.nefroped.core.domain.usecase.login

import com.laguipemo.nefroped.core.domain.model.auth.AuthError
import com.laguipemo.nefroped.core.domain.model.result.NefroResult

interface ContinueAsGuestUseCase {
    suspend operator fun invoke(): NefroResult<Unit, AuthError>
}