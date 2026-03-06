package com.laguipemo.nefroped.core.data.dto

import com.laguipemo.nefroped.core.domain.model.chat.Message
import kotlinx.serialization.Serializable
import kotlin.time.Instant


@Serializable
data class MessageDto(
    val id: String,
    val client_id: String,
    val conversation_id: String,
    val content: String,
    val user_id: String,
    val email: String,
    val role: String,
    val created_at: String,
)

fun MessageDto.toDomain(): Message = Message(
    id = id,
    clientId = client_id,
    conversationId = conversation_id,
    content = content,
    userId = user_id,
    email = email,
    role = role,
    createdAt = Instant.parse(created_at)
)
