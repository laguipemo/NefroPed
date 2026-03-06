package com.laguipemo.nefroped.features.profile.di

import com.laguipemo.nefroped.features.profile.ProfileViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val profileKoinModule = module {
    viewModelOf(::ProfileViewModel)
}