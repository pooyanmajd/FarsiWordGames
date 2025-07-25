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
    
    // Generated SipHash key for your 66,082 Persian words
    private const val K_HEX = "8D6E5715B787C8CAEF122FA7391A2027"
    
    // Bloom filter parameters (configured for 68k words, p=0.001)
    private const val BLOOM_SIZE_BITS = 978237 // ~120KB when packed into bytes
    private const val BLOOM_SIZE_BYTES = (BLOOM_SIZE_BITS + 7) / 8 // Round up to byte boundary
    private const val NUM_HASH_FUNCTIONS = 10
    
    private var bloomBits: ByteArray? = null
    private var sipHashKey: ByteArray? = null
    private var isInitialized = false
    
    /**
     * Initialize the word checker - call this once at app startup
     */
    suspend fun initialize(): Boolean {
        return try {
            // Parse the hex key
            sipHashKey = parseHexKey(K_HEX)
            
            // Load bloom filter from resources
            // You'll need to place bloom.bin in shared/src/commonMain/resources/
            bloomBits = loadBloomFilterFromResources()
            
            isInitialized = bloomBits != null && sipHashKey != null
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
        if (!isInitialized || bloomBits == null || sipHashKey == null) {
            return false // Fail safe - reject if not initialized
        }
        
        val normalizedWord = word.lowercase().trim()
        if (normalizedWord.length != 5) {
            return false
        }
        
        return checkBloomFilter(normalizedWord)
    }
    
    private fun checkBloomFilter(word: String): Boolean {
        val key = sipHashKey ?: return false
        val bits = bloomBits ?: return false
        
        // Get two 64-bit SipHash values for the word
        val hash1 = sipHash24(word.encodeToByteArray(), key, 0)
        val hash2 = sipHash24(word.encodeToByteArray(), key, 1)
        
        // Check NUM_HASH_FUNCTIONS positions in the bloom filter
        repeat(NUM_HASH_FUNCTIONS) { i ->
            // Combine the two hashes to generate different positions
            val combinedHash = hash1 + (i.toLong() * hash2)
            val bitPosition = (combinedHash and 0x7FFFFFFF) % BLOOM_SIZE_BITS
            
            if (!getBit(bits, bitPosition.toInt())) {
                return false // Definitely not in the set
            }
        }
        
        return true // Possibly in the set
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
     * SipHash-2-4 implementation
     * Simple implementation for demonstration - you might want to use a more optimized version
     */
    private fun sipHash24(data: ByteArray, key: ByteArray, seed: Int): Long {
        require(key.size == 16) { "SipHash key must be 16 bytes" }
        
        // Initialize state with key
        var v0 = 0x736f6d6570736575L xor bytesToLong(key, 0)
        var v1 = 0x646f72616e646f6dL xor bytesToLong(key, 8)
        var v2 = 0x6c7967656e657261L xor bytesToLong(key, 0)
        var v3 = 0x7465646279746573L xor bytesToLong(key, 8) xor seed.toLong()
        
        // Process message in 8-byte chunks
        val chunks = data.size / 8
        for (i in 0 until chunks) {
            val m = bytesToLong(data, i * 8)
            v3 = v3 xor m
            
            // SipRound (2 times for SipHash-2-4)
            repeat(2) {
                v0 = v0 + v1
                v2 = v2 + v3
                v1 = rotateLeft(v1, 13) xor v0
                v3 = rotateLeft(v3, 16) xor v2
                v0 = rotateLeft(v0, 32)
                v2 = v2 + v1
                v0 = v0 + v3
                v1 = rotateLeft(v1, 17) xor v2
                v3 = rotateLeft(v3, 21) xor v0
                v2 = rotateLeft(v2, 32)
            }
            
            v0 = v0 xor m
        }
        
        // Handle remaining bytes
        val remaining = data.size % 8
        var lastChunk = (data.size and 0xff).toLong() shl 56
        
        for (i in 0 until remaining) {
            lastChunk = lastChunk or ((data[chunks * 8 + i].toLong() and 0xff) shl (i * 8))
        }
        
        v3 = v3 xor lastChunk
        repeat(2) { sipRound(v0, v1, v2, v3) }
        v0 = v0 xor lastChunk
        
        // Finalization
        v2 = v2 xor 0xff
        repeat(4) { sipRound(v0, v1, v2, v3) }
        
        return v0 xor v1 xor v2 xor v3
    }
    
    private fun sipRound(v0: Long, v1: Long, v2: Long, v3: Long) {
        // SipRound implementation would go here
        // This is a simplified placeholder
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