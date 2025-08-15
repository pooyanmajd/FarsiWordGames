package com.pooyan.dev.farsiwords.di

import com.pooyan.dev.farsiwords.data.WordChecker
import com.pooyan.dev.farsiwords.data.auth.AnonymousAuthRepository
import com.pooyan.dev.farsiwords.domain.auth.AuthRepository
import com.pooyan.dev.farsiwords.presentation.WordVerificationViewModel
import com.pooyan.dev.farsiwords.presentation.auth.AuthViewModel
import org.koin.dsl.module

/**
 * Shared Koin module for dependency injection
 * Contains all shared dependencies that work across platforms
 */
val sharedModule = module {
    // Data layer
    single<WordChecker> { WordChecker }

    // Auth
    single<AuthRepository> { AnonymousAuthRepository() }

    // ViewModels
    single { WordVerificationViewModel(get()) }
    single { AuthViewModel(get()) }
}

/**
 * Complete list of shared modules
 */
val sharedModules = listOf(
    sharedModule
) 