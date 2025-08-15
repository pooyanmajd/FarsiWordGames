package com.pooyan.dev.farsiwords.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

// Backward-compatible platform functions for default initialization (used by shared VM)
private var applicationContextRef: Context? = null

internal fun setApplicationContext(ctx: Context) {
    applicationContextRef = ctx.applicationContext
}

// Bloom and key are deprecated and removed in favor of exact lexicon

actual suspend fun loadLexiconPlatformSpecific(): ByteArray? {
    val ctx = applicationContextRef ?: return null
    return try {
        ctx.assets.open("words_5_be.bin").use { it.readBytes() }
    } catch (_: Exception) { null }
}

object WordCheckerInitializer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    fun init(context: Context) {
        setApplicationContext(context)
        // Proactively set key override from assets to avoid any parsing/fallback mismatches
        runCatching {
            // No key needed anymore
        }.onFailure { /* ignore */ }
        scope.launch {
            WordChecker.initialize()
        }
    }
}