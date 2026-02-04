package com.elitesports17.wizardlive.ui.broadcast

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

enum class PreviewKind { IFRAME_WEB, MJPEG_IMG }

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is android.content.ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ProPreviewPlayer(
    url: String,
    kind: PreviewKind,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    // ✅ Para que el Scroll de Compose no robe gestos al WebView
    val interop = rememberNestedScrollInteropConnection()

    // ✅ Cache buster (evita negro por caché / refrescos)
    val bustedUrl = remember(url) {
        if (url.isBlank()) url else "$url${if (url.contains("?")) "&" else "?"}t=${System.currentTimeMillis()}"
    }

    // ✅ BaseURL seguro (si te llega vacío)
    val baseUrl = remember(url) {
        when {
            url.startsWith("http://") || url.startsWith("https://") -> url
            else -> "https://localhost/"
        }
    }

    val html = remember(bustedUrl, kind) {
        when (kind) {
            PreviewKind.IFRAME_WEB -> """
                <!DOCTYPE html>
                <html>
                  <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                    <style>
                      html, body { margin:0; padding:0; background:#000; height:100%; overflow:hidden; }
                      iframe { width:100%; height:100%; border:0; }
                    </style>
                  </head>
                  <body>
                    <iframe
                      src="$bustedUrl"
                      allow="autoplay; fullscreen"
                      allowfullscreen>
                    </iframe>
                  </body>
                </html>
            """.trimIndent()

            PreviewKind.MJPEG_IMG -> """
                <!DOCTYPE html>
                <html>
                  <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                    <style>
                      html, body { margin:0; padding:0; background:#000; height:100%; overflow:hidden; }
                      img { width:100%; height:100%; object-fit:contain; display:block; }
                    </style>
                  </head>
                  <body>
                    <img src="$bustedUrl" />
                  </body>
                </html>
            """.trimIndent()
        }
    }

    // ============================
    // Fullscreen (WebChromeClient)
    // ============================
    var fullscreenView by remember { mutableStateOf<View?>(null) }
    var fullscreenCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }

    fun exitFullscreen() {
        fullscreenCallback?.onCustomViewHidden()
        fullscreenCallback = null
        fullscreenView = null

        // restaura flags si quieres
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    DisposableEffect(Unit) {
        onDispose {
            // si el composable se va y estaba fullscreen -> salir limpio
            exitFullscreen()
        }
    }

    // ✅ Dialog fullscreen para alojar el customView que entrega Chrome
    if (fullscreenView != null) {
        Dialog(
            onDismissRequest = { exitFullscreen() },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    FrameLayout(ctx).apply {
                        setBackgroundColor(Color.BLACK)
                        val v = fullscreenView!!

                        // IMPORTANT: el custom view puede venir ya con parent
                        (v.parent as? ViewGroup)?.removeView(v)

                        addView(
                            v,
                            FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        )
                    }
                },
                update = { container ->
                    val v = fullscreenView ?: return@AndroidView
                    // evita dobles parents
                    (v.parent as? ViewGroup)?.let { parent ->
                        if (parent !== container) parent.removeView(v)
                    }
                    if (v.parent == null) {
                        container.removeAllViews()
                        container.addView(
                            v,
                            FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        )
                    }
                }
            )
        }
    }

    // ============================
    // WebView
    // ============================
    AndroidView(
        modifier = modifier.nestedScroll(interop),
        factory = { ctx ->
            WebView(ctx).apply {
                setBackgroundColor(Color.BLACK)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                // ✅ MUY IMPORTANTE: deja que el WebView reciba los taps (scroll padre no roba)
                setOnTouchListener { v, event ->
                    v.parent?.requestDisallowInterceptTouchEvent(true)
                    false
                }

                // Settings “pro”
                settings.javaScriptEnabled = (kind == PreviewKind.IFRAME_WEB)
                settings.domStorageEnabled = true
                settings.loadsImagesAutomatically = true

                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true

                // Autoplay
                settings.mediaPlaybackRequiresUserGesture = false

                // Mixed content (http dentro de https etc.)
                @Suppress("DEPRECATION")
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                // Extras útiles
                settings.javaScriptCanOpenWindowsAutomatically = true
                settings.setSupportMultipleWindows(false)

                settings.allowFileAccess = true
                settings.allowContentAccess = true

                // ✅ Fullscreen support
                webChromeClient = object : WebChromeClient() {
                    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                        if (view == null) return
                        fullscreenCallback?.onCustomViewHidden()

                        fullscreenView = view
                        fullscreenCallback = callback

                        // opcional: mantener pantalla activa durante fullscreen
                        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }

                    override fun onHideCustomView() {
                        exitFullscreen()
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        // por si el padre vuelve a intentar interceptar
                        view?.parent?.requestDisallowInterceptTouchEvent(true)
                        super.onPageFinished(view, url)
                    }
                }

                // ✅ Carga HTML con baseURL para origen correcto
                loadDataWithBaseURL(baseUrl, html, "text/html", "utf-8", null)
            }
        },
        update = { web ->
            // refresco seguro cuando cambia url/kind
            web.loadDataWithBaseURL(baseUrl, html, "text/html", "utf-8", null)
        }
    )
}
