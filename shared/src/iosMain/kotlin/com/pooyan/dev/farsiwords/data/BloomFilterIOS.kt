package com.pooyan.dev.farsiwords.data

import kotlinx.cinterop.*
import platform.Foundation.*

@OptIn(ExperimentalForeignApi::class)
actual suspend fun loadLexiconPlatformSpecific(): ByteArray? {
    return try {
        val bundle = NSBundle.mainBundle
        val path = bundle.pathForResource("words_5_be", ofType = "bin")
        if (path != null) {
            val data = NSData.dataWithContentsOfFile(path)
            data?.let { nsData ->
                val length = nsData.length.toInt()
                val bytes = ByteArray(length)
                memScoped {
                    val ptr = allocArray<ByteVar>(length)
                    nsData.getBytes(ptr, length = nsData.length)
                    for (i in 0 until length) {
                        bytes[i] = ptr[i]
                    }
                }
                bytes
            }
        } else null
    } catch (_: Exception) {
        null
    }
}