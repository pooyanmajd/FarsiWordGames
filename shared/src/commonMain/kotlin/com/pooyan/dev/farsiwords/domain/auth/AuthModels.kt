package com.pooyan.dev.farsiwords.domain.auth

/** Represents an authenticated user (anonymous or provider-based) */
data class AuthUser(
    val uid: String,
    val displayName: String? = null,
    val email: String? = null,
    val isAnonymous: Boolean = false,
    val providerId: String? = null
)

/** Overall auth state */
sealed class AuthState {
    data object Unauthenticated : AuthState()
    data class Authenticated(val user: AuthUser) : AuthState()
    data class Error(val message: String) : AuthState()
    data object Loading : AuthState()
}

/** Supported sign-in methods */
sealed class SignInMethod {
    data object Google : SignInMethod()
    data object Apple : SignInMethod()
    data object Anonymous : SignInMethod()
}