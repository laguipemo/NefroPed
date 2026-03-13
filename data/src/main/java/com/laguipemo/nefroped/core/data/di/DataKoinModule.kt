package com.laguipemo.nefroped.core.data.di

import com.laguipemo.nefroped.core.data.repository.AppInfoRepositoryImpl
import com.laguipemo.nefroped.core.data.repository.SupabaseAuthRepositoryImpl
import com.laguipemo.nefroped.core.data.repository.SupabaseChatRepositoryImpl
import com.laguipemo.nefroped.core.data.supabase.createSupabaseClient
import com.laguipemo.nefroped.core.domain.repository.app.AppInfoRepository
import com.laguipemo.nefroped.core.domain.repository.auth.AuthRepository
import com.laguipemo.nefroped.core.domain.repository.chat.ChatRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataKoinModule = module {
    single { createSupabaseClient() }
    
    singleOf(::SupabaseAuthRepositoryImpl) { bind<AuthRepository>() }
    singleOf(::SupabaseChatRepositoryImpl) { bind<ChatRepository>() }
    single<AppInfoRepository> { AppInfoRepositoryImpl(androidContext()) }
}
