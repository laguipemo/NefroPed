package com.laguipemo.nefroped.core.local.room.dao

import androidx.room.*
import com.laguipemo.nefroped.core.local.room.entity.LessonEntity
import com.laguipemo.nefroped.core.local.room.entity.TopicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {

    // Temas
    @Query("SELECT * FROM topics ORDER BY `order` ASC")
    fun observeTopics(): Flow<List<TopicEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<TopicEntity>)

    // Lecciones
    @Query("SELECT * FROM lessons WHERE topicId = :topicId ORDER BY `order` ASC")
    fun observeLessonsByTopic(topicId: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    fun observeLessonById(lessonId: String): Flow<LessonEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonEntity>)

    @Query("UPDATE lessons SET isCompleted = :completed WHERE id = :lessonId")
    suspend fun updateLessonCompletion(lessonId: String, completed: Boolean)

    // Limpieza
    @Query("DELETE FROM topics")
    suspend fun clearTopics()

    @Query("DELETE FROM lessons")
    suspend fun clearLessons()
}
