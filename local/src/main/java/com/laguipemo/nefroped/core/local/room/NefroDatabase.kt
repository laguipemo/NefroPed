package com.laguipemo.nefroped.core.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.laguipemo.nefroped.core.local.room.dao.CourseDao
import com.laguipemo.nefroped.core.local.room.entity.*

@Database(
    entities = [
        TopicEntity::class, 
        LessonEntity::class,
        QuizEntity::class,
        QuestionEntity::class,
        QuizResultEntity::class,
        ClinicalCaseEntity::class,
        ComplementaryResourceEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class NefroDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
}
