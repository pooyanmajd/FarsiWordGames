package com.pooyan.dev.farsiwords

import com.pooyan.dev.farsiwords.di.sharedModules
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
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