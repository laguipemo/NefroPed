package com.laguipemo.nefroped.core.domain.usecase.notification

import com.laguipemo.nefroped.core.domain.model.notification.Notification
import com.laguipemo.nefroped.core.domain.repository.notification.NotificationRepository
import kotlinx.coroutines.flow.Flow

class ObserveNotificationsUseCase(
    private val repository: NotificationRepository
) {
    operator fun invoke(): Flow<List<Notification>> = repository.observeNotifications()
}
