package com.laguipemo.nefroped.core.domain.usecase.logout

import com.laguipemo.nefroped.core.domain.repository.appentry.AppEntryRepository
import com.laguipemo.nefroped.core.domain.repository.auth.AuthRepository

class LogoutUseCaseImpl(
    private val repository: AuthRepository,
    private val appEntryRepository: AppEntryRepository
) : LogoutUseCase {
    override suspend fun invoke() {
        repository.logout()
        appEntryRepository.clearGuest()
        //appEntryRepository.clearOnboarding() <- futuro
    }
}