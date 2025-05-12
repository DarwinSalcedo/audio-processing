package com.audio.test.navigation

sealed class Screen(val route: String) {
    object Setup: Screen("setup_screen")
    object Process: Screen("process_screen")
}