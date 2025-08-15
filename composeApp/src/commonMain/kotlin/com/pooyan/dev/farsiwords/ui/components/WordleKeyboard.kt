package com.pooyan.dev.farsiwords.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import com.pooyan.dev.farsiwords.domain.model.KeyboardState
import com.pooyan.dev.farsiwords.domain.model.LetterState
import androidx.compose.ui.graphics.vector.ImageVector

// Farsi keyboard layout borrowed from reference
private val farsiLayout = listOf(
    listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ"),
    listOf("ح", "ج", "چ", "ش", "س", "ی", "ب", "ل", "ا"),
    listOf("ت", "ن", "م", "ک", "گ", "ظ", "ط", "ز", "ر"),
    listOf("ذ", "د", "پ", "و", "ژ")
)

@Composable
fun WordleKeyboard(
    keyboardState: KeyboardState,
    onLetterPressed: (String) -> Unit,
    onDeletePressed: () -> Unit,
    onEnterPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            farsiLayout.forEachIndexed { index, row ->
                if (index < 3) {
                    KeyboardRow(row, keyboardState, onLetterPressed)
                } else {
                    KeyboardActionRow(row, keyboardState, onLetterPressed, onDeletePressed, onEnterPressed)
                }
            }
        }
    }
}

@Composable
private fun KeyboardRow(
    keys: List<String>,
    keyboardState: KeyboardState,
    onLetterPressed: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        keys.forEach { key ->
            LetterKey(
                letter = key,
                state = keyboardState.letterStates[key] ?: LetterState.UNKNOWN,
                onClick = { onLetterPressed(key) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun KeyboardActionRow(
    keys: List<String>,
    keyboardState: KeyboardState,
    onLetterPressed: (String) -> Unit,
    onDeletePressed: () -> Unit,
    onEnterPressed: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ActionKey(onClick = onEnterPressed, icon = Icons.Default.Send, contentDescription = "Enter", modifier = Modifier.weight(1.5f))
        keys.forEach { key ->
            LetterKey(
                letter = key,
                state = keyboardState.letterStates[key] ?: LetterState.UNKNOWN,
                onClick = { onLetterPressed(key) },
                modifier = Modifier.weight(1f)
            )
        }
        ActionKey(onClick = onDeletePressed, icon = Icons.Default.ArrowBack, contentDescription = "Delete", modifier = Modifier.weight(1.5f))
    }
}

@Composable
private fun LetterKey(
    letter: String,
    state: LetterState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.92f else 1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium))
    val background by animateColorAsState(
        when (state) {
            LetterState.CORRECT -> MaterialTheme.colorScheme.primary
            LetterState.WRONG_POSITION -> MaterialTheme.colorScheme.secondary
            LetterState.NOT_IN_WORD -> MaterialTheme.colorScheme.surfaceVariant
            else -> MaterialTheme.colorScheme.surface
        },
        tween(400) // Enhanced duration for smoothness
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(150)
            isPressed = false
        }
    }

    Card(
        modifier = modifier
            .height(48.dp)
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = letter,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ActionKey(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.92f else 1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium))

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(150)
            isPressed = false
        }
    }

    Card(
        modifier = modifier
            .height(48.dp)
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = contentDescription)
        }
    }
}
