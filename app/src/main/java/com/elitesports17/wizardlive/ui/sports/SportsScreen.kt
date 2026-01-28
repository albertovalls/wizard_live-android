package com.elitesports17.wizardlive.ui.sports

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.clickable
import androidx.navigation.NavHostController
import com.elitesports17.wizardlive.R
import com.elitesports17.wizardlive.ui.home.BottomBar
import com.elitesports17.wizardlive.ui.util.setAppLocale
import java.util.Locale
import androidx.compose.ui.res.stringResource
@Composable
fun SportsScreen(
    navController: NavHostController
) {
    val context = LocalContext.current

    val role by produceState<String>("viewer") {
        value = com.elitesports17.wizardlive.ui.util.UserSession
            .getRole(context) ?: "viewer"
    }
    Scaffold(
        topBar = { TopHeader() }, // ✅ MISMO HEADER QUE HOME
        bottomBar = { BottomBar(navController, role) },
        containerColor = Color.Black
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        ) {

            /* ---------- TITLE ---------- */
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.categories),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.explore_sports),

                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            /* ---------- GRID ---------- */
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(sportsCategories) { category ->
                    CategoryCard(category)
                }
            }
        }
    }
}

/* ---------- TOP HEADER (IGUAL QUE HOME) ---------- */

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

/* ---------- LANGUAGE SELECTOR ---------- */

@Composable
private fun LanguageSelectorHeader() {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val currentLang = Locale.getDefault().language
    val label = if (currentLang == "es") "ES" else "EN"

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
                label,
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

/* ---------- CATEGORY CARD ---------- */

@Composable
private fun CategoryCard(item: SportsCategory) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {

        Image(
            painter = painterResource(item.image),
            contentDescription = stringResource(item.titleRes),

                    contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = stringResource(R.string.streams, item.streams),
                color = Color.White,
                fontSize = 11.sp
            )

        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                text = stringResource(item.titleRes),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                item.viewers,
                color = Color.LightGray,
                fontSize = 12.sp
            )
        }
    }
}

/* ---------- DATA ---------- */

data class SportsCategory(
    val titleRes: Int,
    val viewers: String,
    val streams: Int,
    val image: Int
)


val sportsCategories = listOf(
    SportsCategory(R.string.football, "123k", 12, R.drawable.cat_football),
    SportsCategory(R.string.basketball, "74k", 7, R.drawable.cat_basket),
    SportsCategory(R.string.tennis, "23k", 11, R.drawable.cat_tennis),
    SportsCategory(R.string.boxing, "13k", 6, R.drawable.cat_boxing),
    SportsCategory(R.string.volleyball, "14k", 5, R.drawable.cat_volleyball),
    SportsCategory(R.string.handball, "11k", 4, R.drawable.cat_handball),
    SportsCategory(R.string.swimming, "3k", 3, R.drawable.cat_swimming),
    SportsCategory(R.string.table_tennis, "1", 1, R.drawable.cat_pingpong)
)
