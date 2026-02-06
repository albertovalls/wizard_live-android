package com.elitesports17.wizardlive.ui.broadcast

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.elitesports17.wizardlive.R
import com.elitesports17.wizardlive.data.model.BroadcastViewModel
import com.elitesports17.wizardlive.data.model.StreamingNotification
import kotlinx.coroutines.delay
import kotlin.math.max

private val WizardPurple = Color(0xFF6A38EF)
private val LiveRed = Color(0xFFFF3B30)
private val CardBg = Color(0xFF0F0F10)
private val Stroke = Color(0xFF242424)
private val Muted = Color(0xFFBDBDBD)

// TODO: pon la URL final cuando la tengáis
private const val BUY_WIZCAM_URL = "https://example.com"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BroadcastScreen(navController: NavHostController) {
    val vm: BroadcastViewModel = viewModel()
    val ui by vm.ui.collectAsState()
    val context = LocalContext.current

    // ✅ NUEVO: estado del título del stream
    var streamTitle by rememberSaveable { mutableStateOf("") }

    // Permiso notificaciones (Android 13+)
    val notifPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* no hace falta nada */ }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val background = Brush.verticalGradient(
        colors = listOf(Color(0xFF060607), Color(0xFF0A0A0B), Color(0xFF060607))
    )

    // Live timer
    val isLive = ui.status?.status == "6"
    LaunchedEffect(isLive) {
        if (isLive) StreamingNotification.showStreamingOn(context)
        else StreamingNotification.showStreamingOff(context)
    }

    var liveStartMs by remember { mutableStateOf(0L) }
    var elapsedSec by remember { mutableStateOf(0L) }

    LaunchedEffect(isLive) {
        if (isLive) {
            if (liveStartMs == 0L) liveStartMs = System.currentTimeMillis()
            while (isLive) {
                elapsedSec = (System.currentTimeMillis() - liveStartMs) / 1000L
                delay(1000)
            }
        } else {
            liveStartMs = 0L
            elapsedSec = 0L
        }
    }

    var showPreviewDialog by rememberSaveable { mutableStateOf(false) }
    var playerError by remember { mutableStateOf<String?>(null) }

    // Dialog aviso preview
    if (showPreviewDialog) {
        AlertDialog(
            onDismissRequest = { if (!ui.previewBusy) showPreviewDialog = false },
            icon = { Icon(Icons.Filled.Warning, contentDescription = null, tint = Color(0xFFFFB74D)) },
            title = { Text(stringResource(R.string.perf_warning_title)) },
            text = { Text(stringResource(R.string.perf_warning_body)) },
            confirmButton = {
                TextButton(
                    enabled = !ui.previewBusy,
                    onClick = {
                        playerError = null
                        vm.ensurePreview()
                    }
                ) {
                    if (ui.previewBusy) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.perf_warning_enabling))
                    } else {
                        Text(stringResource(R.string.perf_warning_confirm))
                    }
                }
            },
            dismissButton = {
                TextButton(enabled = !ui.previewBusy, onClick = { showPreviewDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    LaunchedEffect(ui.previewActive) {
        if (ui.previewActive) showPreviewDialog = false
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF0B0B0C), Color(0xFF121214), Color(0xFF0B0B0C))
                        )
                    )
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.broadcast_title),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.nav_back),
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            HeaderPro()

            // ✅ OFFLINE: 2 cards separadas + botón Wi-Fi
            if (!ui.connectedToWizardCam) {
                OfflineWizardCamCards(
                    onWifiSettings = { openWifiSettings(context) },
                    onBuyClick = { openUrl(context, BUY_WIZCAM_URL) }
                )
                return@Column
            }

            StreamingCardWizardOnly(
                wizardOk = !ui.disableStream,
                wizardUsername = ui.wizardUsername,
                statusText = ui.status?.wizardName ?: "-",
                battery = ui.status?.battery ?: "-",
                temp = ui.status?.cpuTemp ?: "-",
                isLive = isLive,
                elapsedText = formatElapsed(elapsedSec),
                busy = ui.streamingBusy,
                titleBusy = ui.titleBusy,
                previewBusy = ui.previewBusy,
                previewActive = ui.previewActive,
                onShowPreviewClick = { showPreviewDialog = true },
                onHidePreviewClick = { vm.stopPreview() },
                previewError = ui.previewError ?: playerError,
                titleError = ui.titleError,
                error = ui.error,
                // ✅ NUEVO: input de título
                streamTitle = streamTitle,
                onStreamTitleChange = { streamTitle = it },

                // ✅ NUEVO: start ahora envía título + token y luego inicia
                onStart = { vm.postTitleAndStart(context, streamTitle) },

                onStop = { vm.stopStreaming() },
                streamUrls = vm.streamUrls(),
                onPlayerError = { playerError = it }
            )

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun OfflineWizardCamCards(
    onWifiSettings: () -> Unit,
    onBuyClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(25.dp)
    ) {
        // Card: ya tengo WizCam
        Surface(
            color = Color(0xFF141414),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Stroke),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.wizcam_offline_have_title),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = stringResource(R.string.wizcam_offline_have_body),
                    color = Muted,
                    style = MaterialTheme.typography.bodyMedium
                )

                Surface(
                    color = Color(0xFF101010),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Stroke)
                ) {
                    Text(
                        text = stringResource(R.string.wizcam_offline_tip),
                        color = Color(0xFFB0B0B0),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                FilledTonalButton(
                    onClick = onWifiSettings,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF1A1A1A),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.wizcam_offline_open_wifi_button))
                }
            }
        }

        // Card: no tengo WizCam
        Surface(
            color = Color(0xFF141414),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Stroke),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.wizcam_offline_no_device_title),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = stringResource(R.string.wizcam_offline_no_device_body),
                    color = Muted,
                    style = MaterialTheme.typography.bodyMedium
                )

                Button(
                    onClick = onBuyClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WizardPurple, contentColor = Color.White)
                ) {
                    Icon(Icons.Filled.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.wizcam_offline_buy_button))
                }
            }
        }
    }
}

@Composable
private fun StreamingCardWizardOnly(
    wizardOk: Boolean,
    wizardUsername: String,
    statusText: String,
    battery: String,
    temp: String,
    isLive: Boolean,
    elapsedText: String,
    busy: Boolean,
    titleBusy: Boolean,
    previewBusy: Boolean,
    previewActive: Boolean,
    onShowPreviewClick: () -> Unit,
    onHidePreviewClick: () -> Unit,
    previewError: String?,
    streamTitle: String,
    onStreamTitleChange: (String) -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    streamUrls: List<String>,
    titleError: String?,
    error: String?,
    onPlayerError: (String) -> Unit
) {
    var startPending by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(isLive) {
        if (isLive) startPending = false
    }

    LaunchedEffect(startPending) {
        if (startPending) {
            delay(20_000)
            if (startPending && !isLive) startPending = false
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, Stroke),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.wizcam_detected),
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (wizardOk) stringResource(R.string.account_at, wizardUsername)
                        else stringResource(R.string.wizard_account_not_configured),
                        color = if (wizardOk) Muted else Color(0xFFFFB74D),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (isLive) LiveBadge(elapsedText = elapsedText)
            }

            Surface(
                color = Color.Black,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Stroke),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            ) {
                Box(Modifier.fillMaxSize()) {
                    if (previewActive) {
                        ProPreviewPlayer(
                            url = streamUrls.firstOrNull() ?: "",
                            kind = PreviewKind.IFRAME_WEB,
                            modifier = Modifier.fillMaxSize()
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .background(
                                    Brush.verticalGradient(colors = listOf(Color(0xCC000000), Color.Transparent))
                                )
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = Color(0x1AFFFFFF),
                                    shape = RoundedCornerShape(999.dp),
                                    border = BorderStroke(1.dp, Color(0x22FFFFFF))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Visibility,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = stringResource(R.string.preview_label),
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = onHidePreviewClick,
                                    enabled = !previewBusy,
                                    border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = Color(0x33000000),
                                        contentColor = Color.White,
                                        disabledContentColor = Color(0x88FFFFFF),
                                        disabledContainerColor = Color(0x22000000)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    if (previewBusy) {
                                        CircularProgressIndicator(
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(16.dp),
                                            color = Color.White
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(stringResource(R.string.stopping))
                                    } else {
                                        Icon(Icons.Filled.VisibilityOff, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(stringResource(R.string.hide))
                                    }
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF151515)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.Videocam,
                                            contentDescription = null,
                                            tint = WizardPurple,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = stringResource(R.string.preview_disabled),
                                            color = Color.White,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = stringResource(R.string.preview_disabled_hint),
                                            color = Color(0xFF9E9E9E),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }

                                if (!previewError.isNullOrBlank()) {
                                    Surface(
                                        color = Color(0xFF1A1A1A),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Color(0xFF2A2A2A))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.Warning,
                                                contentDescription = null,
                                                tint = Color(0xFFFFB74D),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = stringResource(R.string.preview_load_failed),
                                                color = Color(0xFFFFD89A),
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            FilledTonalButton(
                                onClick = onShowPreviewClick,
                                enabled = !busy && !previewBusy,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFF1A1A1A),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (previewBusy) {
                                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp), color = Color.White)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.perf_warning_enabling))
                                } else {
                                    Icon(Icons.Filled.Visibility, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.view_preview))
                                }
                            }
                        }
                    }
                }
            }

            // ✅ NUEVO: input para título del stream
            OutlinedTextField(
                value = streamTitle,
                onValueChange = onStreamTitleChange,
                singleLine = true,
                enabled = wizardOk && !busy && !titleBusy && !isLive,
                label = {
                    // si no tienes strings, puedes poner "Título del directo"
                    Text(text = "Título del directo")
                },
                placeholder = {
                    Text(text = "Escribe el título del stream…")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = WizardPurple,
                    unfocusedBorderColor = Stroke,
                    cursorColor = WizardPurple,
                    focusedLabelColor = WizardPurple,
                    unfocusedLabelColor = Muted
                )
            )


            // Mostrar errores de título o streaming
            if (!titleError.isNullOrBlank()) {
                Text(
                    text = titleError,
                    color = Color(0xFFFFB74D),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (!error.isNullOrBlank()) {
                Text(
                    text = error,
                    color = Color(0xFFFF6B6B),
                    style = MaterialTheme.typography.bodySmall
                )
            }


            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                InfoChip(icon = Icons.Filled.BatteryFull, label = stringResource(R.string.battery), value = "${battery}%", modifier = Modifier.weight(1f))
                InfoChip(icon = Icons.Filled.Thermostat, label = stringResource(R.string.temp), value = "${temp}ºC", modifier = Modifier.weight(1f))
            }

            Surface(
                color = Color(0xFF141414),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Stroke)
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.wizard_status, statusText), color = Muted, style = MaterialTheme.typography.bodySmall)

                    // Opcional: mostrar error del título si falla
                    if (!previewError.isNullOrBlank()) {
                        // ya lo enseñas arriba; aquí no hace falta
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                val showStartLoader = (!isLive) && (busy || startPending || titleBusy)

                Button(
                    onClick = {
                        startPending = true
                        onStart()
                    },
                    enabled = wizardOk && !isLive && !busy && !titleBusy && !startPending,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WizardPurple, contentColor = Color.White)
                ) {
                    if (showStartLoader) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp),
                            color = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (busy || titleBusy) stringResource(R.string.starting) else stringResource(R.string.connecting))
                    } else {
                        Text(if (isLive) stringResource(R.string.live_now) else stringResource(R.string.start_live))
                    }
                }

                if (isLive) {
                    FilledTonalButton(
                        onClick = onStop,
                        enabled = !busy,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF1A1A1A),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Filled.StopCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.stop))
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderPro() {
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
            Icon(Icons.Filled.Videocam, contentDescription = null, tint = LiveRed, modifier = Modifier.size(28.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.wizard_live), color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.streaming_and_preview), color = Color(0xFFB0B0B0), style = MaterialTheme.typography.bodyMedium)
        }
    }
    Spacer(Modifier.height(10.dp))
    Divider(color = Color(0xFF1D1D1D))
}

@Composable
private fun InfoChip(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF101010),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Stroke),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF171717)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = Color(0xFFDDDDDD), modifier = Modifier.size(18.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = Color(0xFF8E8E8E), style = MaterialTheme.typography.labelSmall)
                Text(value, color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun LiveBadge(elapsedText: String) {
    val infinite = rememberInfiniteTransition(label = "livePulse")
    val alpha by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Surface(
        color = LiveRed.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, LiveRed.copy(alpha = 0.35f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(LiveRed.copy(alpha = alpha))
            )
            Text("LIVE", color = LiveRed, fontWeight = FontWeight.Bold)
            Text(elapsedText, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun formatElapsed(seconds: Long): String {
    val s = max(0, seconds)
    val hh = s / 3600
    val mm = (s % 3600) / 60
    val ss = s % 60
    return if (hh > 0) "%02d:%02d:%02d".format(hh, mm, ss) else "%02d:%02d".format(mm, ss)
}

private fun openUrl(context: Context, url: String) {
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

private fun openWifiSettings(context: Context) {
    runCatching {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }.recoverCatching {
        val fallback = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(fallback)
    }
}
