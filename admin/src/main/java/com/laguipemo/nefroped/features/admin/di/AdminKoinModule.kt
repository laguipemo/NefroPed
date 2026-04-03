package com.laguipemo.nefroped.features.admin.di

import com.laguipemo.nefroped.features.admin.topics.AdminTopicsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val adminKoinModule = module {
    viewModelOf(::AdminTopicsViewModel)
}
