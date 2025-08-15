package com.pooyan.dev.farsiwords.domain.auth

import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<AuthState>

    suspend fun signInAnonymously(): Result<AuthUser>
    suspend fun signInWithGoogle(idToken: String): Result<AuthUser>
    suspend fun signInWithApple(idToken: String, nonce: String): Result<AuthUser>
    suspend fun signOut(): Result<Unit>
}