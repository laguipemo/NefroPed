package com.laguipemo.nefroped.core.domain.model.course

data class ExternalLink(
    val id: String,
    val topicId: String,
    val title: String,
    val description: String?,
    val url: String,
    val order: Int = 0
)
