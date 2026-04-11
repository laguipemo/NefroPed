package com.laguipemo.nefroped.core.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class NotificationDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val title: String,
    val body: String,
    val type: String,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("created_at") val createdAt: String,
    val payload: Map<String, String?>? = null
)
