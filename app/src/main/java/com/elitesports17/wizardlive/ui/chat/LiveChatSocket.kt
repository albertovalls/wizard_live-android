package com.elitesports17.wizardlive.ui.chat

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class LiveChatSocket(
    private val url: String,
    private val onMessageReceived: (ChatMessage) -> Unit,
    private val onConnectionState: (Boolean) -> Unit,
    private val onViewersChanged: (Int) -> Unit
) {

    private val TAG = "LIVE_CHAT_WS"

    // 🔑 Scope en Main Thread para Compose
    private val uiScope = CoroutineScope(Dispatchers.Main)

    private val client = OkHttpClient.Builder()
        .retryOnConnectionFailure(true)
        .pingInterval(30, TimeUnit.SECONDS) // ✅ igual que web
        .build()

    private var webSocket: WebSocket? = null

    fun connect() {
        Log.d(TAG, "🔌 Connecting to $url")

        val request = Request.Builder()
            .url(url)
            .addHeader("Origin", "https://wizardfootball.com")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d(TAG, "🟢 WS OPENED")
                onConnectionState(true)
            }

            override fun onMessage(ws: WebSocket, text: String) {
                Log.d(TAG, "⬇️ RAW: $text")

                try {
                    val json = JSONObject(text)
                    val type = json.optString("type")

                    Log.d(TAG, "📦 MESSAGE TYPE = $type")

                    when (type) {

                        "chat" -> {
                            Log.d(
                                TAG,
                                "🧪 CHAT RAW from=${json.optString("from")} text=${json.optString("text")}"
                            )

                            val msg = ChatMessage(
                                nick = json.optString(
                                    "from",
                                    json.optString("nick", "anon")
                                ),
                                text = json.optString("text", ""),
                                ts = json.optLong("ts")
                            )

                            Log.d(TAG, "✅ CHAT ${msg.nick}: ${msg.text}")

                            // 🔥 CAMBIO CRÍTICO: mandar a Main Thread
                            uiScope.launch {
                                onMessageReceived(msg)
                            }
                        }

                        "pong" -> {
                            Log.d(TAG, "🏓 PONG recibido")
                        }



                        "already-watching" -> {
                            Log.w(TAG, "⚠️ ALREADY WATCHING")
                        }

                        "presence" -> {
                            val viewers = json.optInt("viewers", 0)
                            onViewersChanged(viewers)
                        }


                        "stream-ended" -> {
                            Log.w(TAG, "🔴 STREAM ENDED")
                        }

                        else -> {
                            Log.d(TAG, "ℹ️ OTHER MESSAGE: $json")
                        }
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "❌ Parse error", e)
                }
            }

            override fun onMessage(ws: WebSocket, bytes: ByteString) {
                Log.d(TAG, "⬇️ Binary (${bytes.size} bytes)")
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.w(TAG, "🔴 WS CLOSED $code / $reason")
                onConnectionState(false)
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "❌ WS FAILURE", t)
                onConnectionState(false)
            }
        })
    }

    fun sendMessage(text: String, nick: String) {
        val socket = webSocket ?: return

        val payload = JSONObject().apply {
            put("type", "chat")
            put("nick", nick.take(20))
            put("text", text.take(240))
            put("ts", System.currentTimeMillis())
        }

        val sent = socket.send(payload.toString())
        Log.d(TAG, "⬆️ SEND result=$sent payload=$payload")
    }

    fun disconnect() {
        Log.d(TAG, "🔌 Disconnecting WS")
        webSocket?.close(1000, "Client closed")
        webSocket = null
    }
}
