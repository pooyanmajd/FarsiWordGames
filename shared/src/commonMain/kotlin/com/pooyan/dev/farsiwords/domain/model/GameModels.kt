package com.pooyan.dev.farsiwords.domain.model

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Core game models for Farsi Wordle
 */

// Letter states for game feedback
enum class LetterState { UNKNOWN, CORRECT, WRONG_POSITION, NOT_IN_WORD, FILLED }

// Game states
enum class GameState { PLAYING, WON, LOST, IDLE }

// Word difficulty levels
enum class WordDifficulty { EASY, MEDIUM, HARD, EXPERT }

// User authentication states
enum class AuthState { ANONYMOUS, GOOGLE, APPLE, LOGGED_OUT }

// Letter in game grid
data class Letter(
    val char: String = "",
    val state: LetterState = LetterState.UNKNOWN
)

// Single guess (row of 5 letters)
data class Guess(
    val letters: List<Letter> = List(5) { Letter() },
    val timestamp: Long? = null
) {
    val word: String get() = letters.joinToString("") { it.char }
    val isComplete: Boolean get() = letters.all { it.char.isNotEmpty() }

    // Borrowed: Evaluate against target word
    fun evaluate(target: String): Guess {
        if (!isComplete) return this

        val targetLetters = target.toList().map { it.toString() }
        val guessLetters = letters.map { it.char }
        val newStates = MutableList(5) { LetterState.NOT_IN_WORD }
        val remaining = targetLetters.toMutableList()

        // First pass: exact positions (CORRECT)
        for (i in letters.indices) {
            if (guessLetters[i] == targetLetters[i]) {
                newStates[i] = LetterState.CORRECT
                remaining.removeAt(remaining.indexOf(guessLetters[i]))
            }
        }

        // Second pass: wrong positions (WRONG_POSITION)
        for (i in letters.indices) {
            if (newStates[i] != LetterState.CORRECT && remaining.contains(guessLetters[i])) {
                newStates[i] = LetterState.WRONG_POSITION
                remaining.removeAt(remaining.indexOf(guessLetters[i]))
            }
        }

        return copy(
            letters = letters.mapIndexed { idx, letter ->
                letter.copy(state = newStates[idx])
            }
        )
    }
}

// Word entity matching your rich JSON structure
data class FarsiWord(
    val id: Int,
    val word: String,
    val difficulty: WordDifficulty,
    val difficultyDescription: String,
    val letters: List<String>, // Individual Persian characters
    val letterSet: String, // All letters as single string
    val hasRareLetter: Boolean,
    val isAnswer: Boolean, // Can be used as target word
    val pack: String, // pack_1, pack_2, etc.
    val meaning: String? = null,
    val category: String? = null
) {
    companion object {
        fun fromJson(json: Map<String, Any>): FarsiWord {
            return FarsiWord(
                id = (json["id"] as? Number)?.toInt() ?: 0,
                word = json["word"] as? String ?: "",
                difficulty = when (json["difficulty"] as? String) {
                    "easy" -> WordDifficulty.EASY
                    "medium" -> WordDifficulty.MEDIUM
                    "hard" -> WordDifficulty.HARD
                    else -> WordDifficulty.MEDIUM
                },
                difficultyDescription = json["difficultyDescription"] as? String ?: "",
                letters = (json["letters"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                letterSet = json["letterSet"] as? String ?: "",
                hasRareLetter = json["hasRareLetter"] as? Boolean ?: false,
                isAnswer = json["isAnswer"] as? Boolean ?: true,
                pack = json["pack"] as? String ?: "pack_1"
            )
        }
    }
}

// User profile and progress
data class UserProfile(
    val userId: String,
    val authState: AuthState,
    val displayName: String?,
    val email: String?,
    val totalCoins: Int = 0,
    val totalPoints: Int = 0,
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val purchasedPackages: Set<String> = emptySet(),
    val unlockedWords: Set<String> = emptySet()
)

// Daily word challenge
data class DailyChallenge(
    val date: String, // YYYY-MM-DD format
    val wordId: String,
    val isCompleted: Boolean = false,
    val attempts: Int = 0,
    val timeToComplete: Long? = null
)

// Borrowed/adapted: Keyboard state for UI
data class KeyboardState(val letterStates: Map<String, LetterState> = emptyMap())

data class Game @OptIn(ExperimentalTime::class) constructor(
    val targetWord: FarsiWord,
    val guesses: MutableList<Guess> = MutableList(6) { Guess() },
    val currentGuessIndex: Int = 0,
    val gameState: GameState = GameState.PLAYING,
    val keyboardState: KeyboardState = KeyboardState(),
    val startTime: Long = Clock.System.now().toEpochMilliseconds(),
    val endTime: Long? = null,
    val isDailyChallenge: Boolean = false,
    val coinsEarned: Int = 0,
    val pointsEarned: Int = 0
) {
    val isGameOver: Boolean get() = gameState == GameState.WON || gameState == GameState.LOST

    @OptIn(ExperimentalTime::class)
    val timeElapsed: Long get() = (endTime ?: Clock.System.now().toEpochMilliseconds()) - startTime

    // Borrowed: Update keyboard states from all guesses
    fun updateKeyboardState(): KeyboardState {
        val states = mutableMapOf<String, LetterState>()
        guesses.flatMap { it.letters }.forEach { letter ->
            if (letter.char.isNotEmpty()) {
                val current = states[letter.char] ?: LetterState.UNKNOWN
                states[letter.char] = when {
                    letter.state == LetterState.CORRECT -> LetterState.CORRECT
                    letter.state == LetterState.WRONG_POSITION && current != LetterState.CORRECT -> LetterState.WRONG_POSITION
                    letter.state == LetterState.NOT_IN_WORD && current == LetterState.UNKNOWN -> LetterState.NOT_IN_WORD
                    else -> current
                }
            }
        }
        return KeyboardState(states)
    }
} 