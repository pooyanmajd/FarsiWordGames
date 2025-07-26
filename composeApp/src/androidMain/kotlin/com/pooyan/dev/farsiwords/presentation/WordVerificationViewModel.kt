package com.pooyan.dev.farsiwords.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pooyan.dev.farsiwords.presentation.WordVerificationLogic
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android-specific ViewModel that wraps shared business logic
 * - Uses AndroidX ViewModel for proper Android lifecycle management
 * - Delegates business logic to shared WordVerificationLogic
 * - Uses Koin for dependency injection
 */
class WordVerificationViewModel : ViewModel(), KoinComponent {
    
    // Inject shared business logic
    private val logic: WordVerificationLogic by inject()
    
    // Expose state from shared logic
    val uiState: StateFlow<WordVerificationState> = logic.uiState
    
    init {
        Napier.d("Android WordVerificationViewModel initialized")
    }
    
    fun verifyWord(word: String) {
        logic.verifyWord(word)
    }
    
    fun clearHistory() {
        logic.clearHistory()
    }
    
    fun testCommonWords() {
        logic.testCommonWords()
    }
    
    override fun onCleared() {
        super.onCleared()
        logic.onCleared()
        Napier.d("Android WordVerificationViewModel cleared")
    }
} 