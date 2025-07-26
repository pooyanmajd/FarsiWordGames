package com.pooyan.dev.farsiwords

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pooyan.dev.farsiwords.presentation.WordVerificationScreen
import io.github.aakira.napier.Napier

/**
 * Main Android Activity for Persepolis Wordle
 * Uses Koin for dependency injection and modern Compose setup
 */
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        Napier.i("🏛️ MainActivity created")
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WordVerificationApp()
                }
            }
        }
    }
}

@Composable
private fun WordVerificationApp() {
    // WordVerificationScreen handles its own ViewModel injection
    WordVerificationScreen(
        modifier = Modifier.fillMaxSize()
    )
}

@Preview(showBackground = true)
@Composable
private fun WordVerificationAppPreview() {
    MaterialTheme {
        // Note: Preview won't work with Koin injection
        // Use a mock setup for previews if needed
    }
}