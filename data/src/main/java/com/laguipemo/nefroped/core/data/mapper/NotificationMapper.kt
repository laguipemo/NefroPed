package com.laguipemo.nefroped.core.data.mapper

import com.laguipemo.nefroped.core.data.dto.NotificationDto
import com.laguipemo.nefroped.core.domain.model.notification.Notification
import com.laguipemo.nefroped.core.domain.model.notification.NotificationType
import kotlinx.datetime.Instant

internal fun NotificationDto.toDomain(): Notification {
    return Notification(
        id = id,
        title = title,
        body = body,
        type = try { 
            NotificationType.valueOf(type.uppercase()) 
        } catch (e: Exception) { 
            NotificationType.OTHER 
        },
        isRead = isRead,
        createdAt = Instant.parse(createdAt),
        payload = payload ?: emptyMap()
    )
}
