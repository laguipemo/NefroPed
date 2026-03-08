package com.laguipemo.nefroped.features.auth.recoverpassword.di

import com.laguipemo.nefroped.features.auth.recoverpassword.RecoverPasswordViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val recoverPasswordKoinModule = module {
    viewModelOf(::RecoverPasswordViewModel)
}