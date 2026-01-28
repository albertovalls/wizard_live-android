package com.elitesports17.wizardlive.ui.login

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun LoginAnimatedBackground() {

    val infiniteTransition = rememberInfiniteTransition(label = "bg")

    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )

    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetX"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0E0E10),
                        Color(0xFF1A0F2E),
                        Color(0xFF4A00E0),
                        Color(0xFF0E0E10)
                    ),
                    start = androidx.compose.ui.geometry.Offset(offsetX, offsetY),
                    end = androidx.compose.ui.geometry.Offset(
                        offsetX + 500f,
                        offsetY + 700f
                    )
                )
            )
    )
}
