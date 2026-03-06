package com.laguipemo.nefroped.core.domain.model.chat

import kotlin.time.Instant


data class Message(
    val id: String,
    val clientId: String,
    val conversationId: String,
    val content: String,
    val userId: String?,
    val email: String,
    val role: String,
    val createdAt: Instant,
    val isSending: Boolean = false,
    val isError: Boolean = false
)