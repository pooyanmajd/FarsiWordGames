package com.pooyan.dev.farsiwords

import com.pooyan.dev.farsiwords.data.WordChecker
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WordCheckerTest {

    private val testKeyHex = "0123456789ABCDEF0123456789ABCDEF"
    private val words = listOf("داشتن", "ساختن", "یافتن")

    @BeforeTest
    fun setup() {
        // Build a small bloom filter in-memory
        val expectedElements = words.size
        val falsePositiveRate = 0.01
        val ln2Squared = 0.4804530139182014
        val mBits = (-expectedElements * kotlin.math.ln(falsePositiveRate) / ln2Squared).toInt().coerceAtLeast(128)
        val mBytes = (mBits + 7) / 8
        val bloom = ByteArray(mBytes)

        fun toLE(i: Int): ByteArray = byteArrayOf(
            (i and 0xff).toByte(),
            ((i ushr 8) and 0xff).toByte(),
            ((i ushr 16) and 0xff).toByte(),
            ((i ushr 24) and 0xff).toByte()
        )

        fun pseudoHmacSha256(key: ByteArray, data: ByteArray): ByteArray {
            var h = 0x6a09e667f3bcc908uL
            for (b in key + data) {
                h = (h * 0x100000001b3uL) xor (b.toUByte().toULong())
                h = h xor (h shr 29)
                h = ((h shl 7) or (h shr 57))
            }
            val out = ByteArray(32)
            var v = h
            for (i in 0 until 8) {
                val w = v + i.toULong() * 0x428a2f98d728ae22uL
                out[i * 4] = (w and 0xffu).toByte()
                out[i * 4 + 1] = ((w shr 8) and 0xffu).toByte()
                out[i * 4 + 2] = ((w shr 16) and 0xffu).toByte()
                out[i * 4 + 3] = ((w shr 24) and 0xffu).toByte()
            }
            return out
        }

        fun sipLike(data: ByteArray, key: ByteArray, seed: Int): Long {
            val combined = data + toLE(seed)
            val mac = pseudoHmacSha256(key, combined)
            var result = 0L
            for (i in 0 until 8) {
                result = result or ((mac[i].toLong() and 0xff) shl (8 * i))
            }
            return result
        }

        val keyBytes = testKeyHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

        for (word in words) {
            val data = word.encodeToByteArray()
            val h1 = sipLike(data, keyBytes, 0)
            val h2 = sipLike(data, keyBytes, 1)
            repeat(10) { j ->
                val bit = ((h1 + j * h2) % mBits).toInt().let { if (it < 0) it + mBits else it }
                val byteIndex = bit / 8
                val bitOffset = bit % 8
                bloom[byteIndex] = (bloom[byteIndex].toInt() or (1 shl bitOffset)).toByte()
            }
        }

        WordChecker.initializeForTesting(bloom, testKeyHex)
    }

    @AfterTest
    fun tearDown() {
        WordChecker.resetForTesting()
    }

    @Test
    fun valid_words_should_return_true() {
        for (w in words) {
            assertTrue(WordChecker.isWordPossiblyValid(w), "Expected '$w' to be possibly valid")
        }
    }

    @Test
    fun invalid_words_should_return_false() {
        val invalids = listOf("سلام!", "hello", "12345", "ک")
        for (w in invalids) {
            assertFalse(WordChecker.isWordPossiblyValid(w), "Expected '$w' to be invalid")
        }
    }
}