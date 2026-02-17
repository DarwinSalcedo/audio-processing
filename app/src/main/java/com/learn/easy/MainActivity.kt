package com.learn.easy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.learn.easy.navigation.Screen
import com.learn.easy.ui.screens.history.HistoryScreen
import com.learn.easy.ui.screens.permission.PermissionScreen
import com.learn.easy.ui.screens.process.ProcessScreen
import com.learn.easy.ui.screens.result.ResultScreen
import com.learn.easy.ui.screens.setup.SetupScreen
import com.learn.easy.ui.theme.AudioAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                        composable(route = Screen.Permission.route) {
                            PermissionScreen(
                                onPermissionGranted = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(route = Screen.Setup.route) {
                            SetupScreen(
                                onStartPractice = { text ->
                                    val safeText = android.net.Uri.encode(text)
                                    navController.navigate("process/$safeText")
                                },
                                onNavigateToHistory = {
                                    navController.navigate(Screen.History.route)
                                },
                                onNavigateToPermission = {
                                    navController.navigate(Screen.Permission.route)
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

                        composable(route = Screen.History.route) {
                            HistoryScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onSessionClick = { sessionId ->
                                    navController.navigate(Screen.Result.createRoute(sessionId))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
