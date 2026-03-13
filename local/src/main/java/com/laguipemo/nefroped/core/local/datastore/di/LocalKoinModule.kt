package com.laguipemo.nefroped.core.local.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.laguipemo.nefroped.core.domain.repository.appentry.AppEntryRepository
import com.laguipemo.nefroped.core.local.datastore.DataStoreModule.dataStore
import com.laguipemo.nefroped.core.local.datastore.DatastoreAppEntryRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val localKoinModule = module {
    single<DataStore<Preferences>> {
        androidContext().dataStore
    }
    
    singleOf(::DatastoreAppEntryRepositoryImpl) { bind<AppEntryRepository>() }
}
