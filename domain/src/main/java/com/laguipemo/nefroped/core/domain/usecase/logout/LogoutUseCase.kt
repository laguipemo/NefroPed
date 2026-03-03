package com.laguipemo.nefroped.core.domain.usecase.logout

interface LogoutUseCase {
    suspend operator fun invoke(): Unit
}