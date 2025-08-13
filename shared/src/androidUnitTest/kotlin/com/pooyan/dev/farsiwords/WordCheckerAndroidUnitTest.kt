package com.pooyan.dev.farsiwords

import com.pooyan.dev.farsiwords.data.WordChecker
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class WordCheckerAndroidUnitTest {

    private fun loadResource(name: String): ByteArray {
        val stream = javaClass.classLoader!!.getResourceAsStream(name)
            ?: error("Missing test resource: $name")
        return stream.readBytes()
    }

    private fun normalize(s: String): String {
        var t = s.trim()
        t = t.replace("\u200C", "").replace("\u200F", "").replace("\u200E", "")
        t = t.replace("\u064A", "\u06CC").replace("\u0643", "\u06A9").replace("\u0629", "\u0647")
        val removes = listOf('\u0670','\u064B','\u064C','\u064D','\u064E','\u064F','\u0650','\u0651','\u0652','\u0653','\u0654','\u0655')
        for (ch in removes) t = t.replace(ch.toString(), "")
        return t.lowercase()
    }

    private fun pseudoHmacSha256(key: ByteArray, data: ByteArray): ByteArray {
        var h = 0x6a09e667f3bcc908L
        for (b in key + data) {
            h = (h * 0x100000001b3L) xor (b.toLong() and 0xff)
            h = h xor (h ushr 29)
            h = (h shl 7) or (h ushr 57)
        }
        val out = ByteArray(32)
        var v = h
        for (i in 0 until 8) {
            val w = v + i * 0x428a2f98d728ae22L
            out[i*4+0] = (w and 0xff).toByte()
            out[i*4+1] = ((w ushr 8) and 0xff).toByte()
            out[i*4+2] = ((w ushr 16) and 0xff).toByte()
            out[i*4+3] = ((w ushr 24) and 0xff).toByte()
        }
        return out
    }

    private fun sipLike(data: ByteArray, key: ByteArray, seed: Int): Long {
        val combined = data + byteArrayOf(
            (seed and 0xff).toByte(),
            ((seed ushr 8) and 0xff).toByte(),
            ((seed ushr 16) and 0xff).toByte(),
            ((seed ushr 24) and 0xff).toByte(),
        )
        val mac = pseudoHmacSha256(key, combined)
        var result = 0L
        for (i in 0 until 8) {
            result = result or ((mac[i].toLong() and 0xff) shl (8 * i))
        }
        return result
    }

    private fun getBit(bytes: ByteArray, bitIndex: Int): Boolean {
        val byteIndex = bitIndex ushr 3
        val bitOffset = bitIndex and 7
        if (byteIndex >= bytes.size) return false
        val b = bytes[byteIndex].toInt() and 0xFF
        return ((b ushr bitOffset) and 1) == 1
    }

    @Test
    fun words_from_dataset_should_be_valid() {
        val valids = listOf("داشتن", "ساختن", "گرفتن", "یافتن", "اوردن")
        val normalized = valids.map { normalize(it) }

        // Build a tiny bloom that exactly matches our generator logic
        val expectedElements = normalized.size
        val falsePositiveRate = 0.001
        val ln2Squared = 0.4804530139182014
        val mBits = (-expectedElements * kotlin.math.ln(falsePositiveRate) / ln2Squared).toInt().coerceAtLeast(64)
        val mBytes = (mBits + 7) / 8
        val bloom = ByteArray(mBytes)

        val keyBytes = ByteArray(16) { (it * 13 + 7).toByte() } // deterministic test key
        val keyHex = keyBytes.joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }.uppercase()

        fun setBit(bit: Int) {
            val byteIndex = bit ushr 3
            val bitOffset = bit and 7
            bloom[byteIndex] = (bloom[byteIndex].toInt() or (1 shl bitOffset)).toByte()
        }

        for (w in normalized) {
            val wb = w.encodeToByteArray()
            val h1 = sipLike(wb, keyBytes, 0)
            val h2 = sipLike(wb, keyBytes, 1)
            repeat(10) { j ->
                val pos = (((h1 + j * h2) % (mBytes * 8)).let { if (it < 0) it + (mBytes * 8) else it }).toInt()
                setBit(pos)
            }
        }

        WordChecker.initializeForTesting(bloom, keyHex)

        for (w in valids) {
            assertTrue("Expected $w to be valid", WordChecker.isWordPossiblyValid(w))
        }

        val invalids = listOf("hello", "12345")
        for (w in invalids) {
            assertFalse("Expected $w to be invalid", WordChecker.isWordPossiblyValid(w))
        }
    }
}


