package com.laguipemo.nefroped.features.auth.recoverpassword.di

import com.laguipemo.nefroped.core.domain.usecase.recoverpassword.UpdatePasswordUseCase
import com.laguipemo.nefroped.core.domain.usecase.recoverpassword.UpdatePasswordUseCaseImpl
import com.laguipemo.nefroped.features.auth.recoverpassword.RecoverPasswordViewModel
import com.laguipemo.nefroped.features.auth.recoverpassword.ResetPasswordViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val recoverPasswordKoinModule = module {
    viewModelOf(::RecoverPasswordViewModel)
    viewModelOf(::ResetPasswordViewModel)
    factoryOf(::UpdatePasswordUseCaseImpl) bind UpdatePasswordUseCase::class
}