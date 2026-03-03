package com.laguipemo.nefroped.core.domain.usecase.onboarding

import com.laguipemo.nefroped.core.domain.repository.appentry.AppEntryRepository

class CompleteOnboardingUseCaseImpl(
    private val repository: AppEntryRepository
): CompleteOnboardingUseCase {
    override suspend operator fun invoke() {
        repository.setOnboardingCompleted()
    }
}