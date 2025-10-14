package com.audio.test.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.audio.test.navigation.Screen


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun SetupScreen(navController: NavController, viewModel: SetupViewModel) {
    // TODO: record permission

    SetupView(viewModel, navController)
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SetupView(
    viewModel: SetupViewModel,
    navController: NavController
) {
    val scrollState = rememberScrollState()
    Scaffold(
        Modifier
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

            Column {
                var inputText by remember { mutableStateOf("") }

                viewModel.addText(inputText)

                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = {
                        Text(
                            "Enter text to repeat or just press the button below to continue",
                            style = MaterialTheme.typography.titleSmall
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 10
                )
                Spacer(Modifier.height(22.dp))

                HistorySpeakingList()
            }


            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    navController.navigate(Screen.Process.route)
                }) {
                Text("Continue", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun HistorySpeakingList() {
    Box(Modifier.fillMaxSize()) {
        Column {
            Box {
                Text("Previous Speaking ", style = MaterialTheme.typography.titleMedium)
            }
            Box {
                Text("Practice 1", style = MaterialTheme.typography.titleSmall)
            }
            Box {
                Text("Practice 2", style = MaterialTheme.typography.titleSmall)
            }
            Box {
                Text("Practice 3", style = MaterialTheme.typography.titleSmall)
            }
            Box {
                Text("Practice 4", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
private fun previewSetupView() {
    SetupView(SetupViewModel(), NavController(LocalContext.current))

}