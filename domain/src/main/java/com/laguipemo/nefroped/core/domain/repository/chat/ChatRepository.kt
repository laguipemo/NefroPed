package com.laguipemo.nefroped.core.domain.repository.chat

import com.laguipemo.nefroped.core.domain.model.chat.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeMessages(conversationId: String): Flow<List<Message>>

    suspend fun sendMessage(
        conversationId: String,
        content: String,
        clientId: String
    )
}