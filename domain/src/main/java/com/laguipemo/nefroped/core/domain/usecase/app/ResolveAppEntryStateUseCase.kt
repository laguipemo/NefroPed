package com.laguipemo.nefroped.core.domain.usecase.app

import com.laguipemo.nefroped.core.domain.model.app.AppEntryState
import com.laguipemo.nefroped.core.domain.model.session.SessionFailure
import com.laguipemo.nefroped.core.domain.model.session.SessionState
import com.laguipemo.nefroped.core.domain.usecase.session.ObserveSessionStateUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ResolveAppEntryStateUseCase(
    private val observeSessionState: ObserveSessionStateUseCase,
    private val observeOnboardingCompleted: ObserveOnboardingCompleteUseCase
) {

    operator fun invoke(): Flow<AppEntryState> =
        observeSessionState()
            .combine(
                observeOnboardingCompleted()
            ) { sessionState, onboardingCompleted ->
                resolve(sessionState, onboardingCompleted)
            }

    private fun resolve(
        sessionState: SessionState,
        onboardingCompleted: Boolean
    ): AppEntryState =
        when (sessionState) {
            SessionState.Initializing ->
                AppEntryState.Loading

            is SessionState.Error ->
                when(sessionState.failure) {
                    SessionFailure.SessionExpired ->
                        AppEntryState.RequireLogin

                    SessionFailure.Network ->
                        AppEntryState.Ready //mantener mundo actual

                    else ->
                        AppEntryState.Error
                }

            SessionState.LoggedOut ->
                AppEntryState.RequireLogin

            is SessionState.User ->
                if (!onboardingCompleted) {
                    AppEntryState.RequireOnboarding
                } else {
                    AppEntryState.Ready
                }

        }
}
