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
import com.pooyan.dev.farsiwords.presentation.WordVerificationViewModel
import com.pooyan.dev.farsiwords.data.initAndroidContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Initialize Android context for shared module
        initAndroidContext(this)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel = WordVerificationViewModel()
                    WordVerificationScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Preview
@Composable
fun WordVerificationPreview() {
    MaterialTheme {
        val viewModel = WordVerificationViewModel()
        WordVerificationScreen(viewModel = viewModel)
    }
}