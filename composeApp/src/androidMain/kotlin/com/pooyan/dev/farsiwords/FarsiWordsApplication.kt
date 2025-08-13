package com.pooyan.dev.farsiwords

import android.app.Application
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level

/**
 * Android Application class
 * Initializes Koin DI, Napier logging, and Android-specific setup
 */
class FarsiWordsApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize logging first
        initializeLogging()
        
        // Initialize Koin DI
        initializeKoin()
        
        // Initialize WordChecker with Android resources (non-suspending wrapper)
        com.pooyan.dev.farsiwords.data.WordCheckerInitializer.init(this)
        
        Napier.i("🏛️ Persepolis Wordle Android app initialized")
    }
    
    private fun initializeLogging() {
        Napier.base(DebugAntilog())
        Napier.i("📝 Napier logging initialized for Android")
    }
    
    private fun initializeKoin() {
        initKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@FarsiWordsApplication)
            // Using shared modules only - no Android-specific module needed
        }
        Napier.i("💉 Koin DI initialized for Android")
    }
} 