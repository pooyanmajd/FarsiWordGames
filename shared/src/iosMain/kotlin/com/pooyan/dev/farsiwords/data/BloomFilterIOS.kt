package com.pooyan.dev.farsiwords.data

import kotlinx.cinterop.*
import platform.Foundation.*

/**
 * iOS-specific implementation for loading bloom filter from resources
 */
@OptIn(ExperimentalForeignApi::class)
actual suspend fun loadBloomFilterPlatformSpecific(): ByteArray? {
    return try {
        // Load from iOS app bundle
        val bundle = NSBundle.mainBundle
        val path = bundle.pathForResource("bloom", ofType = "bin")
        
        if (path != null) {
            val data = NSData.dataWithContentsOfFile(path)
            data?.let { nsData ->
                // Convert NSData to ByteArray
                val length = nsData.length.toInt()
                val bytes = ByteArray(length)
                
                // Use memScoped for safe memory operations
                memScoped {
                    val ptr = allocArray<ByteVar>(length)
                    nsData.getBytes(ptr, length = nsData.length)
                    for (i in 0 until length) {
                        bytes[i] = ptr[i]
                    }
                }
                bytes
            }
        } else {
            println("Error: bloom.bin not found in iOS bundle")
            null
        }
    } catch (e: Exception) {
        println("Error loading bloom filter on iOS: ${e.message}")
        null
    }
} 