package com.laguipemo.nefroped.core.domain.usecase.chat

interface SendMessageUseCase {
    suspend operator fun invoke(conversationId: String, content: String, clientId: String)
}