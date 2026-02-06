package com.elitesports17.wizardlive.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.elitesports17.wizardlive.R
import com.elitesports17.wizardlive.ui.navigation.Screen
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.SportsSoccer
private val ActiveColor = Color(0xFFC27AFF)

@Composable
fun BottomBar(
    navController: NavHostController,
    role: String
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Column {

        // 🔝 TOP BORDER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            ActiveColor.copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    )
                )
        )

        NavigationBar(
            containerColor = Color(0xFF1A1A1A),
            tonalElevation = 0.dp
        ) {

            NavigationBarItem(
                selected = currentRoute == Screen.Home.route,
                onClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                icon = {
                    Icon(
                        Icons.Outlined.Home,
                        contentDescription = stringResource(R.string.nav_home),
                        tint = if (currentRoute == Screen.Home.route)
                            ActiveColor else Color.Gray
                    )
                },
                label = {
                    Text(
                        text = stringResource(R.string.nav_home),
                        color = if (currentRoute == Screen.Home.route)
                            ActiveColor else Color.Gray
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )

            NavigationBarItem(
                selected = currentRoute == Screen.Sports.route,
                onClick = {
                    navController.navigate(Screen.Sports.route) {
                        launchSingleTop = true
                    }
                },
                icon = {
                    Icon(
                        Icons.Outlined.SportsSoccer,
                        contentDescription = stringResource(R.string.nav_sports),
                        tint = if (currentRoute == Screen.Sports.route)
                            ActiveColor else Color.Gray
                    )
                },
                label = {
                    Text(
                        text = stringResource(R.string.nav_sports),
                        color = if (currentRoute == Screen.Sports.route)
                            ActiveColor else Color.Gray
                    )

                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )

            NavigationBarItem(
                selected = currentRoute == Screen.Events.route,
                onClick = {
                    navController.navigate(Screen.Events.route) {
                        launchSingleTop = true
                    }
                },
                icon = {
                    Icon(
                        Icons.Outlined.Event,
                        contentDescription = stringResource(R.string.nav_events),
                        tint = if (currentRoute == Screen.Events.route)
                            ActiveColor else Color.Gray
                    )
                },
                label = {
                    Text(
                        text = stringResource(R.string.nav_events),
                        color = if (currentRoute == Screen.Events.route)
                            ActiveColor else Color.Gray
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )


                NavigationBarItem(
                    selected = currentRoute == Screen.Broadcast.route,
                    onClick = {
                        navController.navigate(Screen.Broadcast.route) {
                            launchSingleTop = true
                        }
                    },
                    icon = {
                        Image(
                            painter = painterResource(R.drawable.logo),
                            contentDescription = stringResource(R.string.nav_broadcast),
                            modifier = Modifier.size(22.dp),
                            colorFilter = ColorFilter.tint(
                                if (currentRoute == Screen.Broadcast.route)
                                    ActiveColor else Color.Gray
                            )
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.nav_broadcast),
                            color = if (currentRoute == Screen.Broadcast.route)
                                ActiveColor else Color.Gray
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent
                    )
                )



            NavigationBarItem(
                selected = currentRoute == Screen.Profile.route,
                onClick = {
                    navController.navigate(Screen.Profile.route) {
                        launchSingleTop = true
                    }
                },
                icon = {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = stringResource(R.string.nav_profile),

                                tint = if (currentRoute == Screen.Profile.route)
                            ActiveColor else Color.Gray
                    )
                },
                label = {
                    Text(
                        text = stringResource(R.string.nav_profile),
                        color = if (currentRoute == Screen.Profile.route)
                            ActiveColor else Color.Gray
                    )

                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )

            )
        }
    }
}
