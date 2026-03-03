package com.laguipemo.nefroped.core.domain.usecase.chat

import com.laguipemo.nefroped.core.domain.model.chat.Message
import com.laguipemo.nefroped.core.domain.repository.chat.ChatRepository
import kotlinx.coroutines.flow.Flow

class ObserveMessagesUseCaseImpl(
    private val repository: ChatRepository
) : ObserveMessagesUseCase {
    override fun invoke(conversationId: String): Flow<List<Message>> =
        repository.observeMessages(conversationId)
}