package com.laguipemo.nefroped.core.domain.repository.auth

import com.laguipemo.nefroped.core.domain.model.auth.AuthError
import com.laguipemo.nefroped.core.domain.model.auth.AuthState
import com.laguipemo.nefroped.core.domain.model.result.NefroResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    fun observeAuthState(): Flow<AuthState>
    suspend fun login(email: String, password: String): NefroResult<Unit, AuthError>
    suspend fun register(
        email: String,
        password: String
    ): NefroResult<Unit, AuthError>
    suspend fun anonymous(): NefroResult<Unit, AuthError>
    suspend fun recoverPassword(email: String): NefroResult<Unit, AuthError>
    suspend fun updatePassword(newPassword: String): NefroResult<Unit, AuthError>
    suspend fun loginWithGoogle(idToken: String): NefroResult<Unit, AuthError>
    suspend fun logout()
}