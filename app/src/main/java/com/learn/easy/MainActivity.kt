package com.learn.easy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.learn.easy.navigation.Screen
import com.learn.easy.ui.screens.history.HistoryScreen
import com.learn.easy.ui.screens.home.HomeScreen
import com.learn.easy.ui.screens.justtalk.JustTalkScreen
import com.learn.easy.ui.screens.onboarding.OnboardingScreen
import com.learn.easy.ui.screens.random.RandomPhrasesScreen
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
                    val mainViewModel: MainViewModel =
                        hiltViewModel()
                    val startDestination by mainViewModel.startDestination.collectAsState(initial = null)

                    if (startDestination == null) {
                        // Loading Screen
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        NavHost(
                            navController = navController,
                            startDestination = startDestination!!
                        ) {
                            composable(route = Screen.Onboarding.route) {
                                OnboardingScreen(
                                    onFinish = {
                                        navController.navigate(Screen.Home.route) {
                                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                                        }
                                    }
                                )
                            }
                            
                            composable(route = Screen.Home.route) {
                                HomeScreen(
                                    onNavigateToShadowing = { navController.navigate(Screen.Setup.route) },
                                    onNavigateToJustTalk = { navController.navigate(Screen.JustTalk.route) },
                                    onNavigateToRandomPhrases = { navController.navigate(Screen.RandomPhrases.route) }
                                )
                            }

                            composable(route = Screen.JustTalk.route) {
                                JustTalkScreen(
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            composable(route = Screen.RandomPhrases.route) {
                                RandomPhrasesScreen(
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
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
                                arguments = listOf(navArgument("text") {
                                    type = NavType.StringType
                                })
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
                                arguments = listOf(navArgument("sessionId") {
                                    type = NavType.LongType
                                })
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
}

