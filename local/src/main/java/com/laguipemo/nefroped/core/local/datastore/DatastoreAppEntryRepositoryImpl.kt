package com.laguipemo.nefroped.core.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.laguipemo.nefroped.core.domain.repository.appentry.AppEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.prefs.Preferences

class DatastoreAppEntryRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : AppEntryRepository {

    private val ONBOARDING_COMPLETED = booleanPreferencesKey(
        "onboarding_completed"
    )

    override suspend fun setOnboardingCompleted() {
        dataStore.edit {
            it.set(ONBOARDING_COMPLETED, true)
        }
    }

    override fun observeOnboardingCompleted(): Flow<Boolean> =
        dataStore.data.map {
            it[ONBOARDING_COMPLETED] ?: false
        }

}
