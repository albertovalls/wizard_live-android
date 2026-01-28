package com.elitesports17.wizardlive.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Sports : Screen("sports")
    object Profile : Screen("profile")
    object Events : Screen("events")
    object Broadcast : Screen("broadcast")
    object Splash : Screen("splash")
}


