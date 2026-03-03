package com.laguipemo.nefroped.core.domain.usecase.chat

import com.laguipemo.nefroped.core.domain.repository.chat.ChatRepository

class SendMessageUseCaseImpl(
    private val repository: ChatRepository
) : SendMessageUseCase {
    override suspend fun invoke(conversationId: String, content: String, clientId: String) {
        repository.sendMessage(conversationId, content, clientId)
    }
}