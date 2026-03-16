package com.laguipemo.nefroped.core.data.repository

import android.util.Log
import com.laguipemo.nefroped.core.domain.model.course.Lesson
import com.laguipemo.nefroped.core.domain.model.course.Topic
import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository
import com.laguipemo.nefroped.core.local.room.dao.CourseDao
import com.laguipemo.nefroped.core.local.room.entity.LessonEntity
import com.laguipemo.nefroped.core.local.room.entity.TopicEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SupabaseCourseRepositoryImpl(
    private val supabase: SupabaseClient,
    private val courseDao: CourseDao
) : CourseRepository {

    override fun observeTopics(): Flow<List<Topic>> {
        return courseDao.observeTopics().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeLessons(topicId: String): Flow<List<Lesson>> {
        return courseDao.observeLessonsByTopic(topicId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeLesson(lessonId: String): Flow<Lesson?> {
        return courseDao.observeLessonById(lessonId).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun syncTopics(): Result<Unit> {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: ""
            val topicsDto = supabase.postgrest["topics"].select().decodeList<TopicDto>()
            
            // Obtenemos todo el progreso del usuario para este curso
            val userProgress = if (userId.isNotEmpty()) {
                supabase.postgrest["user_progress"]
                    .select { filter { eq("user_id", userId) } }
                    .decodeList<UserProgressDto>()
                    .map { it.lesson_id }
                    .toSet()
            } else emptySet()

            val entities = topicsDto.map { dto ->
                val lessons = supabase.postgrest["lessons"]
                    .select { filter { eq("topic_id", dto.id) } }
                    .decodeList<LessonDto>()
                
                // Contamos cuántas lecciones de este tema están completadas localmente
                val completedCount = lessons.count { it.id in userProgress }
                
                dto.toEntity(
                    lessonsCount = lessons.size,
                    completedCount = completedCount
                )
            }
            
            courseDao.insertTopics(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CourseRepo", "Error syncing topics", e)
            Result.failure(e)
        }
    }

    override suspend fun syncLessons(topicId: String): Result<Unit> {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: ""
            val lessonsDto = supabase.postgrest["lessons"].select {
                filter { eq("topic_id", topicId) }
            }.decodeList<LessonDto>()
            
            // Obtenemos progreso específico
            val userProgress = if (userId.isNotEmpty()) {
                supabase.postgrest["user_progress"]
                    .select { filter { eq("user_id", userId) } }
                    .decodeList<UserProgressDto>()
                    .map { it.lesson_id }
                    .toSet()
            } else emptySet()
            
            courseDao.insertLessons(lessonsDto.map { it.toEntity(isCompleted = it.id in userProgress) })
            
            // Actualizar el conteo en el tema después de insertar lecciones (por si el sync de temas fue incompleto)
            courseDao.refreshTopicProgress(topicId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CourseRepo", "Error syncing lessons for topic $topicId", e)
            Result.failure(e)
        }
    }

    override suspend fun markLessonAsCompleted(lessonId: String): Boolean {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return false
        return try {
            // 1. Persistir en Supabase
            supabase.postgrest["user_progress"].upsert(
                UserProgressDto(userId, lessonId)
            )
            
            // 2. Obtener la lección para saber a qué tema pertenece antes de actualizar
            val lesson = courseDao.getLessonById(lessonId)
            
            // 3. Actualizar la lección localmente
            courseDao.updateLessonCompletion(lessonId, true)
            
            // 4. Recalcular el progreso del tema localmente
            lesson?.topicId?.let { topicId ->
                courseDao.refreshTopicProgress(topicId)
            }
            
            true
        } catch (e: Exception) {
            Log.e("CourseRepo", "Error marking lesson as completed", e)
            false
        }
    }
}

@Serializable
internal data class TopicDto(
    val id: String,
    val title: String,
    val description: String,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("image_placeholder") val imagePlaceholder: String? = null,
    @SerialName("content_url") val contentUrl: String? = null,
    val content: String? = null, 
    val order: Int,
    @SerialName("conversation_id") val conversationId: String? = null
)

internal fun TopicDto.toEntity(lessonsCount: Int, completedCount: Int) = TopicEntity(
    id = id,
    title = title,
    description = description,
    imageUrl = imageUrl,
    imagePlaceholder = imagePlaceholder,
    contentUrl = contentUrl,
    indexContent = content,
    order = order,
    conversationId = conversationId,
    lessonsCount = lessonsCount,
    completedLessonsCount = completedCount
)

internal fun TopicEntity.toDomain() = Topic(
    id = id,
    title = title,
    description = description,
    imageUrl = imageUrl,
    imagePlaceholder = imagePlaceholder,
    contentUrl = contentUrl,
    indexContent = indexContent,
    order = order,
    conversationId = conversationId,
    lessonsCount = lessonsCount,
    completedLessonsCount = completedLessonsCount
)

@Serializable
internal data class LessonDto(
    val id: String,
    @SerialName("topic_id") val topicId: String,
    val title: String,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("image_placeholder") val imagePlaceholder: String? = null,
    val description: String? = null,
    @SerialName("content_url") val contentUrl: String,
    @SerialName("video_url") val videoUrl: String? = null,
    @SerialName("audio_url") val audioUrl: String? = null,
    @SerialName("pdf_url") val pdfUrl: String? = null,
    val order: Int
)

internal fun LessonDto.toEntity(isCompleted: Boolean) = LessonEntity(
    id = id,
    topicId = topicId,
    title = title,
    imageUrl = imageUrl,
    imagePlaceholder = imagePlaceholder,
    description = description,
    content = contentUrl,
    videoUrl = videoUrl,
    audioUrl = audioUrl,
    pdfUrl = pdfUrl,
    order = order,
    isCompleted = isCompleted
)

internal fun LessonEntity.toDomain() = Lesson(
    id = id,
    topicId = topicId,
    title = title,
    imageUrl = imageUrl,
    imagePlaceholder = imagePlaceholder,
    description = description,
    contentUrl = content,
    videoUrl = videoUrl,
    audioUrl = audioUrl,
    pdfUrl = pdfUrl,
    order = order,
    isCompleted = isCompleted
)

@Serializable
internal data class UserProgressDto(
    val user_id: String,
    val lesson_id: String
)
