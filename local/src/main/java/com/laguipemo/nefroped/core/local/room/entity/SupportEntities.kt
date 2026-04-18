package com.laguipemo.nefroped.core.local.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.laguipemo.nefroped.core.domain.model.course.ExternalLink

@Entity(
    tableName = "external_links",
    foreignKeys = [
        ForeignKey(
            entity = TopicEntity::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["topicId"])]
)
data class ExternalLinkEntity(
    @PrimaryKey val id: String,
    val topicId: String,
    val title: String,
    val description: String?,
    val url: String,
    val order: Int
) {
    fun toDomain() = ExternalLink(
        id = id,
        topicId = topicId,
        title = title,
        description = description,
        url = url,
        order = order
    )

    companion object {
        fun fromDomain(link: ExternalLink) = ExternalLinkEntity(
            id = link.id,
            topicId = link.topicId,
            title = link.title,
            description = link.description,
            url = link.url,
            order = link.order
        )
    }
}
