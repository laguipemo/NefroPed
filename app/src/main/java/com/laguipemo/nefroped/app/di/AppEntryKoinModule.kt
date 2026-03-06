package com.laguipemo.nefroped.app.di

import com.laguipemo.nefroped.app.AppEntryViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appEntryKoinModule = module {
    viewModelOf(::AppEntryViewModel)
}