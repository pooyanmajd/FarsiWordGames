package com.pooyan.dev.farsiwords

import com.pooyan.dev.farsiwords.di.sharedModules
import com.pooyan.dev.farsiwords.presentation.WordVerificationViewModel
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/**
 * Initialize Koin DI for KMP
 * Call this from each platform's entry point
 */
fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(sharedModules)
    }

/**
 * Initialize Koin for JVM/Android
 */
fun initKoin() = initKoin {}

/**
 * Initialize logging with Napier
 * Call this from each platform's entry point
 */
fun initLogging() {
    Napier.base(DebugAntilog())
    Napier.i("🏛️ Persepolis Wordle - Logging initialized")
}

/**
 * Swift-accessible helpers for iOS to resolve dependencies from Koin
 */
fun getKoin() = GlobalContext.get().koin

fun getWordVerificationViewModel(): WordVerificationViewModel = getKoin().get() 