package com.laguipemo.nefroped.core.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.laguipemo.nefroped.core.local.room.dao.CourseDao
import com.laguipemo.nefroped.core.local.room.entity.LessonEntity
import com.laguipemo.nefroped.core.local.room.entity.TopicEntity

@Database(
    entities = [TopicEntity::class, LessonEntity::class],
    version = 2,
    exportSchema = false
)
abstract class NefroDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
}
