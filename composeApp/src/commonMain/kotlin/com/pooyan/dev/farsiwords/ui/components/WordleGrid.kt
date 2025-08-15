package com.pooyan.dev.farsiwords.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import com.pooyan.dev.farsiwords.domain.model.Game
import com.pooyan.dev.farsiwords.domain.model.LetterState
import com.pooyan.dev.farsiwords.design.GreenExact
import com.pooyan.dev.farsiwords.design.YellowPresent
import com.pooyan.dev.farsiwords.design.GreyAbsent
import com.pooyan.dev.farsiwords.design.GreyEmpty
import com.pooyan.dev.farsiwords.domain.model.Guess
import com.pooyan.dev.farsiwords.domain.model.Letter

@Composable
fun WordleGrid(
    game: Game,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.Center)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            repeat(game.guesses.size) { rowIndex ->
                WordleRow(
                    guess = game.guesses[rowIndex],
                    isActive = rowIndex == game.currentGuessIndex,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun WordleRow(
    guess: Guess,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        repeat(guess.letters.size) { letterIndex ->
            WordleTile(
                letter = guess.letters[letterIndex],
                isActive = isActive,
                modifier = Modifier.aspectRatio(1f).weight(1f)
            )
        }
    }
}

@Composable
private fun WordleTile(
    letter: Letter,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (letter.state) {
        LetterState.CORRECT -> GreenExact
        LetterState.WRONG_POSITION -> YellowPresent
        LetterState.NOT_IN_WORD -> GreyAbsent
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when (letter.state) {
        LetterState.UNKNOWN -> if (letter.char.isNotEmpty() || isActive) MaterialTheme.colorScheme.primary else GreyEmpty
        else -> backgroundColor
    }

    val textColor = when (letter.state) {
        LetterState.CORRECT -> MaterialTheme.colorScheme.onPrimary
        LetterState.WRONG_POSITION -> MaterialTheme.colorScheme.onSecondary
        LetterState.NOT_IN_WORD -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurface
    }

    val borderWidth = if (letter.state == LetterState.UNKNOWN && (letter.char.isNotEmpty() || isActive)) 2.dp else 1.dp

    Card(
        modifier = modifier.border(
            width = borderWidth,
            color = borderColor,
            shape = RoundedCornerShape(8.dp)
        ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (letter.state != LetterState.UNKNOWN) 4.dp else 0.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            AnimatedContent(
                targetState = letter.char,
                transitionSpec = {
                    (scaleIn(spring(stiffness = 200f)) + fadeIn(tween(200))).togetherWith(
                        scaleOut(tween(200)) + fadeOut(tween(200))
                    )
                },
                label = "letter_animation"
            ) { targetLetter ->
                Text(
                    text = targetLetter,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    color = textColor
                )
            }
        }
    }
}
