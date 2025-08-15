package com.pooyan.dev.farsiwords.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pooyan.dev.farsiwords.domain.model.GameState
import com.pooyan.dev.farsiwords.ui.components.WordleGrid
import com.pooyan.dev.farsiwords.ui.components.WordleKeyboard
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

/**
 * Android-specific UI using Compose
 * - Uses shared WordVerificationViewModel via Koin injection
 * - Platform-specific UI implementation
 * - Follows Material 3 design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordVerificationScreen(
    modifier: Modifier = Modifier,
    viewModel: WordVerificationViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val game by viewModel.gameState.collectAsStateWithLifecycle()

    // Shake animation for invalid guess
    var shakeOffset by remember { mutableStateOf(0f) }
    // Trigger shake if needed (e.g., on invalid submit - assume ViewModel sets a flag, or derive)
    val shouldShake by remember {
        derivedStateOf {
            // Trigger on invalid submit (e.g., check if current guess is complete but not evaluated - adapt as needed)
            game.guesses[game.currentGuessIndex].isComplete && game.gameState == GameState.PLAYING  // Placeholder
        }
    }

    LaunchedEffect(shouldShake) {
        if (shouldShake) {
            // Simple shake: left-right
            // Animate to 10, then -10, then 0
            // Use a loop or keyframe, but keep simple
            // For now, set shakeOffset = 10f, delay, -10f, delay, 0f
            shakeOffset = 10f
            delay(50)
            shakeOffset = -10f
            delay(50)
            shakeOffset = 0f
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Game Status
        Text(
            text = when (game.gameState) {
                GameState.WON -> "You Won!"
                GameState.LOST -> "Game Over! Word was ${game.targetWord.word}"
                else -> "Guess the Word"
            },
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Grid with shake
        WordleGrid(
            game = game,
            modifier = Modifier
                .weight(1f)
                .offset(x = shakeOffset.dp)  // Fixed: use offset instead of translate
        )

        // Keyboard
        WordleKeyboard(
            keyboardState = game.keyboardState,
            onLetterPressed = viewModel::addLetter,
            onDeletePressed = viewModel::removeLetter,
            onEnterPressed = viewModel::submitGuess,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun WordResultCard(
    result: WordVerificationResult,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (result.isValid) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = result.word,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (result.isValid) "✅ Valid" else "❌ Invalid",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = if (result.isValid) {
                    "Word might be valid (0.1% chance of false positive)"
                } else {
                    "Word not found in dictionary"
                },
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Verified: ${formatTimestamp(result.timestamp)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 1000 -> "Just now"
        diff < 60000 -> "${diff / 1000}s ago"
        diff < 3600000 -> "${diff / 60000}m ago"
        else -> "${diff / 3600000}h ago"
    }
} 