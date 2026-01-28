package com.elitesports17.wizardlive.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.statusBars
import com.elitesports17.wizardlive.R
import androidx.navigation.NavHostController
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.elitesports17.wizardlive.ui.util.setAppLocale
import java.util.Locale
import androidx.compose.foundation.clickable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.produceState
import com.elitesports17.wizardlive.data.model.WizardChannel
import com.elitesports17.wizardlive.ui.util.UserSession
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.elitesports17.wizardlive.ui.player.PlayerHolder
import com.elitesports17.wizardlive.ui.chat.ViewersViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(navController: NavHostController) {

    val viewModel: HomeViewModel = viewModel()
    val wizardState by viewModel.wizardState.collectAsState()
    val isRefreshing = wizardState is WizardUiState.Loading
    val viewersViewModel: ViewersViewModel = viewModel()
    val viewersMap by viewersViewModel.viewers.collectAsState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            android.util.Log.d("WIZARD_UI", "🔄 Pull to refresh")
            viewModel.loadWizardChannels()
        }
    )
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current
    LaunchedEffect(wizardState) {
        android.util.Log.d("WIZARD_UI", "Estado UI = $wizardState")
    }
    LaunchedEffect(Unit) {
        // 1️⃣ Leer token de DataStore
        val token = UserSession.getToken(context)

        // 2️⃣ Guardarlo en memoria (Interceptor)
        UserSession.setCachedToken(token)

        android.util.Log.d(
            "WIZARD_UI",
            "🔐 Token listo en HomeScreen = ${!token.isNullOrEmpty()}"
        )

        // 3️⃣ AHORA sí: llamar a la API
        if (!token.isNullOrEmpty()) {
            viewModel.loadWizardChannels()
        }
    }



    val role by produceState<String>("viewer") {
        value = com.elitesports17.wizardlive.ui.util.UserSession
            .getRole(context) ?: "viewer"
    }


    val filteredPopular = remember(searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) popularItems
        else popularItems.filter { item ->
            item.title.lowercase().contains(q) ||
                    item.source.lowercase().contains(q)
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
                .background(Color.Black)
                .pullRefresh(pullRefreshState)
        ) {

            item {
                TrendingSection(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it }
                )
            }

            item {
                WizardSection(
                    state = wizardState,
                    navController = navController,
                    searchQuery = searchQuery
                )
            }

            item { SectionTitle(stringResource(R.string.popular)) }


            items(filteredPopular) { item ->
                LiveCard(item)
            }

            item { Spacer(modifier = Modifier.height(90.dp)) }
        }
    }
}

/* ---------- TOP HEADER ---------- */

@Composable
private fun TopHeader() {

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

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                "Wizard",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(Color.Red.copy(alpha = dotAlpha))
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
               "LIVE",
                color = Color.Red,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.weight(1f))

            LanguageSelectorHeader()


        }

        // 🔽 BORDER INFERIOR SUTIL
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Red.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}


@Composable
private fun LanguageSelectorHeader() {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val currentLang = Locale.getDefault().language
    val label = when (currentLang) {
        "es" -> "ES"
        "en" -> "EN"
        else -> "EN"
    }

    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Language,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = label,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
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


/* ---------- TRENDING ---------- */

@Composable
private fun TrendingSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔥", fontSize = 18.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.trending),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            SearchBarCompact(
                query = searchQuery,
                onQueryChange = onSearchQueryChange
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LiveCard(
            LiveItem(
                R.drawable.img_football,
                "UEFA Champions League · Real Madrid vs Man City",
                stringResource(R.string.football)
            )
        )
    }
}

@Composable
private fun SearchBarCompact(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        placeholder = {
            Text(
                text = stringResource(R.string.search),
                color = Color.Gray,
                fontSize = 13.sp
            )
        },
        leadingIcon = {
            Icon(
                Icons.Outlined.Search,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(18.dp)
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color(0xFF2A2A2A),
            unfocusedContainerColor = Color(0xFF2A2A2A),
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = Color.White
        ),
        shape = RoundedCornerShape(50),
        modifier = Modifier
            .height(50.dp)
            .width(170.dp)
    )
}




@Composable
private fun WizardSection(
    state: WizardUiState,
    navController: NavHostController,
    searchQuery: String
) {
    Column(modifier = Modifier.padding(16.dp)) {

        Text(
            text = "Wizard",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        when (state) {
            is WizardUiState.Loading -> {
                CircularProgressIndicator(color = Color(0xFFB983FF))
            }

            is WizardUiState.Empty -> {
                Text(
                    text = "No hay streams disponibles",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            is WizardUiState.Error -> {
                Text(
                    text = state.message,
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }

            is WizardUiState.Success -> {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val filteredChannels = remember(state.channels, searchQuery) {
                        val q = searchQuery.trim().lowercase()
                        if (q.isEmpty()) state.channels
                        else state.channels.filter { ch ->
                            ch.title.lowercase().contains(q) ||
                                    ch.channelSlug.lowercase().contains(q)
                        }
                    }

                    if (filteredChannels.isEmpty() && searchQuery.isNotBlank()) {
                        Text(
                            text = "No hay resultados para \"$searchQuery\"",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            filteredChannels.forEach { channel ->
                                WizardChannelCard(channel, navController)
                            }
                        }
                    }

                }
            }
        }
    }
}

@Composable
private fun WizardChannelCard(channel: WizardChannel, navController: NavHostController) {


    val context = LocalContext.current

    val streamUrl =
        "https://livewizard.westeurope.cloudapp.azure.com/hls/${channel.channelSlug}/index.m3u8"

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(streamUrl))
            prepare()
            playWhenReady = true
            volume = 0f // 🔇 preview mute
            repeatMode = ExoPlayer.REPEAT_MODE_ALL
        }
    }

    LaunchedEffect(exoPlayer) {
        PlayerHolder.player = exoPlayer
    }


    DisposableEffect(Unit) {
        onDispose {
            //exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
            .clickable {
                navController.navigate(
                    "player/${channel.channelSlug}/${channel.streamId}"
                )
            }
    ) {

        // 🎥 VIDEO PREVIEW
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = false
                }
            },
            modifier = Modifier.matchParentSize()
        )

        // 🌑 OVERLAY GRADIENT
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // 🔴 LIVE BADGE
        Row(
            modifier = Modifier
                .padding(10.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.Red)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "LIVE",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }


        // 📝 TEXTOS
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                text = channel.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = channel.channelSlug,
                color = Color.LightGray,
                fontSize = 12.sp
            )
        }
    }
}



/* ---------- SECTION TITLE ---------- */

@Composable
private fun SectionTitle(title: String) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = Icons.Outlined.TrendingUp,
            contentDescription = null,
            tint = Color(0xFFC27AFF),
            modifier = Modifier.size(20.dp)
        )

        Spacer(Modifier.width(8.dp))

        Text(
            title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


/* ---------- LIVE CARD ---------- */

@Composable
private fun LiveCard(item: LiveItem) {

    val isLocked = item.title.contains("NBA")

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 18.dp) // ⬅ más separación
            .clip(RoundedCornerShape(18.dp))
    ) {

        Image(
            painter = painterResource(item.image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
        )

        // 🌑 OVERLAY MÁS OSCURO
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.75f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        // 🔴 LIVE BADGE MEJORADO
        Row(
            modifier = Modifier
                .padding(10.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFD32F2F))
                .padding(horizontal = 12.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.live),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 🔒 LOCKED
        if (isLocked) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(text = stringResource(R.string.locked), color = Color.White, fontSize = 12.sp)
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
        ) {
            Text(
                item.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                item.source,
                color = Color.LightGray,
                fontSize = 12.sp
            )
        }
    }
}

/* ---------- BOTTOM BAR ---------- */



/* ---------- DATA ---------- */

data class LiveItem(
    val image: Int,
    val title: String,
    val source: String
)

val popularItems = listOf(
    LiveItem(R.drawable.img_tennis, "ATP Finals · Djokovic vs Alcaraz", "ATP TV"),
    LiveItem(R.drawable.img_nba, "NBA Match · Warriors vs LA", "NBA Live"),
    LiveItem(R.drawable.img_boxing, "Boxing Night · Main Event", "Fight TV")
)
