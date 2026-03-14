package com.laguipemo.nefroped.core.data.repository

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

    override suspend fun syncTopics(): Result<Unit> {
        return try {
            val topicsDto = supabase.postgrest["topics"].select().decodeList<TopicDto>()
            
            val entities = topicsDto.map { dto ->
                val lessons = supabase.postgrest["lessons"]
                    .select {
                        filter { eq("topic_id", dto.id) }
                    }.decodeList<LessonDto>()
                
                dto.toEntity(lessonsCount = lessons.size)
            }
            
            courseDao.insertTopics(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncLessons(topicId: String): Result<Unit> {
        return try {
            val lessonsDto = supabase.postgrest["lessons"].select {
                filter { eq("topic_id", topicId) }
            }.decodeList<LessonDto>()
            
            courseDao.insertLessons(lessonsDto.map { it.toEntity() })
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markLessonAsCompleted(lessonId: String): Boolean {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return false
        return try {
            supabase.postgrest["user_progress"].insert(
                UserProgressDto(userId, lessonId)
            )
            courseDao.updateLessonCompletion(lessonId, true)
            true
        } catch (e: Exception) {
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

internal fun TopicDto.toEntity(lessonsCount: Int) = TopicEntity(
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
    completedLessonsCount = 0
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

internal fun LessonDto.toEntity() = LessonEntity(
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
    isCompleted = false
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
