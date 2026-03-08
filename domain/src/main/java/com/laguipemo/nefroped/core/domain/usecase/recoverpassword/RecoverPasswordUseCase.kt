package com.laguipemo.nefroped.core.domain.usecase.recoverpassword

import com.laguipemo.nefroped.core.domain.model.auth.AuthError
import com.laguipemo.nefroped.core.domain.model.result.NefroResult

interface RecoverPasswordUseCase {
    suspend operator fun invoke(email: String): NefroResult<Unit, AuthError>
}