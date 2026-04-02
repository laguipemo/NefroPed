package com.laguipemo.nefroped.features.notifications.di

import com.laguipemo.nefroped.features.notifications.NotificationViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val notificationsKoinModule = module {
    viewModelOf(::NotificationViewModel)
}
