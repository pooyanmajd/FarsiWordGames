package com.pooyan.dev.farsiwords.data

import io.github.aakira.napier.Napier

/**
 * Offline word checker using an exact lexicon binary with fixed-size records.
 * - Exact, no false positives/negatives
 * - ~66k words x 10 bytes = ~650KB
 */
object WordChecker {

    // Exact lexicon: fixed-length UTF-16BE 5-letter words for binary search
    private var lexiconBytes: ByteArray? = null
    private var lexiconCount: Int = 0
    private const val LEXICON_RECORD_SIZE: Int = 10 // 5 UTF-16 code units, big-endian
    private var isInitialized = false

    /** Initialize using platform lexicon loader */
    suspend fun initialize(): Boolean {
        return initializeInternal()
    }

    /** Public readiness flag for UI to gate navigation */
    fun isReady(): Boolean = isInitialized

    private suspend fun initializeInternal(): Boolean {
        return try {
            val lexBytes = runCatching { loadLexiconPlatformSpecific() }.getOrNull()
            if (lexBytes != null && lexBytes.size >= 4) {
                val count = ((lexBytes[0].toInt() and 0xFF) shl 24) or
                        ((lexBytes[1].toInt() and 0xFF) shl 16) or
                        ((lexBytes[2].toInt() and 0xFF) shl 8) or
                        (lexBytes[3].toInt() and 0xFF)
                val expectedSize = 4 + count * LEXICON_RECORD_SIZE
                if (count > 0 && lexBytes.size >= expectedSize) {
                    lexiconBytes = lexBytes
                    lexiconCount = count
                    isInitialized = true
                    Napier.i("WordChecker: Lexicon loaded records=$lexiconCount bytes=${lexBytes.size}")
                } else {
                    Napier.w("WordChecker: Lexicon size mismatch (count=$count, bytes=${lexBytes.size})")
                    isInitialized = false
                }
            } else {
                Napier.w("WordChecker: Lexicon missing or too small (${lexBytes?.size ?: 0} bytes)")
                isInitialized = false
            }
            isInitialized
        } catch (e: Exception) {
            Napier.e("WordChecker: Initialization failed", e)
            false
        }
    }

    /** Check if a word is valid using exact lexicon */
    fun isWordPossiblyValid(word: String): Boolean {
        val normalizedWord = normalizePersian(word)
        if (normalizedWord.length != 5) return false
        val bytes = lexiconBytes ?: return false
        if (!isInitialized || lexiconCount <= 0) return false
        val key = encodeUtf16BEFixed5(normalizedWord) ?: return false
        return binarySearchLexicon(bytes, lexiconCount, key)
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

    private fun encodeUtf16BEFixed5(s: String): ByteArray? {
        if (s.length != 5) return null
        val out = ByteArray(LEXICON_RECORD_SIZE)
        var idx = 0
        for (i in 0 until 5) {
            val code = s[i].code
            out[idx++] = ((code ushr 8) and 0xFF).toByte()
            out[idx++] = (code and 0xFF).toByte()
        }
        return out
    }

    private fun binarySearchLexicon(bytes: ByteArray, count: Int, key: ByteArray): Boolean {
        var low = 0
        var high = count - 1
        while (low <= high) {
            val mid = (low + high) ushr 1
            val cmp = compareRecord(bytes, mid, key)
            when {
                cmp < 0 -> low = mid + 1
                cmp > 0 -> high = mid - 1
                else -> return true
            }
        }
        return false
    }

    private fun compareRecord(bytes: ByteArray, index: Int, key: ByteArray): Int {
        val base = 4 + index * LEXICON_RECORD_SIZE
        var i = 0
        while (i < LEXICON_RECORD_SIZE) {
            val a = bytes[base + i].toInt() and 0xFF
            val b = key[i].toInt() and 0xFF
            if (a != b) return a - b
            i++
        }
        return 0
    }

}

/** Platform-specific function to load exact lexicon (UTF-16BE fixed-length records) */
expect suspend fun loadLexiconPlatformSpecific(): ByteArray?
