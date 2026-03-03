package com.laguipemo.nefroped.core.domain.usecase.chat

import com.laguipemo.nefroped.core.domain.model.chat.Message
import kotlinx.coroutines.flow.Flow

interface ObserveMessagesUseCase {
    operator fun invoke(conversationId: String): Flow<List<Message>>
}