package com.laguipemo.nefroped.core.data.repository

import android.util.Log
import com.laguipemo.nefroped.core.data.mapper.toDomain
import com.laguipemo.nefroped.core.domain.model.auth.AuthError
import com.laguipemo.nefroped.core.domain.model.auth.AuthState
import com.laguipemo.nefroped.core.domain.model.result.NefroResult
import com.laguipemo.nefroped.core.domain.model.user.UserRole
import com.laguipemo.nefroped.core.domain.repository.auth.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
internal data class ProfileDto(
    val id: String,
    val role: String
)

class SupabaseAuthRepositoryImpl(
    private val supabase: SupabaseClient
) : AuthRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeAuthState(): Flow<AuthState> =
        supabase.auth.sessionStatus
            .flatMapLatest { sessionStatus ->
                flow {
                    when (sessionStatus) {
                        is SessionStatus.Authenticated -> {
                            val user = sessionStatus.session.user ?: run {
                                emit(AuthState.Unauthenticated)
                                return@flow
                            }
                            
                            // Consultamos el rol desde la tabla profiles
                            val role = try {
                                val profile = supabase.from("profiles")
                                    .select { filter { eq("id", user.id) } }
                                    .decodeSingleOrNull<ProfileDto>()
                                
                                when (profile?.role?.uppercase()) {
                                    "TEACHER" -> UserRole.TEACHER
                                    "ADMIN" -> UserRole.ADMIN
                                    else -> UserRole.STUDENT
                                }
                            } catch (e: Exception) {
                                Log.e("AuthRepo", "Error fetching user role", e)
                                UserRole.STUDENT
                            }

                            val isAnonymous = user.identities.isNullOrEmpty()
                            val isResetFlow = sessionStatus.session.type == "recovery"

                            emit(AuthState.Authenticated(
                                user.toDomain().copy(role = role),
                                isAnonymous,
                                isResetPasswordFlow = isResetFlow
                            ))
                        }
                        is SessionStatus.Initializing -> emit(AuthState.Initializing)
                        else -> emit(AuthState.Unauthenticated)
                    }
                }
            }


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
        password: String,
        fullName: String
    ): NefroResult<Unit, AuthError> {
        return try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("full_name", fullName)
                }
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
            Log.e("AuthRepo", "Anonymous login failed", e)
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
            supabase.auth.signOut()
            NefroResult.Success(Unit)
        } catch (e: Exception) {
            NefroResult.Error(e.toAuthError())
        }
    }

    override suspend fun loginWithGoogle(idToken: String): NefroResult<Unit, AuthError> {
        return try {
            Log.d("AuthRepo", "Attempting Google Login with token length: ${idToken.length}")
            supabase.auth.signInWith(IDToken) {
                this.idToken = idToken
                provider = Google
            }
            NefroResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepo", "Google Login Failed: ${e.message}", e)
            NefroResult.Error(e.toAuthError())
        }
    }
    
    override suspend fun linkEmailPassword(email: String, password: String): NefroResult<Unit, AuthError> {
        return try {
            supabase.auth.updateUser {
                this.email = email
                this.password = password
            }
            NefroResult.Success(Unit)
        } catch (e: Exception) {
            NefroResult.Error(e.toAuthError())
        }
    }

    override suspend fun updateAvatar(byteArray: ByteArray, fileName: String): NefroResult<String, AuthError> {
        return try {
            val user = supabase.auth.currentUserOrNull() ?: throw Exception("User not found")
            val bucket = supabase.storage.from("avatars")
            val path = "${user.id}/$fileName"
            bucket.upload(path, byteArray) {
                upsert = true
            }
            val publicUrl = bucket.publicUrl(path)
            
            supabase.auth.updateUser {
                data = buildJsonObject {
                    put("avatar_url", publicUrl)
                }
            }
            NefroResult.Success(publicUrl)
        } catch (e: Exception) {
            NefroResult.Error(e.toAuthError())
        }
    }


    override suspend fun logout() {
        supabase.auth.signOut()
    }

    private fun Throwable.toAuthError(): AuthError =
        when (this) {
            is AuthRestException -> {
                Log.e("AuthRepo", "Supabase Auth Error: ${this.error} - ${this.message}")
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
