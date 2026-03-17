package com.laguipemo.nefroped.core.data.repository

import android.util.Log
import com.laguipemo.nefroped.core.domain.model.course.*
import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository
import com.laguipemo.nefroped.core.local.room.dao.CourseDao
import com.laguipemo.nefroped.core.local.room.entity.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

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

    override fun observeQuizByTopic(topicId: String): Flow<Quiz?> {
        return courseDao.observeQuizWithQuestionsByTopic(topicId).map { it?.toDomain() }
    }

    override fun observeQuizResult(quizId: String): Flow<QuizResult?> {
        return courseDao.observeQuizResult(quizId).map { it?.toDomain() }
    }

    override suspend fun syncTopics(): Result<Unit> {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: ""
            val topicsDto = supabase.postgrest["topics"].select().decodeList<TopicDto>()
            
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
            
            val userProgress = if (userId.isNotEmpty()) {
                supabase.postgrest["user_progress"]
                    .select { filter { eq("user_id", userId) } }
                    .decodeList<UserProgressDto>()
                    .map { it.lesson_id }
                    .toSet()
            } else emptySet()
            
            courseDao.insertLessons(lessonsDto.map { it.toEntity(isCompleted = it.id in userProgress) })
            courseDao.refreshTopicProgress(topicId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CourseRepo", "Error syncing lessons for topic $topicId", e)
            Result.failure(e)
        }
    }

    override suspend fun syncQuiz(topicId: String): Result<Unit> {
        return try {
            // 1. Obtener el Quiz de Supabase
            val quizDto = supabase.postgrest["quizzes"]
                .select { filter { eq("topic_id", topicId) } }
                .decodeSingleOrNull<QuizDto>() ?: return Result.failure(Exception("Quiz not found"))

            // 2. Obtener las preguntas del Quiz
            val questionsDto = supabase.postgrest["questions"]
                .select { filter { eq("quiz_id", quizDto.id) } }
                .decodeList<QuestionDto>()

            // 3. Guardar en Room
            courseDao.insertQuiz(quizDto.toEntity())
            courseDao.insertQuestions(questionsDto.map { it.toEntity() })

            // 4. Intentar sincronizar el resultado previo si existe
            val userId = supabase.auth.currentUserOrNull()?.id
            if (userId != null) {
                val resultDto = supabase.postgrest["quiz_results"]
                    .select {
                        filter { 
                            eq("quiz_id", quizDto.id)
                            eq("user_id", userId)
                        }
                    }
                    .decodeSingleOrNull<QuizResultDto>()
                
                resultDto?.let { courseDao.insertQuizResult(it.toEntity()) }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CourseRepo", "Error syncing quiz for topic $topicId", e)
            Result.failure(e)
        }
    }

    override suspend fun markLessonAsCompleted(lessonId: String): Boolean {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return false
        return try {
            supabase.postgrest["user_progress"].upsert(
                UserProgressDto(userId, lessonId)
            )
            val lesson = courseDao.getLessonById(lessonId)
            courseDao.updateLessonCompletion(lessonId, true)
            lesson?.topicId?.let { topicId ->
                courseDao.refreshTopicProgress(topicId)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun saveQuizResult(result: QuizResult): Boolean {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return false
        return try {
            val dto = QuizResultDto(
                userId = userId,
                quizId = result.quizId,
                score = result.score,
                correctAnswers = result.correctAnswers,
                totalQuestions = result.totalQuestions,
                completedAt = result.completedAt
            )
            
            // 1. Guardar en Supabase
            supabase.postgrest["quiz_results"].upsert(dto)
            
            // 2. Guardar en Room
            courseDao.insertQuizResult(dto.toEntity())
            
            true
        } catch (e: Exception) {
            Log.e("CourseRepo", "Error saving quiz result", e)
            false
        }
    }
}

// --- DTOs ---

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

@Serializable
internal data class QuizDto(
    val id: String,
    @SerialName("topic_id") val topicId: String,
    val title: String
)

@Serializable
internal data class QuestionDto(
    val id: String,
    @SerialName("quiz_id") val quizId: String,
    val text: String,
    val options: List<String>,
    @SerialName("correct_answer_index") val correctAnswerIndex: Int,
    val explanation: String? = null
)

@Serializable
internal data class QuizResultDto(
    @SerialName("user_id") val userId: String,
    @SerialName("quiz_id") val quizId: String,
    val score: Float,
    @SerialName("correct_answers") val correctAnswers: Int,
    @SerialName("total_questions") val totalQuestions: Int,
    @SerialName("completed_at") val completedAt: Long
)

@Serializable
internal data class UserProgressDto(
    val user_id: String,
    val lesson_id: String
)

// --- Mappers ---

internal fun TopicDto.toEntity(lessonsCount: Int, completedCount: Int) = TopicEntity(
    id = id, title = title, description = description, imageUrl = imageUrl,
    imagePlaceholder = imagePlaceholder, contentUrl = contentUrl, indexContent = content,
    order = order, conversationId = conversationId, lessonsCount = lessonsCount,
    completedLessonsCount = completedCount
)

internal fun TopicEntity.toDomain() = Topic(
    id = id, title = title, description = description, imageUrl = imageUrl,
    imagePlaceholder = imagePlaceholder, contentUrl = contentUrl, indexContent = indexContent,
    order = order, conversationId = conversationId, lessonsCount = lessonsCount,
    completedLessonsCount = completedLessonsCount
)

internal fun LessonDto.toEntity(isCompleted: Boolean) = LessonEntity(
    id = id, topicId = topicId, title = title, imageUrl = imageUrl,
    imagePlaceholder = imagePlaceholder, description = description, content = contentUrl,
    videoUrl = videoUrl, audioUrl = audioUrl, pdfUrl = pdfUrl, order = order, isCompleted = isCompleted
)

internal fun LessonEntity.toDomain() = Lesson(
    id = id, topicId = topicId, title = title, imageUrl = imageUrl,
    imagePlaceholder = imagePlaceholder, description = description, contentUrl = content,
    videoUrl = videoUrl, audioUrl = audioUrl, pdfUrl = pdfUrl, order = order, isCompleted = isCompleted
)

internal fun QuizDto.toEntity() = QuizEntity(id = id, topicId = topicId, title = title)

internal fun QuestionDto.toEntity() = QuestionEntity(
    id = id, quizId = quizId, text = text, 
    optionsJson = Json.encodeToString(options),
    correctAnswerIndex = correctAnswerIndex, explanation = explanation
)

internal fun QuizResultDto.toEntity() = QuizResultEntity(
    quizId = quizId, score = score, correctAnswers = correctAnswers,
    totalQuestions = totalQuestions, completedAt = completedAt
)

internal fun QuizWithQuestions.toDomain() = Quiz(
    id = quiz.id,
    topicId = quiz.topicId,
    title = quiz.title,
    questions = questions.map { it.toDomain() }
)

internal fun QuestionEntity.toDomain() = Question(
    id = id,
    quizId = quizId,
    text = text,
    options = Json.decodeFromString<List<String>>(optionsJson).mapIndexed { index, s -> QuestionOption(index.toString(), s) },
    correctAnswerIndex = correctAnswerIndex,
    explanation = explanation
)

internal fun QuizResultEntity.toDomain() = QuizResult(
    quizId = quizId, score = score, correctAnswers = correctAnswers,
    totalQuestions = totalQuestions, completedAt = completedAt
)
