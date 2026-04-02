package com.laguipemo.nefroped.core.domain.usecase.notification

import com.laguipemo.nefroped.core.domain.repository.notification.NotificationRepository

class DeleteNotificationUseCase(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> = repository.deleteNotification(id)
}
