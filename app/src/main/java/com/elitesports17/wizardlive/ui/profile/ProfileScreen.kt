package com.elitesports17.wizardlive.ui.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.elitesports17.wizardlive.R
import com.elitesports17.wizardlive.ui.home.BottomBar
import com.elitesports17.wizardlive.ui.util.UserSession
import com.elitesports17.wizardlive.ui.util.setAppLocale
import kotlinx.coroutines.launch
import java.util.Locale
import com.elitesports17.wizardlive.data.remote.ApiService
import com.elitesports17.wizardlive.data.remote.RetrofitClient
import com.elitesports17.wizardlive.ui.profile.ProfileViewModelFactory
import coil.compose.AsyncImage
import com.elitesports17.wizardlive.data.model.ProfileMeResponse

/* ------------------------------------------------ */
/* ---------------- PROFILE SCREEN ---------------- */
/* ------------------------------------------------ */

@Composable
fun ProfileScreen(navController: NavHostController) {

    val context = LocalContext.current

    val api = remember {
        RetrofitClient.api   // 👈 usa TU singleton real de Retrofit
    }

    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(api)
    )

    val followersState by profileViewModel.followersState.collectAsState()
    val subscriptionsState by profileViewModel.subscriptionsState.collectAsState()
    val channelState by profileViewModel.channelState.collectAsState()
    val viewerProfileState by profileViewModel.viewerProfileState.collectAsState()
    // 🔥 FETCH FOLLOWERS (UNA SOLA VEZ)



    val channelName = when (channelState) {
        is ChannelUiState.Success ->
            (channelState as ChannelUiState.Success).data.channelSlug
        is ChannelUiState.Loading -> "—"
        is ChannelUiState.Error -> "Unknown"
    }

    val channelLogoUrl = when (channelState) {
        is ChannelUiState.Success ->
            (channelState as ChannelUiState.Success).data.logoUrl
        else -> null
    }


    val viewerData: ProfileMeResponse? = when (viewerProfileState) {
        is ViewerProfileUiState.Success ->
            (viewerProfileState as ViewerProfileUiState.Success).data
        else -> null
    }



    val role by produceState<String>("viewer") {
        value = com.elitesports17.wizardlive.ui.util.UserSession
            .getRole(context) ?: "viewer"
    }

    LaunchedEffect(role) {
        if (role == "viewer") {
            val token = UserSession.getToken(context)
            if (token != null) {
                profileViewModel.loadViewerProfile(token)
            }
        } else {
            val token = UserSession.getToken(context)
            if (token != null) {
                profileViewModel.loadFollowers(token)
                profileViewModel.loadSubscriptions(token)
                profileViewModel.loadMyChannel(token)
            }
        }
    }




    Scaffold(
        topBar = { TopHeader() },
        bottomBar = { BottomBar(navController, role) },
        containerColor = Color.Black
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            item {
                UserCard(
                    channelName = viewerData?.username ?: channelName,
                    logoUrl = viewerData?.logo_url ?: channelLogoUrl
                )
            }

            if (role != "viewer") {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        val followersCount = when (followersState) {
                            is FollowersUiState.Success ->
                                (followersState as FollowersUiState.Success)
                                    .data.count.toString()

                            is FollowersUiState.Loading -> "—"
                            is FollowersUiState.Error -> "0"
                        }

                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.People,
                            title = stringResource(R.string.followers),
                            value = followersCount
                        )

                        val subscriptionsCount = when (subscriptionsState) {
                            is SubscriptionsUiState.Success ->
                                (subscriptionsState as SubscriptionsUiState.Success).count.toString()

                            is SubscriptionsUiState.Loading -> "—"
                            is SubscriptionsUiState.Error -> "0"
                        }
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Outlined.PersonAdd,
                            title = stringResource(R.string.subscriptions),
                            value = subscriptionsCount
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SubscriptionsCard(modifier = Modifier.weight(1f))
                    BioCard(modifier = Modifier.weight(1f))
                }
            }

            item { ActionButtons(navController) }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

/* ------------------------------------------------ */
/* -------------------- HEADER -------------------- */
/* ------------------------------------------------ */

@Composable
internal fun TopHeader() {

    val infiniteTransition = rememberInfiniteTransition(label = "liveDot")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A1A))
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(38.dp)
            )

            Spacer(Modifier.width(10.dp))

            Text("Wizard", color = Color.White, fontWeight = FontWeight.Bold)

            Spacer(Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(Color.Red.copy(alpha = dotAlpha))
            )

            Spacer(Modifier.width(6.dp))

            Text(
                "LIVE",
                color = Color.Red,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.weight(1f))

            LanguageSelectorHeader()
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, Color.Red.copy(0.4f), Color.Transparent)
                    )
                )
        )
    }
}

/* ------------------------------------------------ */
/* --------------- LANGUAGE SELECTOR -------------- */
/* ------------------------------------------------ */

@Composable
private fun LanguageSelectorHeader() {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val lang = Locale.getDefault().language
    val label = if (lang == "es") "ES" else "EN"

    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(Icons.Outlined.Language, null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, color = Color.White, fontSize = 13.sp)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Español") },
                onClick = {
                    expanded = false
                    setAppLocale(context, "es")
                }
            )
            DropdownMenuItem(
                text = { Text("English") },
                onClick = {
                    expanded = false
                    setAppLocale(context, "en")
                }
            )
        }
    }
}

/* ------------------------------------------------ */
/* ------------------ USER CARD ------------------- */
/* ------------------------------------------------ */

@Composable
private fun UserCard(
    channelName: String,
    logoUrl: String?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))
                )
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = logoUrl,
                    contentDescription = "Channel logo",
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape),
                    placeholder = painterResource(R.drawable.avatar_placeholder),
                    error = painterResource(R.drawable.avatar_placeholder),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.width(14.dp))

                Column {
                    Text(
                        channelName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                    Text(
                        "@$channelName",
                        color = Color.White.copy(0.85f),
                        fontSize = 14.sp
                    )

                }
            }
        }
    }
}

/* ------------------------------------------------ */
/* ------------------- STAT CARD ------------------ */
/* ------------------------------------------------ */

@Composable
private fun StatCard(
    modifier: Modifier,
    icon: ImageVector,
    title: String,
    value: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFF3A3A3A)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Color(0xFFC27AFF), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(title, color = Color.Gray, fontSize = 13.sp)
            }
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        }
    }
}

/* ------------------------------------------------ */
/* ---------------- OTHER COMPOSABLES ------------- */
/* ------------------------------------------------ */

@Composable private fun SubscriptionsCard(modifier: Modifier) {}
@Composable private fun BioCard(modifier: Modifier) {}

@Composable
private fun ActionButtons(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lang = Locale.getDefault().language
    val logoutText = if (lang == "es") "Cerrar sesión" else "Log out"

    Button(
        onClick = {
            scope.launch {
                UserSession.clearSession(context)
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFD32F2F),
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        )
    ) {
        Icon(
            imageVector = Icons.Outlined.Logout,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = logoutText,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
    }


}
