package com.laguipemo.nefroped.core.domain.repository

import com.laguipemo.nefroped.core.domain.model.auth.AuthState
import com.laguipemo.nefroped.core.domain.model.user.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    val currentAuthState: Flow<AuthState>

    suspend fun loginWithEmail(email: String, password: String)
    suspend fun loginAnonymously()
    suspend fun loginWithGoogle()

    suspend fun registerWithEmail(email: String, password: String)
    suspend fun sendPasswordReset(email: String)

    suspend fun logout()

    suspend fun getCurrentUser(): User?
}