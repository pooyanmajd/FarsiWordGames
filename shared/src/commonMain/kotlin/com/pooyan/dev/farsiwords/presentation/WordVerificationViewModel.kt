package com.pooyan.dev.farsiwords.presentation

import com.pooyan.dev.farsiwords.data.WordChecker
import com.pooyan.dev.farsiwords.domain.model.FarsiWord
import com.pooyan.dev.farsiwords.domain.model.Game
import com.pooyan.dev.farsiwords.domain.model.GameState
import com.pooyan.dev.farsiwords.domain.model.Guess
import com.pooyan.dev.farsiwords.domain.model.Letter
import com.pooyan.dev.farsiwords.domain.model.WordDifficulty
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Shared ViewModel for word verification - works on ALL platforms
 * - Cross-platform lifecycle management
 * - Uses constructor injection for dependencies (SOLID)
 * - Uses Napier for cross-platform logging
 * - Contains all business logic
 */
class WordVerificationViewModel(
    private val wordChecker: WordChecker
) {

    // CoroutineScope for this ViewModel
    private val viewModelScope = CoroutineScope(
        context = SupervisorJob() + Dispatchers.Main.immediate
    )

    // UI State
    private val _uiState = MutableStateFlow(WordVerificationState())
    val uiState: StateFlow<WordVerificationState> = _uiState.asStateFlow()

    // Add game state
    private val _gameState = MutableStateFlow(
        Game(
            targetWord = FarsiWord(
                id = 1,
                word = "داشتن",
                difficulty = WordDifficulty.MEDIUM,
                difficultyDescription = "متوسط",
                letters = listOf("د", "ا", "ش", "ت", "ن"),
                letterSet = "داشتن",
                hasRareLetter = false,
                isAnswer = true,
                pack = "pack_1"
            ),
            guesses = MutableList(6) { Guess() },
            currentGuessIndex = 0
        )
    )
    val gameState: StateFlow<Game> = _gameState.asStateFlow()

    init {
        Napier.d("Shared WordVerificationViewModel initialized")
        initializeWordChecker()
        // Initialize game here if needed
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
                    timestamp = Clock.System.now().toEpochMilliseconds()
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
                        timestamp = Clock.System.now().toEpochMilliseconds()
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

    // Borrowed/adapted: Add letter to current guess
    fun addLetter(letter: String) {
        viewModelScope.launch {
            _gameState.update { current ->
                if (current.isGameOver) return@update current
                
                val currentGuess = current.guesses[current.currentGuessIndex]
                val position = currentGuess.letters.indexOfFirst { it.char.isEmpty() }
                if (position == -1) return@update current
                
                val updatedLetters = currentGuess.letters.toMutableList().apply {
                    this[position] = Letter(letter)
                }
                val updatedGuesses = current.guesses.toMutableList().apply {
                    this[current.currentGuessIndex] = currentGuess.copy(letters = updatedLetters)
                }
                
                current.copy(guesses = updatedGuesses)
            }
        }
    }

    // Borrowed/adapted: Remove last letter from current guess
    fun removeLetter() {
        viewModelScope.launch {
            _gameState.update { current ->
                if (current.isGameOver) return@update current
                
                val currentGuess = current.guesses[current.currentGuessIndex]
                val position = currentGuess.letters.indexOfLast { it.char.isNotEmpty() }
                if (position == -1) return@update current
                
                val updatedLetters = currentGuess.letters.toMutableList().apply {
                    this[position] = Letter()
                }
                val updatedGuesses = current.guesses.toMutableList().apply {
                    this[current.currentGuessIndex] = currentGuess.copy(letters = updatedLetters)
                }
                
                current.copy(guesses = updatedGuesses)
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    fun submitGuess() {
        viewModelScope.launch {
            _gameState.update { current ->
                if (current.isGameOver || !current.guesses[current.currentGuessIndex].isComplete) return@update current
                
                val guess = current.guesses[current.currentGuessIndex]
                
                // Pre-validate with Bloom (your existing checker)
                if (!wordChecker.isWordPossiblyValid(guess.word)) {
                    // Handle invalid (e.g., not in dictionary) - perhaps shake animation in UI
                    return@update current.copy() // Or add error state
                }
                
                // Evaluate against target
                val evaluated = guess.evaluate(current.targetWord.word)
                val updatedGuesses = current.guesses.toMutableList().apply {
                    this[current.currentGuessIndex] = evaluated
                }
                
                val isCorrect = evaluated.word == current.targetWord.word
                val nextIndex = current.currentGuessIndex + 1
                val isLost = nextIndex >= current.guesses.size && !isCorrect
                
                val newState = when {
                    isCorrect -> GameState.WON
                    isLost -> GameState.LOST
                    else -> GameState.PLAYING
                }
                
                val updatedKeyboard = if (newState == GameState.PLAYING) current.updateKeyboardState() else current.keyboardState
                
                current.copy(
                    guesses = updatedGuesses,
                    currentGuessIndex = if (isCorrect || isLost) current.currentGuessIndex else nextIndex,
                    gameState = newState,
                    keyboardState = updatedKeyboard,
                    endTime = if (newState != GameState.PLAYING) Clock.System.now().toEpochMilliseconds() else null
                )
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