package com.laguipemo.nefroped.features.chat

import com.laguipemo.nefroped.core.domain.model.chat.ChatCapabilities
import com.laguipemo.nefroped.core.domain.model.chat.Message

sealed interface ChatUiState {
    object Loading : ChatUiState
    data class Active(
        val messages: List<Message>,
        val capabilities: ChatCapabilities,
        val remainingMessages: Int?,
        val canSendMessage: Boolean,
        val currentUserId: String? = null
    ) : ChatUiState
}