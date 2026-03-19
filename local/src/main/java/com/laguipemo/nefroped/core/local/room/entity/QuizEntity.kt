package com.laguipemo.nefroped.core.local.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "quizzes",
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
data class QuizEntity(
    @PrimaryKey val id: String,
    val topicId: String,
    val title: String
)

@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = QuizEntity::class,
            parentColumns = ["id"],
            childColumns = ["quizId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["quizId"])]
)
data class QuestionEntity(
    @PrimaryKey val id: String,
    val quizId: String,
    val text: String,
    val intro: String?,
    val type: String, // TRUE_FALSE, ONE_CHOICE, MULTIPLE_CHOICE, MATCH_DEFINITION
    val optionsJson: String,
    val correctAnswerJson: String,
    val explanation: String?
)

@Entity(
    tableName = "quiz_results",
    foreignKeys = [
        ForeignKey(
            entity = QuizEntity::class,
            parentColumns = ["id"],
            childColumns = ["quizId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["quizId"])]
)
data class QuizResultEntity(
    @PrimaryKey val quizId: String,
    val score: Float,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val completedAt: Long
)
