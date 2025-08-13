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

    @Test
    fun words_from_dataset_should_be_valid() {
        val bloom = loadResource("bloom.bin")
        val keyHex = String(loadResource("key.hex"))
        WordChecker.initializeForTesting(bloom, keyHex)

        val valids = listOf("داشتن", "ساختن", "گرفتن", "یافتن", "اوردن")
        for (w in valids) {
            assertTrue("Expected $w to be valid", WordChecker.isWordPossiblyValid(w))
        }

        val invalids = listOf("hello", "12345")
        for (w in invalids) {
            assertFalse("Expected $w to be invalid", WordChecker.isWordPossiblyValid(w))
        }
    }
}


