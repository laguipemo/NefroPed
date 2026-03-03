package com.laguipemo.nefroped.core.domain.repository.appentry

import kotlinx.coroutines.flow.Flow

interface AppEntryRepository {
    // onboarding
    suspend fun setOnboardingCompleted()
    fun observeOnboardingCompleted(): Flow<Boolean>

}