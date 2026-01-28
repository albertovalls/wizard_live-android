package com.elitesports17.wizardlive.ui.navigation


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.elitesports17.wizardlive.ui.events.EventsScreen
import com.elitesports17.wizardlive.ui.home.HomeScreen
import com.elitesports17.wizardlive.ui.login.LoginScreen
import com.elitesports17.wizardlive.ui.profile.ProfileScreen
import com.elitesports17.wizardlive.ui.sports.SportsScreen
import com.elitesports17.wizardlive.ui.player.PlayerScreen
import com.elitesports17.wizardlive.ui.player.PlayerHolder
import com.elitesports17.wizardlive.ui.broadcast.BroadcastScreen
import com.elitesports17.wizardlive.ui.splash.SplashScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {

        composable(Screen.Splash.route) {
            SplashScreen { destination ->
                navController.navigate(destination) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(navController)
        }

        composable(Screen.Sports.route) {
            SportsScreen(navController)
        }

        composable(Screen.Events.route) {
            EventsScreen(navController)
        }

        composable(Screen.Broadcast.route) {
            BroadcastScreen(navController)
        }

        composable(Screen.Profile.route) {
            ProfileScreen(navController)
        }

        composable("player/{channelSlug}/{serial}") { backStackEntry ->
            val slug = backStackEntry.arguments?.getString("channelSlug")!!
            val serial = backStackEntry.arguments?.getString("serial")!!
            PlayerScreen(
                streamUrl = "https://livewizard.westeurope.cloudapp.azure.com/hls/$slug/index.m3u8",
                channelSlug = slug,
                serial = serial,
                onBack = {
                    PlayerHolder.player?.pause()
                    navController.popBackStack()
                }
            )
        }
    }
}