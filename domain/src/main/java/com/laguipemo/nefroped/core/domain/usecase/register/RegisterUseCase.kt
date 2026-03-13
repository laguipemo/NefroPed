package com.laguipemo.nefroped.core.domain.usecase.register

import com.laguipemo.nefroped.core.domain.model.auth.AuthError
import com.laguipemo.nefroped.core.domain.model.result.NefroResult

interface RegisterUseCase {
    suspend operator fun invoke(
        email: String, 
        password: String, 
        fullName: String
    ): NefroResult<Unit, AuthError>
}