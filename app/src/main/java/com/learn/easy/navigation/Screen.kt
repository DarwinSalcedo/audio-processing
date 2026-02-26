package com.learn.easy.navigation

sealed class Screen(val route: String) {
    object Permission : Screen("permission")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object JustTalk : Screen("just_talk")
    object RandomPhrases : Screen("random_phrases")
    object Setup : Screen("setup")
    object Process : Screen("process/{text}") {
        fun createRoute(text: String): String {
            // Encode the text to handle special characters if necessary.
            // For now, assuming standard text. Using Base64 could be better for long text.
            return "process/$text"
        }
    }
    object Result : Screen("result/{sessionId}") {
        fun createRoute(sessionId: Long): String {
            return "result/$sessionId"
        }
    }
    object History : Screen("history")
}