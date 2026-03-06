package com.laguipemo.nefroped.features.auth.login.di

import com.laguipemo.nefroped.features.auth.login.LoginViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authKoinModule = module {
    viewModelOf(::LoginViewModel)
}