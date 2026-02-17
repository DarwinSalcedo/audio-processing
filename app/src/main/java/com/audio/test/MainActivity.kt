package com.audio.test

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.audio.test.navigation.Screen
import com.audio.test.ui.screens.process.ProcessScreen
import com.audio.test.ui.screens.result.ResultScreen
import com.audio.test.ui.screens.setup.SetupScreen
import com.audio.test.ui.theme.AudioAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission Granted
        } else {
            // Permission Denied
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

        setContent {
            AudioAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Setup.route
                    ) {
                        composable(route = Screen.Setup.route) {
                            SetupScreen(
                                onStartPractice = { text ->
                                    val safeText = android.net.Uri.encode(text)
                                    navController.navigate("process/$safeText")
                                }
                            )
                        }

                        composable(
                            route = Screen.Process.route,
                            arguments = listOf(navArgument("text") { type = NavType.StringType })
                        ) {
                             // Arguments are handled by ViewModel via SavedStateHandle, so we don't need to extract them here manually
                             // unless we wanted to pass them to screen composable directly. 
                             // But ProcessViewModel gets it from SavedStateHandle.
                             // However, ProcessScreen needs the callback for navigation.
                             
                             // We also need to decode the text if we encoded it? 
                             // SavedStateHandle might give the raw string from the path.
                             // Navigation library usually decodes path arguments automatically.
                             
                            ProcessScreen(
                                onFinish = { sessionId ->
                                    navController.navigate("result/$sessionId") {
                                        popUpTo(Screen.Setup.route) { inclusive = false }
                                    }
                                }
                            )
                        }

                        composable(
                            route = Screen.Result.route,
                            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
                        ) {
                            ResultScreen(
                                onNavigateHome = {
                                    navController.navigate(Screen.Setup.route) {
                                        popUpTo(Screen.Setup.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
