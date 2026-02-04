package com.elitesports17.wizardlive.ui.broadcast

import android.view.ViewGroup
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun JetsonStreamPlayer(
    urls: List<String>,
    modifier: Modifier = Modifier,
    onError: (String) -> Unit = {}
) {
    var currentIndex by remember { mutableStateOf(0) }
    val currentUrl = urls.getOrNull(currentIndex)

    if (currentUrl == null) {
        LaunchedEffect(Unit) { onError("No hay URLs de stream configuradas.") }
        return
    }

    // ✅ Mantener referencia al player y soltarlo al desmontar
    var playerRef by remember { mutableStateOf<ExoPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            playerRef?.release()
            playerRef = null
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val player = ExoPlayer.Builder(ctx).build().apply {
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_OFF
                volume = 0f // preview sin sonido
            }
            playerRef = player

            val playerView = PlayerView(ctx).apply {
                this.player = player
                useController = false
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            fun play(url: String) {
                player.setMediaItem(MediaItem.fromUri(url))
                player.prepare()
                player.play()
            }

            player.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    if (currentIndex < urls.lastIndex) {
                        currentIndex += 1
                    } else {
                        onError("ExoPlayer error: ${error.errorCodeName}")
                    }
                }
            })

            play(currentUrl)
            playerView
        },
        update = { view ->
            val player = view.player as? ExoPlayer ?: return@AndroidView
            player.setMediaItem(MediaItem.fromUri(currentUrl))
            player.prepare()
            player.play()
        }
    )
}
