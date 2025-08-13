package com.pooyan.dev.farsiwords.presentation

import com.pooyan.dev.farsiwords.data.WordChecker
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.ExperimentalTime

/**
 * Shared ViewModel for word verification - works on ALL platforms
 * - Cross-platform lifecycle management
 * - Uses Koin for dependency injection
 * - Uses Napier for cross-platform logging
 * - Contains all business logic
 */
class WordVerificationViewModel : KoinComponent {

    // Koin injection
    private val wordChecker: WordChecker by inject()

    // CoroutineScope for this ViewModel
    private val viewModelScope = CoroutineScope(
        context = SupervisorJob() + Dispatchers.Main.immediate
    )

    // UI State
    private val _uiState = MutableStateFlow(WordVerificationState())
    val uiState: StateFlow<WordVerificationState> = _uiState.asStateFlow()

    init {
        Napier.d("Shared WordVerificationViewModel initialized")
        initializeWordChecker()
    }

    private fun initializeWordChecker() {
        Napier.i("Starting word checker initialization")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                message = "Initializing word checker..."
            )

            try {
                val success = wordChecker.initialize()
                if (success) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isInitialized = true,
                        message = "✅ Word checker initialized successfully!\n🔍 Bloom filter loaded (116KB)\n📝 Ready to verify Persian words"
                    )
                    Napier.i("Word checker initialized successfully")
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isInitialized = false,
                        message = "❌ Failed to initialize word checker\nCheck if bloom.bin is in resources"
                    )
                    Napier.e("Word checker initialization failed")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isInitialized = false,
                    message = "❌ Error during initialization: ${e.message}"
                )
                Napier.e("Word checker initialization error", e)
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    fun verifyWord(word: String) {
        if (word.isBlank()) {
            Napier.w("Attempted to verify empty word")
            return
        }

        Napier.d("Verifying word: '$word'")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val isValid = wordChecker.isWordPossiblyValid(word.trim())
                val result = WordVerificationResult(
                    word = word.trim(),
                    isValid = isValid,
                    timestamp = kotlin.time.Clock.System.now().toEpochMilliseconds()
                )

                val updatedHistory = listOf(result) + _uiState.value.verificationHistory
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    verificationHistory = updatedHistory
                )

                Napier.i("Word '$word' verification result: ${if (isValid) "VALID" else "INVALID"}")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                Napier.e("Error verifying word '$word'", e)
            }
        }
    }

    fun clearHistory() {
        Napier.d("Clearing verification history")
        _uiState.value = _uiState.value.copy(verificationHistory = emptyList())
    }

    @OptIn(ExperimentalTime::class)
    fun testCommonWords() {
        Napier.i("Starting common words test")
        val testWords = listOf("داشتن", "ساختن", "گرفتن", "یافتن", "اوردن", "hello", "12345")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                message = "🧪 Testing common words..."
            )

            try {
                val results = testWords.map { word ->
                    val isValid = wordChecker.isWordPossiblyValid(word)
                    Napier.d("Test word '$word': ${if (isValid) "VALID" else "INVALID"}")
                    WordVerificationResult(
                        word = word,
                        isValid = isValid,
                        timestamp = kotlin.time.Clock.System.now().toEpochMilliseconds()
                    )
                }

                val validCount = results.count { it.isValid }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    verificationHistory = results + _uiState.value.verificationHistory,
                    message = "🧪 Test completed: $validCount/${testWords.size} words passed"
                )

                Napier.i("Common words test completed: $validCount/${testWords.size} words valid")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "❌ Test failed: ${e.message}"
                )
                Napier.e("Common words test failed", e)
            }
        }
    }

    fun onCleared() {
        Napier.d("Shared WordVerificationViewModel cleared")
        viewModelScope.cancel()
    }
}

/**
 * UI State for word verification screen
 */
data class WordVerificationState(
    val isLoading: Boolean = false,
    val isInitialized: Boolean = false,
    val message: String = "Initializing...",
    val verificationHistory: List<WordVerificationResult> = emptyList()
)

/**
 * Result of a word verification
 */
data class WordVerificationResult(
    val word: String,
    val isValid: Boolean,
    val timestamp: Long
) 