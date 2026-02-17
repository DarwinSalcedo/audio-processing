package com.audio.test.ui.screens.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.audio.test.ui.components.AppButton
import com.audio.test.ui.components.AppTextField
import com.audio.test.ui.components.SessionCard

@Composable
fun SetupScreen(
    viewModel: SetupViewModel = hiltViewModel(),
    onStartPractice: (String) -> Unit
) {
    val inputText by viewModel.inputText.collectAsState()
    val sessions by viewModel.sessions.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Let's Practice!",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            AppTextField(
                value = inputText,
                onValueChange = viewModel::onInputTextChanged,
                label = "Paste text to practice here...",
                minLines = 5,
                maxLines = 10
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AppButton(
                text = "Start Practice",
                onClick = { onStartPractice(inputText.ifBlank { "Sample text for practice." }) },
                enabled = true // Allowing empty text for quick practice as requested
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Previous Sessions",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(sessions) { session ->
                    SessionCard(
                        session = session,
                        onClick = { /* Navigate to details if needed */ }
                    )
                }
            }
        }
    }
}
