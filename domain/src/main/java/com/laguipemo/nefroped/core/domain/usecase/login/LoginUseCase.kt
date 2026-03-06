package com.laguipemo.nefroped.core.domain.usecase.login

import com.laguipemo.nefroped.core.domain.model.auth.AuthError
import com.laguipemo.nefroped.core.domain.model.result.NefroResult

interface LoginUseCase {
    suspend operator fun invoke( email: String, password: String): NefroResult<Unit, AuthError>
}