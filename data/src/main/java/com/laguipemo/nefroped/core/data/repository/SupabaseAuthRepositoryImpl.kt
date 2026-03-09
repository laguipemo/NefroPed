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
            NefroResult.Success(Unit)
        } catch (e: Exception) {
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

    override suspend fun recoverPassword(email: String): NefroResult<Unit, AuthError> {
        return try {
            supabase.auth.resetPasswordForEmail(
                email = email,
                redirectUrl = "nefroped://reset-password"
            )
            NefroResult.Success(Unit)
        } catch (e: Exception) {
            NefroResult.Error(e.toAuthError())
        }
    }

    override suspend fun updatePassword(newPassword: String): NefroResult<Unit, AuthError> {
        return try {
            supabase.auth.updateUser {
                password = newPassword
            }
            
            // 1. Cerramos sesión para que AppRoot cambie el estado y nos mande al Login
            supabase.auth.signOut()
            
            NefroResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("CHACHY::: Error updatePassword", e.stackTraceToString())
            NefroResult.Error(e.toAuthError())
        }
    }


    override suspend fun logout() {
        supabase.auth.signOut()
    }

    private fun SessionStatus.toDomain(): AuthState =
        when (this) {
            is SessionStatus.Initializing -> AuthState.Initializing

            is SessionStatus.Authenticated -> {
                val user = session.user ?: return AuthState.Unauthenticated
                val isAnonymous = user.identities.isNullOrEmpty()

                // Mantenemos la solución que funciona
                val isResetFlow = session.type == "recovery"

                AuthState.Authenticated(
                    user.toDomain(),
                    isAnonymous,
                    isResetPasswordFlow = isResetFlow
                )
            }

            is SessionStatus.NotAuthenticated,
            is SessionStatus.RefreshFailure -> AuthState.Unauthenticated

        }


    private fun Throwable.toAuthError(): AuthError =
        when (this) {
            is AuthRestException -> {
                // Detectamos el error de misma contraseña
                if (this.error == "same_password" || this.message?.contains("same_password", ignoreCase = true) == true) {
                    AuthError.SamePassword
                } else {
                    when (this.errorCode) {
                        AuthErrorCode.InvalidCredentials -> AuthError.InvalidCredentials
                        AuthErrorCode.UserNotFound -> AuthError.UserNotFound
                        else -> AuthError.Unknown(this)
                    }
                }
            }
            is java.io.IOException -> AuthError.Network
            else -> AuthError.Unknown(this)
        }

}