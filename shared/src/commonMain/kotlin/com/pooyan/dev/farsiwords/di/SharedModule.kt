package com.pooyan.dev.farsiwords.di

import com.pooyan.dev.farsiwords.data.WordChecker
import com.pooyan.dev.farsiwords.presentation.WordVerificationViewModel
import org.koin.dsl.module

/**
 * Shared Koin module for dependency injection
 * Contains all shared dependencies that work across platforms
 */
val sharedModule = module {
    
    // Data layer
    single<WordChecker> { WordChecker }
    
    // Shared ViewModels (work on all platforms)
    single { WordVerificationViewModel() }
}

/**
 * Complete list of shared modules
 */
val sharedModules = listOf(
    sharedModule
) 