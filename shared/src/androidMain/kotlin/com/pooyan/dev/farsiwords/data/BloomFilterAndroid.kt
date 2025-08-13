package com.pooyan.dev.farsiwords.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Android implementation using explicit context (no globals) */
class AndroidBloomResources(private val context: Context) : BloomResources {
    override suspend fun loadBloom(): ByteArray? = try {
        context.assets.open("bloom.bin").use { it.readBytes() }
    } catch (_: Exception) { null }

    override suspend fun loadKeyHex(): String? = try {
        context.assets.open("key.hex").bufferedReader().use { it.readText() }
    } catch (_: Exception) { null }
}

// Backward-compatible platform functions for default initialization (used by shared VM)
private var applicationContextRef: Context? = null

internal fun setApplicationContext(ctx: Context) {
    applicationContextRef = ctx.applicationContext
}

actual suspend fun loadBloomFilterPlatformSpecific(): ByteArray? {
    val ctx = applicationContextRef ?: return null
    return try {
        ctx.assets.open("bloom.bin").use { it.readBytes() }
    } catch (_: Exception) { null }
}

actual suspend fun loadKeyHexFromResources(): String? {
    val ctx = applicationContextRef ?: return null
    return try {
        ctx.assets.open("key.hex").bufferedReader().use { it.readText() }
    } catch (_: Exception) { null }
}

object WordCheckerInitializer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    fun init(context: Context) {
        setApplicationContext(context)
        // Proactively set key override from assets to avoid any parsing/fallback mismatches
        runCatching {
            val raw = context.assets.open("key.hex").bufferedReader().use { it.readText() }
            val hexOnly = buildString(raw.length) {
                for (ch in raw) {
                    val c = ch.lowercaseChar()
                    if (c in '0'..'9' || c in 'a'..'f') append(c)
                }
            }
            if (hexOnly.length >= 32) {
                val use = hexOnly.substring(0, 32)
                WordChecker.overrideKeyForTesting(use)
            }
        }.onFailure { /* ignore */ }
        scope.launch {
            WordChecker.initialize(AndroidBloomResources(context.applicationContext))
        }
    }
}