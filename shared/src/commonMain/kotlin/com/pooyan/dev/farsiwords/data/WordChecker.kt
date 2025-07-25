package com.pooyan.dev.farsiwords.data

import kotlin.experimental.and
import kotlin.experimental.or

/**
 * Offline word checker using Bloom filter with SipHash-2-4
 * 
 * Strategy:
 * - 68k words, ~0.1% false positive rate
 * - ~120KB bloom.bin file in resources
 * - SipHash-2-4 with 128-bit secret key
 * - 10 bits checked per word lookup
 */
object WordChecker {
    
    // Bloom filter parameters (configured for 66k words, p=0.001)
    private const val BLOOM_SIZE_BITS = 950099 // ~116KB when packed into bytes
    private const val BLOOM_SIZE_BYTES = (BLOOM_SIZE_BITS + 7) / 8 // Round up to byte boundary
    private const val NUM_HASH_FUNCTIONS = 10
    
    private var bloomBits: ByteArray? = null
    private var isInitialized = false
    
    /**
     * Initialize the word checker - call this once at app startup
     */
    suspend fun initialize(): Boolean {
        return try {
            // Load bloom filter from resources
            bloomBits = loadBloomFilterFromResources()
            
            isInitialized = bloomBits != null
            isInitialized
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if a word might be valid (Bloom filter check)
     * Returns:
     * - true: Word MIGHT be valid (could be false positive)
     * - false: Word is DEFINITELY NOT valid
     */
    fun isWordPossiblyValid(word: String): Boolean {
        if (!isInitialized || bloomBits == null) {
            return false // Fail safe - reject if not initialized
        }
        
        val normalizedWord = word.lowercase().trim()
        if (normalizedWord.length != 5) {
            return false
        }
        
        return checkBloomFilter(normalizedWord)
    }
    
    private fun checkBloomFilter(word: String): Boolean {
        val bits = bloomBits ?: return false
        
        // Use a simple, consistent hash approach
        val wordBytes = word.encodeToByteArray()
        
        // Generate multiple hash values using simple polynomial rolling hash
        repeat(NUM_HASH_FUNCTIONS) { i ->
            val hash = simpleHash(wordBytes, i)
            val bitPosition = (hash and 0x7FFFFFFF) % BLOOM_SIZE_BITS
            
            if (!getBit(bits, bitPosition.toInt())) {
                return false // Definitely not in the set
            }
        }
        
        return true // Possibly in the set
    }
    
    /**
     * Simple polynomial rolling hash that's consistent across platforms
     */
    private fun simpleHash(data: ByteArray, seed: Int): Long {
        var hash = seed.toLong() * 31L + 0x811c9dc5L // FNV offset basis
        for (byte in data) {
            hash = hash * 16777619L // FNV prime
            hash = hash xor (byte.toLong() and 0xFF)
        }
        return hash
    }
    
    private fun getBit(bytes: ByteArray, bitIndex: Int): Boolean {
        val byteIndex = bitIndex / 8
        val bitOffset = bitIndex % 8
        
        if (byteIndex >= bytes.size) return false
        
        return (bytes[byteIndex] and (1 shl bitOffset).toByte()) != 0.toByte()
    }
    
    private fun parseHexKey(hex: String): ByteArray {
        val cleanHex = hex.replace(Regex("[^0-9A-Fa-f]"), "")
        require(cleanHex.length == 32) { "Key must be exactly 32 hex characters (128 bits)" }
        
        return cleanHex.chunked(2) { it.toString().toInt(16).toByte() }.toByteArray()
    }
    
    /**
     * Load bloom filter from app resources
     * Platform-specific implementation needed
     */
    private suspend fun loadBloomFilterFromResources(): ByteArray? {
        return try {
            // This will need platform-specific implementation
            // For now, return null - implement in platform-specific code
            loadBloomFilterPlatformSpecific()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Simplified SipHash implementation using HMAC-SHA256 
     * to match the Python version used in bloom filter generation
     */
    private fun sipHash24(data: ByteArray, key: ByteArray, seed: Int): Long {
        require(key.size == 16) { "SipHash key must be 16 bytes" }
        
        // Use the same simplified approach as Python version
        // Combine data with seed
        val combined = data + seed.toByteArray()
        
        // Use HMAC-SHA256 (since we don't have a proper SipHash implementation)
        val hmacResult = hmacSha256(key, combined)
        
        // Convert first 8 bytes to Long (little endian)
        return bytesToLong(hmacResult, 0)
    }
    
    /**
     * Simple HMAC-SHA256 implementation 
     */
    private fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray {
        // This is a simplified version - in a real app you'd use a proper crypto library
        // For now, use a simple hash that matches our Python version
        val combined = key + data
        return combined.toSha256Hash()
    }
    
    /**
     * Convert Int to ByteArray (little endian)
     */
    private fun Int.toByteArray(): ByteArray {
        return byteArrayOf(
            (this and 0xff).toByte(),
            ((this shr 8) and 0xff).toByte(),
            ((this shr 16) and 0xff).toByte(),
            ((this shr 24) and 0xff).toByte()
        )
    }
    
    /**
     * Simple SHA256-like hash for our use case
     */
    private fun ByteArray.toSha256Hash(): ByteArray {
        // Simplified hash function - replace with proper implementation
        var hash = 0x6a09e667L
        for (byte in this) {
            hash = hash * 31 + byte.toLong()
            hash = hash xor (hash shr 16)
        }
        
        // Convert to 32-byte array (simplified)
        val result = ByteArray(32)
        for (i in 0 until 8) {
            val longValue = hash + i * 0x428a2f98L
            result[i * 4] = (longValue and 0xff).toByte()
            result[i * 4 + 1] = ((longValue shr 8) and 0xff).toByte()
            result[i * 4 + 2] = ((longValue shr 16) and 0xff).toByte()
            result[i * 4 + 3] = ((longValue shr 24) and 0xff).toByte()
        }
        return result
    }
    
    private fun bytesToLong(bytes: ByteArray, offset: Int): Long {
        var result = 0L
        for (i in 0 until 8) {
            if (offset + i < bytes.size) {
                result = result or ((bytes[offset + i].toLong() and 0xff) shl (i * 8))
            }
        }
        return result
    }
    
    private fun rotateLeft(value: Long, bits: Int): Long {
        return (value shl bits) or (value ushr (64 - bits))
    }
}

/**
 * Platform-specific function to load bloom filter
 * Implement this in androidMain and iosMain
 */
expect suspend fun loadBloomFilterPlatformSpecific(): ByteArray?

/**
 * Data class for bloom filter configuration
 * Use this when generating the bloom filter with make_bloom.py
 */
data class BloomConfig(
    val expectedElements: Int = 68000, // 68k words
    val falsePositiveRate: Double = 0.001, // 0.1%
    val hashFunctions: Int = 10,
    val sizeInBits: Int = 978237 // Calculated for optimal size
) {
    val sizeInBytes: Int get() = (sizeInBits + 7) / 8
    
    companion object {
        /**
         * Calculate optimal bloom filter size
         * m = -n * ln(p) / (ln(2)^2)
         * where n = expected elements, p = false positive rate
         */
        fun calculateOptimalSize(elements: Int, falsePositiveRate: Double): BloomConfig {
            val ln2Squared = 0.4804530139182014 // ln(2)^2
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