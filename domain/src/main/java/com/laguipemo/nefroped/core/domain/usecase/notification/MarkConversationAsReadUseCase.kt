package com.laguipemo.nefroped.core.domain.usecase.notification

import com.laguipemo.nefroped.core.domain.repository.notification.NotificationRepository

class MarkConversationAsReadUseCase(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(conversationId: String): Result<Unit> = 
        repository.markConversationAsRead(conversationId)
}
