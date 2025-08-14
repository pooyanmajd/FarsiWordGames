package com.pooyan.dev.farsiwords.data.auth

import com.pooyan.dev.farsiwords.domain.auth.AuthRepository
import com.pooyan.dev.farsiwords.domain.auth.AuthState
import com.pooyan.dev.farsiwords.domain.auth.AuthUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class AnonymousAuthRepository : AuthRepository {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override suspend fun signInAnonymously(): Result<AuthUser> {
        _authState.value = AuthState.Loading
        val uid = Random.nextBytes(8).joinToString(separator = "") { byte ->
            val v = (byte.toInt() and 0xFF)
            v.toString(16).padStart(2, '0')
        }
        val user = AuthUser(uid = uid, isAnonymous = true, providerId = "anonymous")
        _authState.value = AuthState.Authenticated(user)
        return Result.success(user)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> {
        _authState.value = AuthState.Error("Google sign-in not implemented on this platform")
        return Result.failure(UnsupportedOperationException("Google sign-in not implemented"))
    }

    override suspend fun signInWithApple(idToken: String, nonce: String): Result<AuthUser> {
        _authState.value = AuthState.Error("Apple sign-in not implemented on this platform")
        return Result.failure(UnsupportedOperationException("Apple sign-in not implemented"))
    }

    override suspend fun signOut(): Result<Unit> {
        _authState.value = AuthState.Unauthenticated
        return Result.success(Unit)
    }
}