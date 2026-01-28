package com.elitesports17.wizardlive.ui.broadcast

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.elitesports17.wizardlive.R
import com.elitesports17.wizardlive.ui.home.BottomBar
import com.elitesports17.wizardlive.ui.profile.TopHeader
import com.elitesports17.wizardlive.ui.util.UserSession

private val WizardPurple = Color(0xFF6A38EF)

@Composable
fun BroadcastScreen(
    navController: NavHostController
) {
    val context = LocalContext.current

    val role by produceState("viewer") {
        value = UserSession.getRole(context) ?: "viewer"
    }

    val buyUrl = "https://tu-web.com/comprar-wizardcam" // <- tu URL real

    Scaffold(
        topBar = { TopHeader() },
        bottomBar = { BottomBar(navController, role) },
        containerColor = Color.Black
    ) { padding ->

        val background = Brush.verticalGradient(
            colors = listOf(Color(0xFF000000), Color(0xFF0A0A0A), Color(0xFF000000))
        )

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(background)
                .verticalScroll(scrollState) // ✅ scroll
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Header()

            SectionLabel(text = stringResource(R.string.broadcast_section_have_cam))

            WizardCamCard(
                onOpenWifiSettings = {
                    context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                }
            )

            SectionLabel(text = stringResource(R.string.broadcast_section_no_cam))

            NoWizardCamCard(
                onBuyWizardCam = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(buyUrl)))
                }
            )

            Text(
                text = stringResource(R.string.broadcast_tip),
                color = Color(0xFF8E8E8E),
                style = MaterialTheme.typography.bodySmall
            )

            // ✅ un pelín de aire para que no quede pegado al BottomBar al final del scroll
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Header() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Color(0xFF151515)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Videocam,
                contentDescription = stringResource(R.string.broadcast_icon_description),
                tint = Color.Red,
                modifier = Modifier.size(28.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.broadcast_title),
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.broadcast_subtitle),
                color = Color(0xFFB0B0B0),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    Spacer(Modifier.height(6.dp))
    Divider(color = Color(0xFF1D1D1D))
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = Color(0xFFDDDDDD),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun WizardCamCard(
    onOpenWifiSettings: () -> Unit
) {
    CardShell(
        icon = Icons.Filled.Wifi,
        iconDescription = stringResource(R.string.broadcast_wifi_icon_description),
        title = stringResource(R.string.broadcast_have_cam_title),
        subtitle = stringResource(R.string.broadcast_have_cam_subtitle)
    ) {
        Steps(
            steps = listOf(
                stringResource(R.string.broadcast_step_cam_power_on),
                stringResource(R.string.broadcast_step_connect_wifi),
                stringResource(R.string.broadcast_step_return_app)
            )
        )

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = onOpenWifiSettings,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = WizardPurple,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 2.dp
            )
        ) {
            Text(stringResource(R.string.broadcast_open_wifi_settings))
        }
    }
}

@Composable
private fun NoWizardCamCard(
    onBuyWizardCam: () -> Unit
) {
    CardShell(
        icon = Icons.Filled.ShoppingCart,
        iconDescription = stringResource(R.string.broadcast_shop_icon_description),
        title = stringResource(R.string.broadcast_no_cam_title),
        subtitle = stringResource(R.string.broadcast_no_cam_subtitle)
    ) {
        Surface(
            color = Color(0xFF141414),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFF242424))
        ) {
            Text(
                text = stringResource(R.string.broadcast_no_cam_body),
                modifier = Modifier.padding(12.dp),
                color = Color(0xFFBDBDBD),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(10.dp))

        FilledTonalButton(
            onClick = onBuyWizardCam,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = Color(0xFF1A1A1A),
                contentColor = Color.White
            )
        ) {
            Text(stringResource(R.string.broadcast_buy_wizardcam))
        }
    }
}

@Composable
private fun CardShell(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconDescription: String,
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F10)),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, Color(0xFF242424)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF171717)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = iconDescription,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = subtitle,
                        color = Color(0xFFBDBDBD),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            content()
        }
    }
}

@Composable
private fun Steps(steps: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        steps.forEachIndexed { idx, text ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF202020)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (idx + 1).toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = text,
                    color = Color(0xFFBDBDBD),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}