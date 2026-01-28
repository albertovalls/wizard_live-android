package com.elitesports17.wizardlive.ui.player

import android.content.pm.ActivityInfo
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.FullscreenExit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.mediarouter.app.MediaRouteButton
import com.elitesports17.wizardlive.ui.chat.LiveChat
import com.elitesports17.wizardlive.ui.util.UserSession
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    streamUrl: String,
    channelSlug: String,
    serial: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as FragmentActivity

    var isFullscreen by remember { mutableStateOf(false) }
    var controlsVisible by remember { mutableStateOf(true) }
    var token by remember { mutableStateOf("") }

    /* ================= TOKEN ================= */

    LaunchedEffect(Unit) {
        token = UserSession.getToken(context) ?: ""
    }

    /* ================= FULLSCREEN / ORIENTATION ================= */

    DisposableEffect(isFullscreen) {
        if (isFullscreen) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        onDispose {}
    }

    /* ================= EXOPLAYER ================= */

    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(streamUrl))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose { player.release() }
    }

    /* ================= CAST ================= */

    val castContext = remember { CastContext.getSharedInstance(context) }

    DisposableEffect(Unit) {
        val sessionManager = castContext.sessionManager

        val listener = object : SessionManagerListener<CastSession> {
            override fun onSessionStarted(session: CastSession, sessionId: String) {
                player.pause()
            }

            override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
                player.pause()
            }

            override fun onSessionEnded(session: CastSession, error: Int) {
                player.play()
            }

            override fun onSessionStarting(session: CastSession) {}
            override fun onSessionStartFailed(session: CastSession, error: Int) {}
            override fun onSessionEnding(session: CastSession) {}
            override fun onSessionResuming(session: CastSession, sessionId: String) {}
            override fun onSessionResumeFailed(session: CastSession, error: Int) {}
            override fun onSessionSuspended(session: CastSession, reason: Int) {}
        }

        sessionManager.addSessionManagerListener(listener, CastSession::class.java)
        onDispose {
            sessionManager.removeSessionManagerListener(listener, CastSession::class.java)
        }
    }

    /* ================= BACK ================= */

    BackHandler {
        if (isFullscreen) {
            isFullscreen = false
        } else {
            onBack()
        }
    }

    /* ================= UI ================= */

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column {

            /* ================= VIDEO ================= */

            Box(
                modifier = if (isFullscreen) {
                    Modifier.fillMaxSize()
                } else {
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                }
            ) {

                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            setPlayer(player)
                            useController = true
                            controllerAutoShow = true
                            controllerHideOnTouch = true
                            controllerShowTimeoutMs = 1300
                            // 🔥 SINCRONIZA CON CONTROLES NATIVOS
                            setControllerVisibilityListener(
                                PlayerView.ControllerVisibilityListener { visibility ->
                                    controlsVisible = visibility == View.VISIBLE
                                }
                            )

                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    }
                )

                /* ================= TOP BAR ================= */

                if (controlsVisible) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(12.dp)
                            .align(Alignment.TopStart),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        IconButton(
                            onClick = {
                                if (isFullscreen) isFullscreen = false else onBack()
                            }
                        ) {
                            Icon(Icons.Outlined.Close, null, tint = Color.White)
                        }

                        Spacer(Modifier.weight(1f))

                        AndroidView(
                            modifier = Modifier.size(32.dp),
                            factory = {
                                val appCtx = ContextThemeWrapper(
                                    activity,
                                    androidx.appcompat.R.style.Theme_AppCompat_DayNight_NoActionBar
                                )
                                val routerCtx = ContextThemeWrapper(
                                    appCtx,
                                    androidx.mediarouter.R.style.Theme_MediaRouter
                                )

                                MediaRouteButton(
                                    routerCtx,
                                    null,
                                    androidx.mediarouter.R.attr.mediaRouteButtonStyle
                                ).apply {
                                    CastButtonFactory.setUpMediaRouteButton(routerCtx, this)
                                }
                            }
                        )

                        Spacer(Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                isFullscreen = !isFullscreen
                                controlsVisible = true
                            }
                        ) {
                            Icon(
                                if (isFullscreen)
                                    Icons.Outlined.FullscreenExit
                                else
                                    Icons.Outlined.Fullscreen,
                                null,
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            /* ================= CONTENEDOR INFERIOR ================= */

            if (!isFullscreen) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Wizard Live",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Streaming en directo",
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )
                    }

                    Divider(color = Color.DarkGray.copy(alpha = 0.4f))

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Chat, null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Live Chat",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        if (token.isNotBlank()) {
                            LiveChat(
                                room = channelSlug,
                                token = token
                            )
                        } else {
                            Text(
                                "Conectando al chat…",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
