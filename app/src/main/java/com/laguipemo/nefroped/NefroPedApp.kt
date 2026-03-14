package com.laguipemo.nefroped

import android.app.Application
import com.laguipemo.nefroped.app.di.appEntryKoinModule
import com.laguipemo.nefroped.di.koinAppModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class NefroPedApp: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@NefroPedApp)
            // Cargamos el agregador de todas las librerías + el módulo local de la App
            modules(koinAppModule, appEntryKoinModule)
        }
    }
}
