package com.pooyan.dev.farsiwords.data

import android.content.Context

// Global variable to hold Android context (will be set from MainActivity)
private var androidContext: Context? = null

/**
 * Call this from Android Application or Activity to set the context
 */
fun initAndroidContext(context: Context) {
    androidContext = context.applicationContext
}

/**
 * Android-specific implementation for loading bloom filter from assets
 */
actual suspend fun loadBloomFilterPlatformSpecific(): ByteArray? {
    return try {
        val context = androidContext
        if (context == null) {
            println("Error: Android context not initialized. Call initAndroidContext() first.")
            return null
        }
        
        // Load from Android assets
        context.assets.open("bloom.bin").use { inputStream ->
            inputStream.readBytes()
        }
    } catch (e: Exception) {
        println("Error loading bloom filter on Android: ${e.message}")
        e.printStackTrace()
        null
    }
} 