package com.audio.test.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.audio.test.navigation.Screen


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun SetupScreen(navController: NavController, viewModel: SetupViewModel) {
    // TODO: record permission

    val scrollState = rememberScrollState()
    Scaffold(Modifier
        .fillMaxSize()
        .padding(34.dp),
        topBar = {
            TopAppBar(title = {
                Text(
                    "Speaking Practice",
                    style = MaterialTheme.typography.titleLarge
                )
            })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(
                    state = scrollState,
                ),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var inputText by remember { mutableStateOf("") }

            viewModel.addText(inputText)

            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = {
                    Text(
                        "Enter/paste text to practice",
                        style = MaterialTheme.typography.titleSmall
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 10
            )

            Button(modifier = Modifier.fillMaxWidth(), enabled = inputText.isNotEmpty(),
                onClick = {
                    navController.navigate(Screen.Process.route)
                }) {
                Text("Process", style = MaterialTheme.typography.titleMedium)
            }

        }
    }
}