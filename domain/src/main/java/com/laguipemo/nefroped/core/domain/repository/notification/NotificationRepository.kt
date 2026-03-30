package com.laguipemo.nefroped.core.domain.repository.notification

import com.laguipemo.nefroped.core.domain.model.notification.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    /**
     * Observa las notificaciones en tiempo real del usuario actual.
     */
    fun observeNotifications(): Flow<List<Notification>>

    /**
     * Marca una notificación como leída.
     */
    suspend fun markAsRead(id: String): Result<Unit>

    /**
     * Elimina una notificación.
     */
    suspend fun deleteNotification(id: String): Result<Unit>

    /**
     * Marca todas las notificaciones como leídas.
     */
    suspend fun markAllAsRead(): Result<Unit>
}
