package com.elitesports17.wizardlive.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.elitesports17.wizardlive.R
import com.elitesports17.wizardlive.ui.home.BottomBar
import com.elitesports17.wizardlive.ui.profile.TopHeader

@Composable
fun EventsScreen(
    navController: NavHostController
) {
    val context = LocalContext.current

    val role by produceState<String>("viewer") {
        value = com.elitesports17.wizardlive.ui.util.UserSession
            .getRole(context) ?: "viewer"
    }

    // Filtros (strings)
    val filters = listOf(
        stringResource(R.string.events_filter_all),
        stringResource(R.string.events_filter_premieres),
        stringResource(R.string.events_filter_watch_parties),
        stringResource(R.string.events_filter_qa),
        stringResource(R.string.events_filter_drops),
        stringResource(R.string.events_filter_community)
    )

    var selectedFilterIndex by remember { mutableIntStateOf(0) }

    // Mock data (sin deportes): cámbialo por tu API
    val liveNow = listOf(
        EventUi("LIVE", "Creator Q&A (Live)", "Join chat and ask questions"),
        EventUi("LIVE", "Studio Session", "Behind the scenes · Live discussion")
    )
    val upcoming = listOf(
        EventUi("SOON", "New Feature Premiere", "Tomorrow · 20:00"),
        EventUi("SOON", "Community Watch Party", "Fri · 22:30")
    )
    val featured = listOf(
        EventUi("FEATURED", "Drops Weekend", "Unlock rewards by watching"),
        EventUi("FEATURED", "Top Clips Premiere", "Best moments curated by the community")
    )

    val hasAnyEvents = liveNow.isNotEmpty() || upcoming.isNotEmpty() || featured.isNotEmpty()

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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            item {
                Text(
                    text = stringResource(R.string.events_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Chips con estado real
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    filters.forEachIndexed { index, label ->
                        FilterChip(
                            selected = selectedFilterIndex == index,
                            onClick = { selectedFilterIndex = index },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF7C3AED),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF1A1A1A),
                                labelColor = Color.White
                            ),
                            border = null
                        )
                    }
                }
            }

            // (Opcional) aquí luego aplicarías el filtro real según selectedFilterIndex

            if (liveNow.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = stringResource(R.string.events_section_live_title),
                        subtitle = stringResource(R.string.events_section_live_subtitle),
                        badgeText = stringResource(R.string.events_badge_live)
                    )
                }
                item { ItemsBlock(liveNow) }
            }

            if (upcoming.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = stringResource(R.string.events_section_upcoming_title),
                        subtitle = stringResource(R.string.events_section_upcoming_subtitle),
                        badgeText = stringResource(R.string.events_badge_soon)
                    )
                }
                item { ItemsBlock(upcoming) }
            }

            if (featured.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = stringResource(R.string.events_section_featured_title),
                        subtitle = stringResource(R.string.events_section_featured_subtitle),
                        badgeText = stringResource(R.string.events_badge_hot)
                    )
                }
                item { ItemsBlock(featured) }
            }

            if (!hasAnyEvents) {
                item {
                    Text(
                        text = stringResource(R.string.events_empty_message),
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    badgeText: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Badge(
                containerColor = Color(0xFF7C3AED),
                contentColor = Color.White
            ) {
                Text(
                    text = badgeText,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            color = Color(0xFF9CA3AF),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ItemsBlock(list: List<EventUi>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        list.forEach { event ->
            EventCard(event)
        }
    }
}

@Composable
private fun EventCard(event: EventUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 92.dp, height = 56.dp)
                    .background(Color(0xFF1F1F1F), RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                AssistChip(
                    onClick = {},
                    label = { Text(event.tag, style = MaterialTheme.typography.labelSmall) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFF7C3AED),
                        labelColor = Color.White
                    ),
                    border = null
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = event.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.subtitle,
                    color = Color(0xFF9CA3AF),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = { /* TODO open event */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.events_action_view), color = Color.White)
            }
        }
    }
}

private data class EventUi(
    val tag: String,
    val title: String,
    val subtitle: String
)
