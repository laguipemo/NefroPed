package com.laguipemo.nefroped.features.auth.login.di

import com.laguipemo.nefroped.features.auth.login.LoginViewModel
import com.laguipemo.nefroped.features.auth.recoverpassword.RecoverPasswordViewModel
import com.laguipemo.nefroped.features.auth.recoverpassword.ResetPasswordViewModel
import com.laguipemo.nefroped.features.auth.register.RegisterViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authKoinModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::RecoverPasswordViewModel)
    viewModelOf(::ResetPasswordViewModel)
}
