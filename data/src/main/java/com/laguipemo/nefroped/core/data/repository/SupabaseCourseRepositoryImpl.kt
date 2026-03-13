package com.laguipemo.nefroped.core.data.repository

import com.laguipemo.nefroped.core.domain.model.course.Lesson
import com.laguipemo.nefroped.core.domain.model.course.Topic
import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

class SupabaseCourseRepositoryImpl(
    private val supabase: SupabaseClient
) : CourseRepository {

    override suspend fun getTopics(): List<Topic> {
        val userId = supabase.auth.currentUserOrNull()?.id ?: ""
        
        val topicsDto = supabase.postgrest["topics"].select().decodeList<TopicDto>()
        
        return topicsDto.map { dto ->
            val lessons = supabase.postgrest["lessons"]
                .select {
                    filter {
                        eq("topic_id", dto.id)
                    }
                }.decodeList<LessonDto>()
            
            Topic(
                id = dto.id,
                title = dto.title,
                description = dto.description,
                imageUrl = dto.imageUrl,
                order = dto.order,
                conversationId = dto.conversationId,
                lessonsCount = lessons.size,
                completedLessonsCount = 0 // TODO: Implementar lógica de progreso real
            )
        }
    }

    override suspend fun getLessons(topicId: String): List<Lesson> {
        val lessonsDto = supabase.postgrest["lessons"].select {
            filter {
                eq("topic_id", topicId)
            }
        }.decodeList<LessonDto>()
        
        return lessonsDto.map { it.toDomain() }
    }

    override suspend fun markLessonAsCompleted(lessonId: String): Boolean {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return false
        return try {
            supabase.postgrest["user_progress"].insert(
                UserProgressDto(userId, lessonId)
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun observeTopicProgress(topicId: String): Flow<Float> = flow {
        emit(0f)
    }
}

@Serializable
internal data class TopicDto(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String? = null,
    val order: Int,
    val conversationId: String? = null
)

@Serializable
internal data class LessonDto(
    val id: String,
    val topicId: String,
    val title: String,
    val imageUrl: String? = null,
    val description: String? = null,
    val content: String,
    val order: Int
)

internal fun LessonDto.toDomain(): Lesson = Lesson(
    id = id,
    topicId = topicId,
    title = title,
    imageUrl = imageUrl,
    description = description,
    content = content,
    order = order,
    isCompleted = false
)

@Serializable
internal data class UserProgressDto(
    val user_id: String,
    val lesson_id: String
)
