package com.laguipemo.nefroped.core.data.dto

import com.laguipemo.nefroped.core.domain.model.course.ExternalLink
import com.laguipemo.nefroped.core.local.room.entity.ExternalLinkEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExternalLinkDto(
    @SerialName("id") val id: String,
    @SerialName("topic_id") val topicId: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String? = null,
    @SerialName("url") val url: String,
    @SerialName("order") val order: Int = 0
) {
    fun toEntity() = ExternalLinkEntity(
        id = id,
        topicId = topicId,
        title = title,
        description = description,
        url = url,
        order = order
    )

    fun toDomain() = ExternalLink(
        id = id,
        topicId = topicId,
        title = title,
        description = description,
        url = url,
        order = order
    )
}
