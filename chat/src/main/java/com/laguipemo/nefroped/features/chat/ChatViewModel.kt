package com.laguipemo.nefroped.features.chat

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.laguipemo.nefroped.core.domain.model.chat.Message
import com.laguipemo.nefroped.core.domain.model.session.SessionState
import com.laguipemo.nefroped.core.domain.usecase.chat.ObserveMessagesUseCase
import com.laguipemo.nefroped.core.domain.usecase.chat.ResolveChatCapabilitiesUseCase
import com.laguipemo.nefroped.core.domain.usecase.chat.SendMessageUseCase
import com.laguipemo.nefroped.core.domain.usecase.session.ObserveSessionStateUseCase
import com.laguipemo.nefroped.navigation.AuthenticatedRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.time.Clock


class ChatViewModel(
    savedStateHandle: SavedStateHandle,
    private val observeSessionState: ObserveSessionStateUseCase,
    private val resolveChatCapabilities: ResolveChatCapabilitiesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val observeMessages: ObserveMessagesUseCase
) : ViewModel() {

    private val conversationId: String =
        savedStateHandle.toRoute<AuthenticatedRoute.Chat>().conversationId

    private val _localMessages = MutableStateFlow<List<Message>>(emptyList())

    val uiState: StateFlow<ChatUiState> =
        combine(
            observeSessionState(),
            observeMessages(conversationId),
            _localMessages
        ) { sessionState, remoteMessages, localMessages ->

            // Obtener id de usuario atenticado
            val currentUserId = (sessionState as? SessionState.User)?.user?.id

            val mergeMessages = (remoteMessages + localMessages)
                .distinctBy { it.clientId }
                .sortedBy { it.createdAt }

            val sendCount = if (sessionState is SessionState.User)
                remoteMessages.count { it.userId == sessionState.user.id }
            else 0

            val capabilities = resolveChatCapabilities(sessionState)
            val remaining = capabilities.messageLimit?.let {
                (it - sendCount).coerceAtLeast(0)
            }
            val canSend = remaining != 0

            ChatUiState.Active(
                messages = mergeMessages,
                capabilities = capabilities,
                remainingMessages = remaining,
                canSendMessage = canSend,
                currentUserId = currentUserId
            )
        }
            .distinctUntilChanged() // Evita que la UI parpadee
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                ChatUiState.Loading
            )

    fun sendMessage(conversationId: String, content: String) {
        viewModelScope.launch {
            when (val sessionState = observeSessionState().first()) {
                is SessionState.User -> {
                    val user = sessionState.user
                    val clientId = UUID.randomUUID().toString()
                    Log.i("CHACHY::: ChatViewModel", "userId: ${user.id} clientId: ${clientId}")
                    val tempMessage = Message(
                        id = "local-${System.currentTimeMillis()}",
                        clientId = clientId,
                        conversationId = conversationId,
                        content = content,
                        userId = user.id,
                        role = "user",
                        email = user.email ?: "",
                        createdAt = Clock.System.now()
                    )
                    _localMessages.update { it + tempMessage }
                    try {
                        sendMessageUseCase(conversationId, content, clientId)
                        // Éxito: Lo eliminamos de la lista local porque ya sabemos que vendrá por el Flow remoto
                        _localMessages.update { list -> list.filterNot { it.clientId == clientId } }
                    } catch (e: Exception) {
                        Log.i("CHACHY::: ChatViewModel ", e.stackTraceToString())
                        // Error: Lo mantenemos pero marcamos el error
                        _localMessages.update { list ->
                            list.map {
                                if (it.clientId == clientId)
                                    it.copy(isSending = false, isError = true)
                                else it
                            }
                        }
                    }

                }

                else -> return@launch

            }

        }
    }

}