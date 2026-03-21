package com.laguipemo.nefroped.core.domain.di

import com.laguipemo.nefroped.core.domain.usecase.app.*
import com.laguipemo.nefroped.core.domain.usecase.chat.*
import com.laguipemo.nefroped.core.domain.usecase.course.*
import com.laguipemo.nefroped.core.domain.usecase.login.*
import com.laguipemo.nefroped.core.domain.usecase.logout.*
import com.laguipemo.nefroped.core.domain.usecase.onboarding.*
import com.laguipemo.nefroped.core.domain.usecase.profile.*
import com.laguipemo.nefroped.core.domain.usecase.recoverpassword.*
import com.laguipemo.nefroped.core.domain.usecase.register.*
import com.laguipemo.nefroped.core.domain.usecase.session.*
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainKoinModule = module {
    // Auth & Login
    factoryOf(::LoginUseCaseImpl) { bind<LoginUseCase>() }
    factoryOf(::RegisterUseCaseImpl) { bind<RegisterUseCase>() }
    factoryOf(::LogoutUseCaseImpl) { bind<LogoutUseCase>() }
    factoryOf(::ContinueAsGuestUseCaseImpl) { bind<ContinueAsGuestUseCase>() }
    factoryOf(::LoginWithGoogleUseCaseImpl) { bind<LoginWithGoogleUseCase>() }
    factoryOf(::LinkEmailPasswordUseCaseImpl) { bind<LinkEmailPasswordUseCase>() }
    factoryOf(::RecoverPasswordUseCaseImpl) { bind<RecoverPasswordUseCase>() }
    factoryOf(::UpdatePasswordUseCaseImpl) { bind<UpdatePasswordUseCase>() }
    
    // Profile
    factoryOf(::UpdateAvatarUseCaseImpl) { bind<UpdateAvatarUseCase>() }
    
    // Chat
    factoryOf(::SendMessageUseCaseImpl) { bind<SendMessageUseCase>() }
    factoryOf(::ObserveMessagesUseCaseImpl) { bind<ObserveMessagesUseCase>() }
    factoryOf(::ResolveChatCapabilitiesUseCase)
    
    // Course
    factoryOf(::ObserveTopicsUseCase)
    factoryOf(::ObserveLessonsUseCase)
    factoryOf(::ObserveLessonUseCase)
    factoryOf(::SyncTopicsUseCase)
    factoryOf(::SyncLessonsUseCase)
    factoryOf(::MarkLessonAsCompletedUseCase)
    
    // Clinical Cases (Tema 4)
    factoryOf(::ObserveClinicalCasesUseCase)
    factoryOf(::ObserveComplementaryResourcesUseCase)
    factoryOf(::SyncClinicalDataUseCase)
    
    // Quiz
    factoryOf(::ObserveQuizByTopicUseCase)
    factoryOf(::ObserveQuizByIdUseCase)
    factoryOf(::SubmitQuizUseCase)
    factoryOf(::SyncQuizUseCase)
    factoryOf(::SyncQuizByIdUseCase)
    
    // Onboarding
    factoryOf(::CompleteOnboardingUseCaseImpl) { bind<CompleteOnboardingUseCase>() }
    
    // App State & Session
    factoryOf(::ObserveAuthStateUseCase)
    factoryOf(::ObserveOnboardingCompleteUseCase)
    factoryOf(::ResolveAppEntryStateUseCase)
    factoryOf(::ObserveSessionStateUseCase)
    factoryOf(::GetAppVersionUseCase)
}
