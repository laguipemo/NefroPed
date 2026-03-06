package com.laguipemo.nefroped.features.chat

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laguipemo.nefroped.features.chat.components.DateSeparator
import com.laguipemo.nefroped.features.chat.components.MessageItem
import com.laguipemo.nefroped.features.chat.util.formatDateHeader
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var content by remember { mutableStateOf("") }

    // 1. Estado para controlar el scroll
    val listState = rememberLazyListState()

    // 2. Efecto para hacer scroll al final cuando lleguen mensajes nuevos
    LaunchedEffect(uiState) {
        if (uiState is ChatUiState.Active) {
            val messages = (uiState as ChatUiState.Active).messages
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Scaffold(
        // 1. Elementos FIJOS al fondo
        bottomBar = {
            if (uiState is ChatUiState.Active) {
                val state = uiState as ChatUiState.Active
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .navigationBarsPadding() // RESUELVE EL SOLAPAMIENTO con la barra del sistema
                        .imePadding() // Empuja el contenido cuando sale el teclado
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    state.remainingMessages?.let {
                        Text(
                            "Mensajes restantes: $it",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (state.remainingMessages == 0) {
                        Text(
                            "Límite alcanzado. Regístrate para continuar.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            modifier = Modifier
                                .weight(1f),
                            placeholder = { Text("Escribe un mensaje...") }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                viewModel.sendMessage("default", content)
                                content = ""
                            },
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape) // Color y forma
                                .padding(8.dp)
                                .size(24.dp),
                            enabled = state.canSendMessage
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Enviar",
                                tint = Color.White
                            )
                        }
                    }

                    if (!state.capabilities.canExport) {
                        Text(
                            "Exportación no disponible en modo invitado",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    ) { padding ->
        // 2. Área de MENSAJES (ocupa el resto de la pantalla)
        Column(
            modifier = Modifier
                .padding(padding) // Respeta el espacio del bottomBar
                .fillMaxSize()
        ) {
            when (val state = uiState) {
                ChatUiState.Loading -> Text(
                    "Cargando...",
                    modifier = Modifier.padding(16.dp)
                )

                is ChatUiState.Active -> {
                    // 1. Envolvemos todo en un Box para permitir capas (z-axis)
                    Box(modifier = Modifier.fillMaxSize()) {
                        val scope = rememberCoroutineScope()

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(
                                state.messages,
                                key = { _, message -> message.clientId } // Usar clientId para evitar parpadeos
                            ) { index, message ->
                                // 1. Lógica de Cabecera de Fecha
                                val showHeader = if (index == 0) {
                                    true // Siempre mostrar en el primer mensaje
                                } else {
                                    val prevMessage = state.messages[index - 1]
                                    val currentDate = message.createdAt
                                        .toLocalDateTime(TimeZone.currentSystemDefault()).date
                                    val prevDate = prevMessage.createdAt
                                        .toLocalDateTime(TimeZone.currentSystemDefault()).date
                                    currentDate != prevDate // Mostrar solo si el día cambió
                                }
                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                        .animateItem()
                                ) {
                                    if (showHeader) {
                                        DateSeparator(formatDateHeader(message.createdAt))
                                    }

                                    MessageItem(
                                        message = message,
                                        isMine = message.userId == (uiState as ChatUiState.Active).currentUserId
                                    )
                                }
                            }
                        }
                        // 2. EL BOTÓN (Arriba en el eje Z)
                        // Calculamos visibilidad: si el primer item visible > 2
                        val showScrollToBottom by remember {
                            derivedStateOf {
                                val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                                val totalItems = listState.layoutInfo.totalItemsCount

                                // El botón se muestra si:
                                // 1. Hay mensajes en la lista
                                // 2. El último item visible NO es el último de la lista (el usuario subió)
                                (lastVisibleItem != null && lastVisibleItem.index < totalItems - 1)
                            }
                        }
                        // 3. El botón flotante (capa superior)
                        // Al estar fuera de la LazyColumn pero dentro del Box, podemos usar Alignment.BottomEnd
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showScrollToBottom,
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut(),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        ) {
                            // Envolvemos el FAB en un BadgedBox
                            BadgedBox(
                                badge = {
                                    if (showScrollToBottom) {
                                        Badge(
                                            containerColor = Color.Red,
                                            modifier = Modifier.offset(x = (-8).dp, y = 8.dp) // Ajuste fino de posición
                                        )
                                    }
                                }
                            ) {
                                FloatingActionButton(
                                    onClick = {
                                        scope.launch {
                                            // Bajamos al último mensaje de la lista
                                            val lastIndex =
                                                (uiState as? ChatUiState.Active)?.messages?.size
                                                    ?: 0
                                            if (lastIndex > 0) {
                                                listState.animateScrollToItem(
                                                    lastIndex - 1
                                                )
                                            }
                                        }
                                    },
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    shape = CircleShape,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Ir al final"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

