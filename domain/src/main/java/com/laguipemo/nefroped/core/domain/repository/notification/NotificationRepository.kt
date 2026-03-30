package com.laguipemo.nefroped.core.domain.repository.notification

import com.laguipemo.nefroped.core.domain.model.notification.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun observeNotifications(): Flow<List<Notification>>
    suspend fun markAsRead(id: String): Result<Unit>
    suspend fun deleteNotification(id: String): Result<Unit>
}
