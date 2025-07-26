package com.pooyan.dev.farsiwords.di

import com.pooyan.dev.farsiwords.presentation.WordVerificationViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Android-specific Koin module
 * Contains Android-only dependencies like ViewModels
 */
val androidModule = module {
    
    // ViewModels (Android-specific)
    viewModel { WordVerificationViewModel() }
} 