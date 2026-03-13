package com.laguipemo.nefroped.di

import com.laguipemo.nefroped.core.data.di.dataKoinModule
import com.laguipemo.nefroped.core.domain.di.domainKoinModule
import com.laguipemo.nefroped.core.local.datastore.di.localKoinModule
import com.laguipemo.nefroped.features.auth.login.di.authKoinModule
import com.laguipemo.nefroped.features.chat.di.chatKoinModule
import com.laguipemo.nefroped.features.onboarding.di.onboardingKoinModule
import com.laguipemo.nefroped.features.profile.di.profileKoinModule
import org.koin.dsl.module

val koinAppModule = module {
    includes(
        dataKoinModule,
        domainKoinModule,
        localKoinModule,
        authKoinModule,
        chatKoinModule,
        onboardingKoinModule,
        profileKoinModule
    )
}
