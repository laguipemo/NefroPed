package com.laguipemo.nefroped.core.domain.model.notification

import kotlinx.datetime.Instant

data class Notification(
    val id: String,
    val title: String,
    val body: String,
    val type: NotificationType,
    val isRead: Boolean,
    val createdAt: Instant,
    val payload: Map<String, String> = emptyMap()
)

enum class NotificationType {
    NEW_CONTENT,    // Nuevo tema o lección
    CHAT_REPLY,     // Respuesta en un chat
    SYSTEM,         // Mensajes del sistema
    OTHER
}
