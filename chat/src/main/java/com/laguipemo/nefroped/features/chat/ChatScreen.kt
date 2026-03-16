package com.laguipemo.nefroped.features.chat

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laguipemo.nefroped.designsystem.components.SystemBarsController
import com.laguipemo.nefroped.features.chat.components.DateSeparator
import com.laguipemo.nefroped.features.chat.components.MessageItem
import com.laguipemo.nefroped.features.chat.util.formatDateHeader
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var content by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val darkTheme = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Lógica para mostrar el FAB de "bajar al final"
    // Aparece si podemos hacer scroll hacia adelante (hacia abajo)
    val showScrollToBottom by remember {
        derivedStateOf {
            listState.canScrollForward
        }
    }

    SystemBarsController(
        useStatusDarkIcons = false,
        useNavigationDarkIcons = !darkTheme
    )

    LaunchedEffect(uiState) {
        if (uiState is ChatUiState.Active) {
            val messages = (uiState as ChatUiState.Active).messages
            if (messages.isNotEmpty() && !listState.isScrollInProgress) {
                // Solo auto-scroll si no estamos navegando manualmente
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .statusBarsPadding()
            .imePadding()
    ) {
        CenterAlignedTopAppBar(
            title = { Text("Consultas", fontWeight = FontWeight.Bold, color = Color.White) },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent
            )
        )

        Box(modifier = Modifier.weight(1f)) {
            when (val state = uiState) {
                ChatUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
                is ChatUiState.Active -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(state.messages, key = { _, m -> m.clientId }) { index, message ->
                            val showHeader = index == 0 || 
                                message.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date != 
                                state.messages[index-1].createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date
                            
                            Column(modifier = Modifier.fillMaxWidth().animateItem()) {
                                if (showHeader) DateSeparator(formatDateHeader(message.createdAt))
                                MessageItem(message = message, isMine = message.userId == state.currentUserId)
                            }
                        }
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = showScrollToBottom,
                        modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        SmallFloatingActionButton(
                            onClick = { 
                                scope.launch { 
                                    if (state.messages.isNotEmpty()) {
                                        listState.animateScrollToItem(state.messages.size - 1) 
                                    }
                                } 
                            },
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Bajar al final")
                        }
                    }
                }
                is ChatUiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        if (uiState is ChatUiState.Active) {
            val state = uiState as ChatUiState.Active
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    tonalElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Escribe un mensaje...", style = MaterialTheme.typography.bodyMedium) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            maxLines = 4
                        )
                        
                        Box(
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(32.dp)
                                .background(
                                    color = if (content.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .clickable(enabled = content.isNotBlank() && state.canSendMessage) {
                                    viewModel.sendMessage("default", content)
                                    content = ""
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Enviar",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                state.remainingMessages?.let {
                    Text(
                        "Mensajes restantes: $it",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
