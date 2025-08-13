package com.pooyan.dev.farsiwords

import com.pooyan.dev.farsiwords.data.WordChecker
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

    @Test
    fun words_from_dataset_should_be_valid_with_lexicon() {
        val valids = listOf("داشتن", "ساختن", "گرفتن", "یافتن", "اوردن")
        val normalized = valids.map { normalize(it) }
        // Build a minimal lexicon binary: 4-byte BE count + records (10 bytes each)
        val count = normalized.size
        val header = byteArrayOf(
            ((count ushr 24) and 0xFF).toByte(),
            ((count ushr 16) and 0xFF).toByte(),
            ((count ushr 8) and 0xFF).toByte(),
            (count and 0xFF).toByte()
        )
        val records = mutableListOf<Byte>()
        for (w in normalized.sorted()) {
            require(w.length == 5)
            for (ch in w) {
                val code = ch.code
                records.add(((code ushr 8) and 0xFF).toByte())
                records.add((code and 0xFF).toByte())
            }
        }
        val lexicon = header + records.toByteArray()
        WordChecker.initializeForTestingLexicon(lexicon)

        for (w in valids) {
            assertTrue("Expected $w to be valid", WordChecker.isWordPossiblyValid(w))
        }

        val invalids = listOf("hello", "12345")
        for (w in invalids) {
            assertFalse("Expected $w to be invalid", WordChecker.isWordPossiblyValid(w))
        }
    }
}


