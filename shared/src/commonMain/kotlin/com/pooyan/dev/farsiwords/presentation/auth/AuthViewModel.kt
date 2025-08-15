package com.pooyan.dev.farsiwords.presentation.auth

import com.pooyan.dev.farsiwords.domain.auth.AuthRepository
import com.pooyan.dev.farsiwords.domain.auth.AuthState
import com.pooyan.dev.farsiwords.domain.auth.AuthUser
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val authState: StateFlow<AuthState> = repository.authState

    fun signInAnonymously() {
        Napier.i("Auth: signInAnonymously() called")
        viewModelScope.launch {
            repository.signInAnonymously()
        }
    }

    fun signInWithGoogle(idToken: String) {
        Napier.i("Auth: signInWithGoogle() called")
        viewModelScope.launch {
            repository.signInWithGoogle(idToken)
        }
    }

    fun signInWithApple(idToken: String, nonce: String) {
        Napier.i("Auth: signInWithApple() called")
        viewModelScope.launch {
            repository.signInWithApple(idToken, nonce)
        }
    }

    fun signOut() {
        Napier.i("Auth: signOut() called")
        viewModelScope.launch {
            repository.signOut()
        }
    }
}