package com.elitesports17.wizardlive.ui.chat

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elitesports17.wizardlive.ui.chat.ViewersViewModel


@Composable
fun LiveChat(
    room: String,   // 👈 channelSlug
    token: String
) {
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val listState = rememberLazyListState()
    var viewers by remember { mutableStateOf(0) }
    var input by remember { mutableStateOf("") }
    var connected by remember { mutableStateOf(false) }
    val viewersViewModel: ViewersViewModel = viewModel()

    val cleanToken = remember(token) {
        token.removePrefix("Bearer ").trim()
    }

    val wsUrl = remember {
        "wss://livewizard.westeurope.cloudapp.azure.com/ws" +
                "?room=${Uri.encode(room)}&token=$cleanToken"
    }

    val socket = remember {
        LiveChatSocket(
            url = wsUrl,
            onViewersChanged = { count ->
                viewers = count
                viewersViewModel.update(room, count) // 🔥 CLAVE
            },
            onMessageReceived = { msg ->
                messages.add(msg)
            },
            onConnectionState = { state ->
                connected = state
            }
        )
    }

    // 🔌 Conectar / desconectar WS
    DisposableEffect(Unit) {
        socket.connect()
        onDispose {
            socket.disconnect()
        }
    }

    // ⬇️ Auto-scroll al último mensaje
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Visibility,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "$viewers viendo ahora",
                color = Color.LightGray,
                fontSize = 12.sp
            )
        }


        // 🟢 Estado conexión
        Text(
            text = if (connected) "🟢 Conectado" else "🔴 Desconectado",
            color = if (connected) Color.Green else Color.Red,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 💬 MENSAJES
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(messages) { msg ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {

                    Text(
                        text = msg.nick,        // ✅ CORRECTO
                        color = Color(0xFFB983FF),
                        fontSize = 12.sp
                    )

                    Text(
                        text = msg.text,     // ✅ CORRECTO
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Divider(color = Color.DarkGray)

        // ✍️ INPUT
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escribe un mensaje…") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                enabled = connected && input.isNotBlank(),
                onClick = {
                    socket.sendMessage(
                        text = input,
                        nick = room
                    )
                    input = ""
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB983FF),
                    disabledContainerColor = Color(0xFF3A3A3A)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Send,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Enviar",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

        }
    }
}
