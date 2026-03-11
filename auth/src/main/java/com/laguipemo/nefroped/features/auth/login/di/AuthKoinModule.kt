package com.laguipemo.nefroped.features.auth.login.di

import com.laguipemo.nefroped.core.domain.usecase.login.LoginWithGoogleUseCase
import com.laguipemo.nefroped.core.domain.usecase.login.LoginWithGoogleUseCaseImpl
import com.laguipemo.nefroped.features.auth.login.LoginViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val authKoinModule = module {
    viewModelOf(::LoginViewModel)
    factoryOf(::LoginWithGoogleUseCaseImpl) bind LoginWithGoogleUseCase::class
}