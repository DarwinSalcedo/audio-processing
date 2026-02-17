package com.learn.easy.ui.screens.setup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.learn.easy.R
import com.learn.easy.ui.components.AppButton
import com.learn.easy.ui.components.AppTextField

@Composable
fun SetupScreen(
    viewModel: SetupViewModel = hiltViewModel(),
    onStartPractice: (String) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToPermission: () -> Unit,
) {
    val inputText by viewModel.inputText.collectAsState()

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
                text = stringResource(R.string.setup_title),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            AppTextField(
                value = inputText,
                onValueChange = viewModel::onInputTextChanged,
                label = stringResource(R.string.setup_input_label),
                modifier = Modifier.weight(1f),
                minLines = 5,
                maxLines = 100
            )

            Spacer(modifier = Modifier.height(16.dp))
            val blankText = stringResource(R.string.setup_sample_text)
            val context = androidx.compose.ui.platform.LocalContext.current
            AppButton(
                text = stringResource(R.string.setup_start_button),
                onClick = {
                    if (androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.RECORD_AUDIO
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        onStartPractice(inputText.ifBlank { blankText })
                    } else {
                        onNavigateToPermission()
                    }
                },
                enabled = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onNavigateToHistory,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = stringResource(R.string.setup_history_button),
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = TextDecoration.Underline,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}