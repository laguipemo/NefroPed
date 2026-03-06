package com.laguipemo.nefroped.core.data.repository

import android.util.Log
import com.laguipemo.nefroped.core.data.mapper.toDomain
import com.laguipemo.nefroped.core.domain.model.auth.AuthError
import com.laguipemo.nefroped.core.domain.model.auth.AuthState
import com.laguipemo.nefroped.core.domain.model.result.NefroResult
import com.laguipemo.nefroped.core.domain.repository.auth.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SupabaseAuthRepositoryImpl(
    private val supabase: SupabaseClient
) : AuthRepository {

    override fun observeAuthState(): Flow<AuthState> =
        supabase.auth.sessionStatus
            .map { sessionStatus -> sessionStatus.toDomain() }


    override suspend fun login(
        email: String,
        password: String
    ): NefroResult<Unit, AuthError> {
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val session = supabase.auth.currentSessionOrNull()
                ?: return NefroResult.Error(AuthError.SessionExpired)

            session.user
                ?: return NefroResult.Error(AuthError.UserNotFound)

            NefroResult.Success(Unit)

        } catch (e: Exception) {
            Log.i("CHACHY::: repositorio login", e.stackTraceToString())
            NefroResult.Error(e.toAuthError())
        }
    }

    override suspend fun register(
        email: String,
        password: String
    ): NefroResult<Unit, AuthError> {
        return try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            NefroResult.Success(Unit)
        } catch (e: Exception) {
            NefroResult.Error(e.toAuthError())
        }
    }

    override suspend fun anonymous(): NefroResult<Unit, AuthError> {
        return try {
            supabase.auth.signInAnonymously()
            NefroResult.Success(Unit)
        } catch (e: Exception) {
            Log.i("CHACHY::: nononymoues()", e.stackTraceToString())
            NefroResult.Error(e.toAuthError())
        }
    }


    override suspend fun logout() {
        supabase.auth.signOut()
        // o borrar token, datastore, etc.
    }

    private fun SessionStatus.toDomain(): AuthState =
        when (this) {
            is SessionStatus.Initializing -> AuthState.Initializing

            is SessionStatus.Authenticated -> {
                val user = session.user ?: return AuthState.Unauthenticated
                val isAnonymous = user.identities.isNullOrEmpty()

                AuthState.Authenticated(
                    user.toDomain(),
                    isAnonymous
                )
            }

            is SessionStatus.NotAuthenticated,
            is SessionStatus.RefreshFailure -> AuthState.Unauthenticated

        }


    private fun Throwable.toAuthError(): AuthError =
        when (this) {
            is AuthRestException -> {
                when (this.errorCode) {
                    AuthErrorCode.InvalidCredentials -> AuthError.InvalidCredentials
                    AuthErrorCode.UserNotFound -> AuthError.UserNotFound
                    else -> AuthError.Unknown(this)
                }
            }

            is java.io.IOException ->
                AuthError.Network

            else ->
                AuthError.Unknown(this)

        }

}