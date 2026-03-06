package com.laguipemo.nefroped.features.auth.register.di

import com.laguipemo.nefroped.features.auth.register.RegisterViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val registerKoinModule = module {
    viewModelOf(::RegisterViewModel)
}