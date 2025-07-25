package com.pooyan.dev.farsiwords.domain.repository

import com.pooyan.dev.farsiwords.domain.model.*

/**
 * Main repository interface for Farsi Wordle game
 * Handles Firebase integration, offline storage, and Bloom filter validation
 */
interface GameRepository {
    
    // === AUTHENTICATION ===
    suspend fun signInWithGoogle(): Result<UserProfile>
    suspend fun signInWithApple(): Result<UserProfile>
    suspend fun signInAnonymously(): Result<UserProfile>
    suspend fun signOut(): Result<Unit>
    suspend fun getCurrentUser(): UserProfile?
    
    // === WORD MANAGEMENT ===
    suspend fun getDailyWord(): Result<FarsiWord>
    suspend fun getRandomWord(difficulty: WordDifficulty? = null): Result<FarsiWord>
    suspend fun validateWord(word: String): Boolean // Uses Bloom filter for offline validation
    suspend fun validateWordOnline(word: String): Result<Boolean> // Cloud function fallback
    suspend fun getWordById(wordId: String): Result<FarsiWord>
    
    // === WORD PACKAGES ===
    suspend fun getAvailablePackages(): Result<List<WordPackage>>
    suspend fun purchasePackage(packageId: String): Result<PurchaseResult>
    suspend fun getOwnedPackages(): Result<List<WordPackage>>
    suspend fun downloadPackageWords(packageId: String): Result<Unit>
    
    // === USER PROGRESS ===
    suspend fun saveGameResult(game: Game): Result<Unit>
    suspend fun getUserStats(): Result<UserStats>
    suspend fun updateUserProfile(profile: UserProfile): Result<Unit>
    suspend fun addCoins(amount: Int, reason: String): Result<Unit>
    suspend fun spendCoins(amount: Int, reason: String): Result<Unit>
    
    // === DAILY CHALLENGES ===
    suspend fun getTodayChallenge(): Result<DailyChallenge>
    suspend fun completeDailyChallenge(game: Game): Result<DailyReward>
    suspend fun getChallengeHistory(): Result<List<DailyChallenge>>
    
    // === OFFLINE SUPPORT ===
    suspend fun syncOfflineData(): Result<Unit>
    suspend fun isOfflineMode(): Boolean
    suspend fun updateBloomFilter(): Result<Unit>
    
    // === IN-APP PURCHASES ===
    suspend fun purchaseCoins(amount: Int): Result<PurchaseResult>
    suspend fun restorePurchases(): Result<List<PurchaseResult>>
}

// Supporting data classes for repository
data class WordPackage(
    val id: String,
    val name: String,
    val description: String,
    val wordCount: Int,
    val difficulty: WordDifficulty,
    val price: Int, // in coins
    val isOwned: Boolean = false,
    val isDownloaded: Boolean = false
)

data class UserStats(
    val totalGames: Int,
    val winRate: Float,
    val averageAttempts: Float,
    val bestTime: Long,
    val currentStreak: Int,
    val maxStreak: Int,
    val wordsLearned: Int,
    val pointsThisWeek: Int,
    val rank: String
)

data class DailyReward(
    val coins: Int,
    val points: Int,
    val streakBonus: Int,
    val message: String
)

data class PurchaseResult(
    val success: Boolean,
    val transactionId: String?,
    val error: String?
) 