package com.laguipemo.nefroped.core.local.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "clinical_cases",
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
data class ClinicalCaseEntity(
    @PrimaryKey val id: String,
    val topicId: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val quizId: String?
)

@Entity(
    tableName = "complementary_resources",
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
data class ComplementaryResourceEntity(
    @PrimaryKey val id: String,
    val topicId: String,
    val title: String,
    val url: String
)
