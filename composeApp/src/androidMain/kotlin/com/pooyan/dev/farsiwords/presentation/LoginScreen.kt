package com.pooyan.dev.farsiwords.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pooyan.dev.farsiwords.presentation.auth.AuthViewModel
import com.pooyan.dev.farsiwords.domain.auth.AuthState
import org.koin.androidx.compose.get

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = get()
) {
    val state by viewModel.authState.collectAsState()

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Sign in to continue",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { /* TODO: trigger Google flow, then viewModel.signInWithGoogle(token) */ }, enabled = state !is AuthState.Loading, modifier = Modifier.fillMaxWidth()) {
            Text("Continue with Google")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { /* TODO: trigger Apple flow */ }, enabled = state !is AuthState.Loading, modifier = Modifier.fillMaxWidth()) {
            Text("Continue with Apple")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { viewModel.signInAnonymously() }, enabled = state !is AuthState.Loading, modifier = Modifier.fillMaxWidth()) {
            Text("Skip for now")
        }
    }
}