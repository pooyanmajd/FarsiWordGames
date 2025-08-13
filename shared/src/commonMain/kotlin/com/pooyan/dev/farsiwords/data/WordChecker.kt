package com.pooyan.dev.farsiwords.data

import io.github.aakira.napier.Napier
import kotlin.concurrent.Volatile

/**
 * Offline word checker using Bloom filter with keyed hashing
 * - ~68k words, ~0.1% false positive rate
 * - ~116KB bloom.bin packaged with the app
 * - Double hashing (h1 + j*h2) for k indexes
 */
object WordChecker {

    private const val NUM_HASH_FUNCTIONS = 10

    // IMPORTANT: Replace with your generated 16-byte (32 hex chars) key
    // Or provide a 'key.hex' file and wire loading if preferred
    private const val K_HEX: String = "8BEBC47FC8F452CF86BFD4FD7143F8E7"

    @Volatile
    private var overrideKey: ByteArray? = null
    private val keyBytes: ByteArray get() = overrideKey ?: parseHexKey(K_HEX)

    private var bloomBits: ByteArray? = null
    private var bloomSizeBits: Int = 0
    private var isInitialized = false

    /** Initialize using platform loaders (default). Prefer the DI overload for tests/platform code. */
    suspend fun initialize(): Boolean {
        return initialize(object : BloomResources {
            override suspend fun loadBloom(): ByteArray? = loadBloomFilterPlatformSpecific()
            override suspend fun loadKeyHex(): String? = loadKeyHexFromResources()
        })
    }

    /** Initialize with injected resources loader (SOLID-friendly, testable) */
    suspend fun initialize(resources: BloomResources): Boolean {
        return try {
            // Try to load key from resources (if present)
            runCatching { resources.loadKeyHex() }.getOrNull()?.let { keyHex ->
                val clean = keyHex.trim().replace("\n", "").replace("\r", "")
                if (clean.length == 32) {
                    overrideKey = parseHexKey(clean)
                    Napier.i("WordChecker: Loaded key from resources")
                } else {
                    Napier.w("WordChecker: key.hex present but not 32 hex chars (len=${clean.length})")
                }
            }
            bloomBits = resources.loadBloom()
            bloomSizeBits = (bloomBits?.size ?: 0) * 8
            isInitialized = bloomBits != null && bloomSizeBits > 0
            Napier.i("WordChecker: Bloom loaded bytes=${bloomBits?.size ?: 0}, bits=$bloomSizeBits, ok=$isInitialized")
            isInitialized
        } catch (e: Exception) {
            Napier.e("WordChecker: Initialization failed", e)
            false
        }
    }

    /** Check if a word might be valid (Bloom filter check) */
    fun isWordPossiblyValid(word: String): Boolean {
        val bits = bloomBits ?: return false
        if (!isInitialized || bloomSizeBits <= 0) return false

        val normalizedWord = normalizePersian(word)
        Napier.d("WordChecker: verify word='$normalizedWord'")
        if (normalizedWord.length != 5) return false

        val wordBytes = normalizedWord.encodeToByteArray()

        // Double hashing: h1 + j*h2
        val h1 = sipHash24(wordBytes, keyBytes, seed = 0)
        val h2 = sipHash24(wordBytes, keyBytes, seed = 1)

        val positions = IntArray(NUM_HASH_FUNCTIONS) { j ->
            positiveMod(h1 + j.toLong() * h2, bloomSizeBits.toLong()).toInt()
        }
        Napier.d("WordChecker: positions=${positions.joinToString(limit = 3, truncated = ",...")}")

        for (pos in positions) {
            if (!getBit(bits, pos)) {
                Napier.d("WordChecker: missing bit at pos=$pos of $bloomSizeBits (bloomBytes=${bits.size}) keyPrefix=${keyBytes.toHexPrefix()}")
                return false
            }
        }
        return true
    }

    private fun normalizePersian(input: String): String {
        val base = input.trim()
            .replace("\u200C", "") // ZWNJ
            .replace("\u200F", "") // RTL mark
            .replace("\u200E", "") // LTR mark
            .replace("\u064A", "\u06CC") // ARABIC YEH -> FARSI YEH
            .replace("\u0643", "\u06A9") // ARABIC KAF -> KEHEH
            .replace("\u0629", "\u0647") // TEH MARBUTA -> HEH
            .replace("\u0670", "")       // SUPERSCRIPT ALEF
            .replace("\u064B", "")       // FATHATAN
            .replace("\u064C", "")       // DAMMATAN
            .replace("\u064D", "")       // KASRATAN
            .replace("\u064E", "")       // FATHA
            .replace("\u064F", "")       // DAMMA
            .replace("\u0650", "")       // KASRA
            .replace("\u0651", "")       // SHADDA
            .replace("\u0652", "")       // SUKUN
            .replace("\u0653", "")       // MADDAH ABOVE
            .replace("\u0654", "")       // HAMZA ABOVE
            .replace("\u0655", "")       // HAMZA BELOW
            .lowercase()
        return base
    }

    private fun getBit(bytes: ByteArray, bitIndex: Int): Boolean {
        val byteIndex = bitIndex ushr 3 // divide by 8
        val bitOffset = bitIndex and 7   // modulo 8
        if (byteIndex >= bytes.size) return false
        val b = bytes[byteIndex].toInt() and 0xFF
        return ((b ushr bitOffset) and 1) == 1
    }

    private fun parseHexKey(hex: String): ByteArray {
        val cleanHex = hex.replace(Regex("[^0-9A-Fa-f]"), "")
        require(cleanHex.length == 32) { "Key must be exactly 32 hex characters (128 bits)" }
        return cleanHex.chunked(2) { it.toString().toInt(16).toByte() }.toByteArray()
    }

    private fun ByteArray.toHexPrefix(): String {
        val max = minOf(8, size)
        val sb = StringBuilder()
        for (i in 0 until max) {
            sb.append(((this[i].toInt() and 0xFF)).toString(16).padStart(2, '0'))
        }
        return sb.toString().uppercase()
    }

    /** Load bloom filter from app resources (platform-specific) */
    private suspend fun loadBloomFilterFromResources(): ByteArray? = try {
        loadBloomFilterPlatformSpecific()
    } catch (_: Exception) {
        null
    }

    /**
     * SipHash-2-4 like function using HMAC-SHA256 surrogate to ensure portability.
     * Truncates to 64 bits (little-endian) for index generation.
     */
    private fun sipHash24(data: ByteArray, key: ByteArray, seed: Int): Long {
        require(key.size == 16) { "SipHash key must be 16 bytes" }
        val combined = data + seed.toLittleEndianBytes()
        val mac = hmacSha256(key, combined)
        return bytesToLongLE(mac, 0)
    }

    private fun Int.toLittleEndianBytes(): ByteArray = byteArrayOf(
        (this and 0xff).toByte(),
        ((this ushr 8) and 0xff).toByte(),
        ((this ushr 16) and 0xff).toByte(),
        ((this ushr 24) and 0xff).toByte()
    )

    // Portable, non-crypto placeholder to match Python generator's approach
    private fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray {
        val combined = key + data
        return combined.toPseudoSha256()
    }

    // Produces 32 bytes deterministically; matches Python side surrogate
    private fun ByteArray.toPseudoSha256(): ByteArray {
        var h = 0x6a09e667f3bcc908L
        for (b in this) {
            h = (h * 0x100000001b3L) xor (b.toLong() and 0xff)
            h = h xor (h ushr 29)
            h = (h shl 7) or (h ushr 57)
        }
        val out = ByteArray(32)
        var v = h
        for (i in 0 until 8) {
            val w = v + i * 0x428a2f98d728ae22L
            out[i * 4] = (w and 0xff).toByte()
            out[i * 4 + 1] = ((w ushr 8) and 0xff).toByte()
            out[i * 4 + 2] = ((w ushr 16) and 0xff).toByte()
            out[i * 4 + 3] = ((w ushr 24) and 0xff).toByte()
        }
        return out
    }

    private fun bytesToLongLE(bytes: ByteArray, offset: Int): Long {
        var result = 0L
        for (i in 0 until 8) {
            val idx = offset + i
            if (idx < bytes.size) {
                result = result or ((bytes[idx].toLong() and 0xff) shl (8 * i))
            }
        }
        return result
    }

    private fun positiveMod(value: Long, mod: Long): Long {
        val r = value % mod
        return if (r < 0) r + mod else r
    }

    // -------------------- Test helpers --------------------
    fun overrideKeyForTesting(hex: String) {
        overrideKey = parseHexKey(hex)
    }

    fun initializeForTesting(bloomBits: ByteArray, keyHex: String = K_HEX) {
        overrideKeyForTesting(keyHex)
        this.bloomBits = bloomBits
        this.bloomSizeBits = bloomBits.size * 8
        this.isInitialized = true
    }

    fun resetForTesting() {
        overrideKey = null
        bloomBits = null
        bloomSizeBits = 0
        isInitialized = false
    }
}

/** Platform-specific function to load bloom filter */
expect suspend fun loadBloomFilterPlatformSpecific(): ByteArray?

/** Platform-specific optional key loader (reads 32-hex chars) */
expect suspend fun loadKeyHexFromResources(): String?

/** Abstraction for loading bloom/key (for DI and tests) */
interface BloomResources {
    suspend fun loadBloom(): ByteArray?
    suspend fun loadKeyHex(): String?
}

/** Bloom filter configuration utilities (unchanged) */
data class BloomConfig(
    val expectedElements: Int = 68000,
    val falsePositiveRate: Double = 0.001,
    val hashFunctions: Int = 10,
    val sizeInBits: Int = 978237
) {
    val sizeInBytes: Int get() = (sizeInBits + 7) / 8

    companion object {
        fun calculateOptimalSize(elements: Int, falsePositiveRate: Double): BloomConfig {
            val ln2Squared = 0.4804530139182014
            val optimalBits = (-elements * kotlin.math.ln(falsePositiveRate) / ln2Squared).toInt()
            val optimalHashFunctions = (optimalBits.toDouble() / elements * kotlin.math.ln(2.0)).toInt()
            return BloomConfig(
                expectedElements = elements,
                falsePositiveRate = falsePositiveRate,
                hashFunctions = maxOf(1, optimalHashFunctions),
                sizeInBits = optimalBits
            )
        }
    }
} 