package com.laguipemo.nefroped.features.profile.di

import com.laguipemo.nefroped.core.domain.usecase.login.LinkEmailPasswordUseCase
import com.laguipemo.nefroped.core.domain.usecase.login.LinkEmailPasswordUseCaseImpl
import com.laguipemo.nefroped.features.profile.ProfileViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val profileKoinModule = module {
    viewModelOf(::ProfileViewModel)
    factoryOf(::LinkEmailPasswordUseCaseImpl) bind LinkEmailPasswordUseCase::class
}