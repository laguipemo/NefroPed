package com.laguipemo.nefroped.features.chat.di

import com.laguipemo.nefroped.features.chat.ChatViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val chatKoinModule = module {
    viewModelOf(::ChatViewModel)
}