package com.laguipemo.nefroped

import android.app.Application
import com.laguipemo.nefroped.di.koinAppModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class NefroPedApp: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@NefroPedApp)
            modules(koinAppModule)
        }
    }
}