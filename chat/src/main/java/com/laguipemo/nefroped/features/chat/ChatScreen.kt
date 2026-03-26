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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laguipemo.nefroped.designsystem.R
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
    onBackClick: (() -> Unit)? = null,
    viewModel: ChatViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var content by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val darkTheme = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()

    val reversedMessages = remember(uiState) {
        (uiState as? ChatUiState.Active)?.messages?.asReversed() ?: emptyList()
    }

    // Efecto para scroll automático al recibir o enviar mensajes
    LaunchedEffect(reversedMessages.size) {
        if (reversedMessages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    val showScrollToBottom by remember {
        derivedStateOf { listState.canScrollBackward }
    }

    SystemBarsController(
        useStatusDarkIcons = false,
        useNavigationDarkIcons = !darkTheme
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (viewModel.conversationId != "general" && viewModel.topicTitle != null) {
                            Text(
                                text = "Consulta sobre:",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = viewModel.topicTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.chat_title),
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (viewModel.conversationId != "general" && onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                ),
                windowInsets = WindowInsets.statusBars
            )
        },
        contentWindowInsets = WindowInsets.systemBars.union(WindowInsets.ime)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                ChatUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                }

                is ChatUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is ChatUiState.Active -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f)) {
                            LazyColumn(
                                state = listState,
                                reverseLayout = true,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = dimensionResource(R.dimen.space_m),
                                    end = dimensionResource(R.dimen.space_m),
                                    top = dimensionResource(R.dimen.space_m),
                                    bottom = dimensionResource(R.dimen.space_s)
                                ),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(reversedMessages, key = { _, m -> m.clientId }) { index, message ->
                                    val showHeader = index == reversedMessages.size - 1 || 
                                        message.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date != 
                                        reversedMessages[index + 1].createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date
                                    
                                    Column(modifier = Modifier.fillMaxWidth().animateItem()) {
                                        if (showHeader) DateSeparator(formatDateHeader(message.createdAt))
                                        MessageItem(message = message, isMine = message.userId == state.currentUserId)
                                    }
                                }
                            }

                            androidx.compose.animation.AnimatedVisibility(
                                visible = showScrollToBottom,
                                modifier = Modifier.align(Alignment.BottomEnd).padding(dimensionResource(R.dimen.space_m)),
                                enter = fadeIn() + scaleIn(),
                                exit = fadeOut() + scaleOut()
                            ) {
                                SmallFloatingActionButton(
                                    onClick = { 
                                        scope.launch { 
                                            listState.animateScrollToItem(0) 
                                        } 
                                    },
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                                    shape = CircleShape
                                ) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = stringResource(R.string.chat_scroll_to_bottom))
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = dimensionResource(R.dimen.space_m)),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = RoundedCornerShape(28.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                tonalElevation = 3.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.space_s), vertical = dimensionResource(R.dimen.space_xs)),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = content,
                                        onValueChange = { content = it },
                                        modifier = Modifier.weight(1f),
                                        placeholder = { Text(stringResource(R.string.chat_input_placeholder), style = MaterialTheme.typography.bodyMedium) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color.Transparent,
                                            unfocusedBorderColor = Color.Transparent
                                        ),
                                        maxLines = 4
                                    )
                                    
                                    Box(
                                        modifier = Modifier
                                            .padding(end = dimensionResource(R.dimen.space_xs))
                                            .size(dimensionResource(R.dimen.space_xl))
                                            .background(
                                                color = if (content.isNotBlank() && state.canSendMessage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                shape = CircleShape
                                            )
                                            .clickable(enabled = content.isNotBlank() && state.canSendMessage) {
                                                viewModel.sendMessage(content)
                                                content = ""
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Send,
                                            contentDescription = stringResource(R.string.chat_send_description),
                                            tint = Color.White,
                                            modifier = Modifier.size(dimensionResource(R.dimen.space_m))
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_s)))
                            
                            state.remainingMessages?.let {
                                Text(
                                    stringResource(R.string.chat_remaining_messages, it),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (it <= 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_s)))
                        }
                    }
                }
            }
        }
    }
}
