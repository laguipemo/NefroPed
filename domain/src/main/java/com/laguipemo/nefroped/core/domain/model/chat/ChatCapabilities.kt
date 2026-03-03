package com.laguipemo.nefroped.core.domain.model.chat

data class ChatCapabilities(
    val canPersistHistory: Boolean,
    val canExport: Boolean,
    val canUseAdvancedModel: Boolean,
    val canAttachFiles: Boolean,
    val messageLimit: Int? // null = ilimitado
)
