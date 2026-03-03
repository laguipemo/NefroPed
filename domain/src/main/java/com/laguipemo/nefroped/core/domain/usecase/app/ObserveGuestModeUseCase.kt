package com.laguipemo.nefroped.core.domain.usecase.app

import com.laguipemo.nefroped.core.domain.repository.appentry.AppEntryRepository
import kotlinx.coroutines.flow.Flow

class ObserveGuestModeUseCase(
    private val repository: AppEntryRepository
) {
    operator fun invoke(): Flow<Boolean> =
        repository.observeGuestMode()
}