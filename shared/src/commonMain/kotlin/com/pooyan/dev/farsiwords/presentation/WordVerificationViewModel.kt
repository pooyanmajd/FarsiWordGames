package com.pooyan.dev.farsiwords.presentation

import com.pooyan.dev.farsiwords.data.WordChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Shared ViewModel for testing word verification functionality
 * Works on both iOS and Android
 */
class WordVerificationViewModel {
    
    private val scope = CoroutineScope(Dispatchers.Main)
    
    // UI State
    private val _uiState = MutableStateFlow(WordVerificationState())
    val uiState: StateFlow<WordVerificationState> = _uiState.asStateFlow()
    
    init {
        initializeWordChecker()
    }
    
    private fun initializeWordChecker() {
        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = "Initializing word checker...")
            
            val success = WordChecker.initialize()
            if (success) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isInitialized = true,
                    message = "✅ Word checker initialized successfully!\n🔍 Bloom filter loaded (116KB)\n📝 Ready to verify Persian words"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isInitialized = false,
                    message = "❌ Failed to initialize word checker\nCheck if bloom.bin is in resources"
                )
            }
        }
    }
    
    fun verifyWord(word: String) {
        if (!_uiState.value.isInitialized) {
            _uiState.value = _uiState.value.copy(
                message = "❌ Word checker not initialized"
            )
            return
        }
        
        val cleanWord = word.trim()
        if (cleanWord.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                currentWord = "",
                verificationResult = null,
                message = "Enter a Persian word to verify"
            )
            return
        }
        
        _uiState.value = _uiState.value.copy(
            currentWord = cleanWord,
            isVerifying = true
        )
        
        scope.launch {
            try {
                val isValid = WordChecker.isWordPossiblyValid(cleanWord)
                val result = WordVerificationResult(
                    word = cleanWord,
                    isValid = isValid,
                    confidence = if (isValid) "High (Bloom filter passed)" else "Certain (Not in word set)",
                    details = when {
                        cleanWord.length != 5 -> "❌ Must be exactly 5 letters"
                        !isValid -> "❌ Word not found in dictionary"
                        else -> "✅ Word might be valid (0.1% chance of false positive)"
                    }
                )
                
                _uiState.value = _uiState.value.copy(
                    isVerifying = false,
                    verificationResult = result,
                    message = "Word verification completed",
                    verificationHistory = _uiState.value.verificationHistory + result
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isVerifying = false,
                    message = "❌ Error during verification: ${e.message}"
                )
            }
        }
    }
    
    fun clearHistory() {
        _uiState.value = _uiState.value.copy(
            verificationHistory = emptyList(),
            verificationResult = null,
            currentWord = ""
        )
    }
    
    fun testCommonWords() {
        val testWords = listOf(
            "داشتن", "ساختن", "گرفتن", "یافتن", "اوردن", // Common verbs
            "خانه", "کتاب", "درس", "مدرسه", "دانش",       // Common nouns
            "abcde", "12345", "hello"                      // Invalid words
        )
        
        _uiState.value = _uiState.value.copy(
            message = "🧪 Testing ${testWords.size} words...",
            verificationHistory = emptyList()
        )
        
        scope.launch {
            val results = mutableListOf<WordVerificationResult>()
            testWords.forEach { word ->
                val isValid = WordChecker.isWordPossiblyValid(word)
                results.add(
                    WordVerificationResult(
                        word = word,
                        isValid = isValid,
                        confidence = if (isValid) "High" else "Certain",
                        details = if (isValid) "✅ Passed" else "❌ Failed"
                    )
                )
            }
            
            _uiState.value = _uiState.value.copy(
                verificationHistory = results,
                message = "🧪 Test completed: ${results.count { it.isValid }}/${results.size} words passed"
            )
        }
    }
}

/**
 * UI State for word verification screen
 */
data class WordVerificationState(
    val isLoading: Boolean = false,
    val isInitialized: Boolean = false,
    val isVerifying: Boolean = false,
    val currentWord: String = "",
    val verificationResult: WordVerificationResult? = null,
    val verificationHistory: List<WordVerificationResult> = emptyList(),
    val message: String = "Starting word verification system..."
)

/**
 * Result of word verification
 */
data class WordVerificationResult(
    val word: String,
    val isValid: Boolean,
    val confidence: String,
    val details: String
) 