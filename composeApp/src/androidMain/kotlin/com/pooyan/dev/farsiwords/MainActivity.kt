package com.pooyan.dev.farsiwords

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pooyan.dev.farsiwords.domain.auth.AuthState.Authenticated
import com.pooyan.dev.farsiwords.presentation.WordVerificationScreen
import com.pooyan.dev.farsiwords.presentation.LoginScreen
import com.pooyan.dev.farsiwords.presentation.SplashScreen
import com.pooyan.dev.farsiwords.presentation.auth.AuthViewModel
import io.github.aakira.napier.Napier
import org.koin.compose.koinInject

/**
 * Main Android Activity for Persepolis Wordle
 * Uses Koin for dependency injection and modern Compose setup
 */
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

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
    val authViewModel: AuthViewModel = koinInject()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    // Simple gate: show splash until initialized by Application
    val isReady = com.pooyan.dev.farsiwords.data.WordChecker.isReady()
    if (!isReady) {
        SplashScreen(onNavigateToWordVerification = {

        })
        return
    }

    if (authState is Authenticated) {
        WordVerificationScreen(modifier = Modifier.fillMaxSize())
    } else {
        LoginScreen(modifier = Modifier.fillMaxSize())
    }
}

@Preview(showBackground = true)
@Composable
private fun WordVerificationAppPreview() {
    MaterialTheme {
        // Note: Preview won't work with Koin injection
        // Use a mock setup for previews if needed
    }
}