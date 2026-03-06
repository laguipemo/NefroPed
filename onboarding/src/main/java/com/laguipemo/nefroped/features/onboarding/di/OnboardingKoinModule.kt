package com.laguipemo.nefroped.features.onboarding.di

import com.laguipemo.nefroped.features.onboarding.OnboardingViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val onboardingKoinModule = module {
    viewModelOf(::OnboardingViewModel)
}