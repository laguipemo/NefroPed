package com.laguipemo.nefroped

import android.app.Application
import com.laguipemo.nefroped.app.di.appEntryKoinModule
import com.laguipemo.nefroped.di.koinAppModule
import com.laguipemo.nefroped.features.auth.login.di.authKoinModule
import com.laguipemo.nefroped.features.auth.register.di.registerKoinModule
import com.laguipemo.nefroped.features.chat.di.chatKoinModule
import com.laguipemo.nefroped.features.onboarding.di.onboardingKoinModule
import com.laguipemo.nefroped.features.profile.di.profileKoinModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin


class NefroPedApp: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@NefroPedApp)
            modules(
                koinAppModule,
                authKoinModule,
                registerKoinModule,
                appEntryKoinModule,
                chatKoinModule,
                onboardingKoinModule,
                profileKoinModule
            )
        }
    }
}