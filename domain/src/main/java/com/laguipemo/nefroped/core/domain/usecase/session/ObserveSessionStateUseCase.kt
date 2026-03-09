package com.laguipemo.nefroped.core.domain.usecase.session

import com.laguipemo.nefroped.core.domain.model.auth.AuthState
import com.laguipemo.nefroped.core.domain.model.session.SessionState
import com.laguipemo.nefroped.core.domain.usecase.app.ObserveAuthStateUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveSessionStateUseCase(
    private val observeAuthState: ObserveAuthStateUseCase,
) {
    operator fun invoke(): Flow<SessionState> =
        observeAuthState()
            .map { authState ->
                when (authState) {
                    is AuthState.Initializing ->
                        SessionState.Initializing

                    is AuthState.Error ->
                        SessionState.Error(authState.filure)

                    is AuthState.Authenticated ->
                        SessionState.User(
                            user = authState.user,
                            isAnonymous = authState.isAnonymous,
                            isResetPasswordFlow = authState.isResetPasswordFlow
                        )

                    is AuthState.Unauthenticated -> SessionState.LoggedOut
                }

            }
}