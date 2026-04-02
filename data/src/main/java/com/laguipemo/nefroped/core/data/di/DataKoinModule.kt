package com.laguipemo.nefroped.core.data.di

import com.laguipemo.nefroped.core.data.repository.*
import com.laguipemo.nefroped.core.data.supabase.createSupabaseClient
import com.laguipemo.nefroped.core.domain.repository.app.AppInfoRepository
import com.laguipemo.nefroped.core.domain.repository.auth.AuthRepository
import com.laguipemo.nefroped.core.domain.repository.chat.ChatRepository
import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository
import com.laguipemo.nefroped.core.domain.repository.notification.NotificationRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataKoinModule = module {
    single { createSupabaseClient() }
    
    // HttpClient global para descarga de recursos (Markdown, etc)
    single { HttpClient(OkHttp) }
    
    singleOf(::SupabaseAuthRepositoryImpl) { bind<AuthRepository>() }
    singleOf(::SupabaseChatRepositoryImpl) { bind<ChatRepository>() }
    singleOf(::SupabaseCourseRepositoryImpl) { bind<CourseRepository>() }
    singleOf(::SupabaseNotificationRepositoryImpl) { bind<NotificationRepository>() }
    single<AppInfoRepository> { AppInfoRepositoryImpl(androidContext()) }
}
