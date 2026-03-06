package com.laguipemo.nefroped.di

import android.system.Os.bind
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.laguipemo.nefroped.core.data.repository.SupabaseAuthRepositoryImpl
import com.laguipemo.nefroped.core.data.repository.SupabaseChatRepositoryImpl
import com.laguipemo.nefroped.core.data.supabase.createSupabaseClient
import com.laguipemo.nefroped.core.domain.repository.appentry.AppEntryRepository
import com.laguipemo.nefroped.core.domain.repository.auth.AuthRepository
import com.laguipemo.nefroped.core.domain.repository.chat.ChatRepository
import com.laguipemo.nefroped.core.domain.usecase.app.ObserveAuthStateUseCase
import com.laguipemo.nefroped.core.domain.usecase.app.ObserveOnboardingCompleteUseCase
import com.laguipemo.nefroped.core.domain.usecase.app.ResolveAppEntryStateUseCase
import com.laguipemo.nefroped.core.domain.usecase.chat.ObserveMessagesUseCase
import com.laguipemo.nefroped.core.domain.usecase.chat.ObserveMessagesUseCaseImpl
import com.laguipemo.nefroped.core.domain.usecase.chat.ResolveChatCapabilitiesUseCase
import com.laguipemo.nefroped.core.domain.usecase.chat.SendMessageUseCase
import com.laguipemo.nefroped.core.domain.usecase.chat.SendMessageUseCaseImpl
import com.laguipemo.nefroped.core.domain.usecase.login.ContinueAsGuestUseCase
import com.laguipemo.nefroped.core.domain.usecase.login.ContinueAsGuestUseCaseImpl
import com.laguipemo.nefroped.core.domain.usecase.login.LoginUseCase
import com.laguipemo.nefroped.core.domain.usecase.login.LoginUseCaseImpl
import com.laguipemo.nefroped.core.domain.usecase.logout.LogoutUseCase
import com.laguipemo.nefroped.core.domain.usecase.logout.LogoutUseCaseImpl
import com.laguipemo.nefroped.core.domain.usecase.onboarding.CompleteOnboardingUseCase
import com.laguipemo.nefroped.core.domain.usecase.onboarding.CompleteOnboardingUseCaseImpl
import com.laguipemo.nefroped.core.domain.usecase.register.RegisterUseCase
import com.laguipemo.nefroped.core.domain.usecase.register.RegisterUseCaseImpl
import com.laguipemo.nefroped.core.domain.usecase.session.ObserveSessionStateUseCase
import com.laguipemo.nefroped.core.local.datastore.DataStoreModule.dataStore
import com.laguipemo.nefroped.core.local.datastore.DatastoreAppEntryRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module


val koinAppModule = module {

    single<DataStore<Preferences>> {
        androidContext().dataStore
    }
    single {
        createSupabaseClient()
    }
    singleOf(::SupabaseAuthRepositoryImpl) {
        bind<AuthRepository>()
    }

    singleOf(::DatastoreAppEntryRepositoryImpl) {
        bind<AppEntryRepository>()
    }

    singleOf(::SupabaseChatRepositoryImpl) {
        bind<ChatRepository>()
    }

    factoryOf(::LoginUseCaseImpl) {
        bind<LoginUseCase>()
    }
    factoryOf(::RegisterUseCaseImpl) {
        bind<RegisterUseCase>()
    }
    factoryOf(::LogoutUseCaseImpl) {
        bind<LogoutUseCase>()
    }
    factoryOf(::ContinueAsGuestUseCaseImpl) {
        bind<ContinueAsGuestUseCase>()
    }
    factoryOf(::CompleteOnboardingUseCaseImpl) {
        bind<CompleteOnboardingUseCase>()
    }
    factoryOf(::SendMessageUseCaseImpl) {
        bind<SendMessageUseCase>()
    }
    factoryOf(::ObserveMessagesUseCaseImpl) {
        bind<ObserveMessagesUseCase>()
    }
    factoryOf(::ObserveAuthStateUseCase)
    factoryOf(::ObserveOnboardingCompleteUseCase)
    factoryOf(::ResolveAppEntryStateUseCase)
    factoryOf(::ObserveSessionStateUseCase)
    factoryOf(::ResolveChatCapabilitiesUseCase)

}