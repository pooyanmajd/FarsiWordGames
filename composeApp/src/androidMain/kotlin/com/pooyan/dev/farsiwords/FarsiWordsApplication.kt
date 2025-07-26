package com.pooyan.dev.farsiwords

import android.app.Application
import com.pooyan.dev.farsiwords.data.initAndroidContext
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
        
        // Initialize Android context for shared module
        initAndroidContext(this)
        
        // Initialize Koin DI
        initializeKoin()
        
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