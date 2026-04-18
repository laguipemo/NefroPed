package com.laguipemo.nefroped.core.local.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.laguipemo.nefroped.core.domain.repository.appentry.AppEntryRepository
import com.laguipemo.nefroped.core.local.datastore.DataStoreModule.dataStore
import com.laguipemo.nefroped.core.local.datastore.DatastoreAppEntryRepositoryImpl
import com.laguipemo.nefroped.core.local.room.NefroDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val localKoinModule = module {
    // DataStore
    single<DataStore<Preferences>> {
        androidContext().dataStore
    }
    
    // Room
    single {
        Room.databaseBuilder(
            androidContext(),
            NefroDatabase::class.java,
            "nefroped_database"
        )
        .fallbackToDestructiveMigration() // Añadido para manejar cambios en el esquema sin crashes
        .build()
    }
    
    single { get<NefroDatabase>().courseDao() }
    single { get<NefroDatabase>().supportDao() }
    
    singleOf(::DatastoreAppEntryRepositoryImpl) { bind<AppEntryRepository>() }
}
