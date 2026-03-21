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
import kotlinx.serialization.json.*
import kotlinx.datetime.Instant

class SupabaseCourseRepositoryImpl(
    private val supabase: SupabaseClient,
    private val courseDao: CourseDao
) : CourseRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun observeTopics(): Flow<List<Topic>> = courseDao.observeTopics().map { entities -> entities.map { it.toDomain() } }
    override fun observeLessons(topicId: String): Flow<List<Lesson>> = courseDao.observeLessonsByTopic(topicId).map { entities -> entities.map { it.toDomain() } }
    override fun observeLesson(lessonId: String): Flow<Lesson?> = courseDao.observeLessonById(lessonId).map { it?.toDomain() }

    override fun observeQuizByTopic(topicId: String): Flow<Quiz?> = courseDao.observeQuizWithQuestionsByTopic(topicId).map { it?.toDomain() }

    override fun observeQuizById(quizId: String): Flow<Quiz?> =
        courseDao.observeQuizWithQuestionsById(quizId).map { it?.toDomain() }

    override fun observeQuizResult(quizId: String): Flow<QuizResult?> = courseDao.observeQuizResult(quizId).map { it?.toDomain() }

    override fun observeClinicalCases(topicId: String): Flow<List<ClinicalCase>> =
        courseDao.observeClinicalCases(topicId).map { entities -> entities.map { it.toDomain() } }

    override fun observeComplementaryResources(topicId: String): Flow<List<ComplementaryResource>> =
        courseDao.observeComplementaryResources(topicId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun syncTopics(): Result<Unit> {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: ""
            val topicsDto = supabase.postgrest["topics"].select().decodeList<TopicDto>()
            val userProgress = if (userId.isNotEmpty()) {
                supabase.postgrest["user_progress"].select { filter { eq("user_id", userId) } }.decodeList<UserProgressDto>().map { it.lesson_id }.toSet()
            } else emptySet()
            val entities = topicsDto.map { dto ->
                val lessons = supabase.postgrest["lessons"].select { filter { eq("topic_id", dto.id) } }.decodeList<LessonDto>()
                dto.toEntity(lessonsCount = lessons.size, completedCount = lessons.count { it.id in userProgress })
            }
            courseDao.insertTopics(entities)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun syncLessons(topicId: String): Result<Unit> {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: ""
            val lessonsDto = supabase.postgrest["lessons"].select { filter { eq("topic_id", topicId) } }.decodeList<LessonDto>()
            val userProgress = if (userId.isNotEmpty()) {
                supabase.postgrest["user_progress"].select { filter { eq("user_id", userId) } }.decodeList<UserProgressDto>().map { it.lesson_id }.toSet()
            } else emptySet()
            courseDao.insertLessons(lessonsDto.map { it.toEntity(isCompleted = it.id in userProgress) })
            courseDao.refreshTopicProgress(topicId)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun syncQuiz(topicId: String): Result<Unit> {
        return try {
            val quizDto = supabase.postgrest["quizzes"].select { filter { eq("topic_id", topicId) } }.decodeSingleOrNull<QuizDto>() ?: return Result.failure(Exception("Quiz not found"))
            syncQuestionsAndResult(quizDto)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun syncQuizById(quizId: String): Result<Unit> {
        return try {
            val quizDto = supabase.postgrest["quizzes"].select { filter { eq("id", quizId) } }.decodeSingleOrNull<QuizDto>() ?: return Result.failure(Exception("Quiz not found"))
            syncQuestionsAndResult(quizDto)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    private suspend fun syncQuestionsAndResult(quizDto: QuizDto) {
        val questionsDto = supabase.postgrest["questions"].select { filter { eq("quiz_id", quizDto.id) } }.decodeList<QuestionDto>()
        courseDao.insertQuiz(quizDto.toEntity())
        courseDao.insertQuestions(questionsDto.map { it.toEntity() })

        val userId = supabase.auth.currentUserOrNull()?.id
        if (userId != null) {
            val resultDto = supabase.postgrest["quiz_results"].select {
                filter { eq("quiz_id", quizDto.id); eq("user_id", userId) }
            }.decodeSingleOrNull<QuizResultDto>()

            resultDto?.let { dto ->
                val timestamp = try { Instant.parse(dto.completedAt).toEpochMilliseconds() } catch(e: Exception) { System.currentTimeMillis() }
                courseDao.insertQuizResult(QuizResultEntity(quizId = dto.quizId, score = dto.score, correctAnswers = dto.correctAnswers, totalQuestions = dto.totalQuestions, completedAt = timestamp))
            }
        }
    }

    override suspend fun syncClinicalData(topicId: String): Result<Unit> {
        return try {
            val casesDto = supabase.postgrest["clinical_cases"].select { filter { eq("topic_id", topicId) } }.decodeList<ClinicalCaseDto>()
            val resourcesDto = supabase.postgrest["complementary_resources"].select { filter { eq("topic_id", topicId) } }.decodeList<ComplementaryResourceDto>()
            courseDao.insertClinicalCases(casesDto.map { it.toEntity() })
            courseDao.insertComplementaryResources(resourcesDto.map { it.toEntity() })
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun markLessonAsCompleted(lessonId: String): Boolean {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return false
        return try {
            supabase.postgrest["user_progress"].upsert(UserProgressDto(userId, lessonId))
            val lesson = courseDao.getLessonById(lessonId)
            courseDao.updateLessonCompletion(lessonId, true)
            lesson?.topicId?.let { courseDao.refreshTopicProgress(it) }
            true
        } catch (e: Exception) { false }
    }

    override suspend fun saveQuizResult(result: QuizResult): Boolean {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return false
        return try {
            val instant = Instant.fromEpochMilliseconds(result.completedAt)
            val dto = QuizResultDto(userId = userId, quizId = result.quizId, score = result.score, correctAnswers = result.correctAnswers, totalQuestions = result.totalQuestions, completedAt = instant.toString())
            supabase.postgrest["quiz_results"].upsert(dto)
            courseDao.insertQuizResult(QuizResultEntity(quizId = dto.quizId, score = dto.score, correctAnswers = dto.correctAnswers, totalQuestions = dto.totalQuestions, completedAt = result.completedAt))
            true
        } catch (e: Exception) {
            Log.e("CourseRepo", "Error saving quiz result", e)
            false
        }
    }
}

@Serializable
internal data class TopicDto(val id: String, val title: String, val description: String, @SerialName("image_url") val imageUrl: String? = null, @SerialName("image_placeholder") val imagePlaceholder: String? = null, @SerialName("content_url") val contentUrl: String? = null, val content: String? = null, val order: Int, val type: String? = "lessons", @SerialName("conversation_id") val conversationId: String? = null)
@Serializable
internal data class LessonDto(val id: String, @SerialName("topic_id") val topicId: String, val title: String, @SerialName("image_url") val imageUrl: String? = null, @SerialName("image_placeholder") val imagePlaceholder: String? = null, val description: String? = null, @SerialName("content_url") val contentUrl: String, @SerialName("video_url") val videoUrl: String? = null, @SerialName("audio_url") val audioUrl: String? = null, @SerialName("pdf_url") val pdfUrl: String? = null, val order: Int)
@Serializable
internal data class QuizDto(val id: String, @SerialName("topic_id") val topicId: String, val title: String, val description: String? = null)
@Serializable
internal data class QuestionDto(val id: String, @SerialName("quiz_id") val quizId: String, val text: String, val type: String, val options: JsonElement, @SerialName("correct_answer") val correctAnswer: JsonElement, val explanation: String? = null, val intro: String? = null)
@Serializable
internal data class QuizResultDto( @SerialName("user_id") val userId: String, @SerialName("quiz_id") val quizId: String, val score: Float, @SerialName("correct_answers") val correctAnswers: Int, @SerialName("total_questions") val totalQuestions: Int, @SerialName("completed_at") val completedAt: String)
@Serializable
internal data class ClinicalCaseDto(val id: String, @SerialName("topic_id") val topicId: String, val title: String, val description: String, @SerialName("image_url") val imageUrl: String? = null, @SerialName("quiz_id") val quizId: String? = null)
@Serializable
internal data class ComplementaryResourceDto(val id: String, @SerialName("topic_id") val topicId: String, val title: String, val url: String)
@Serializable
internal data class UserProgressDto(val user_id: String, val lesson_id: String)

internal fun TopicDto.toEntity(lessonsCount: Int, completedCount: Int) = TopicEntity(id = id, title = title, description = description, imageUrl = imageUrl, imagePlaceholder = imagePlaceholder, contentUrl = contentUrl, indexContent = content, order = order, type = type ?: "lessons", conversationId = conversationId, lessonsCount = lessonsCount, completedLessonsCount = completedCount)
internal fun TopicEntity.toDomain() = Topic(id = id, title = title, description = description, imageUrl = imageUrl, imagePlaceholder = imagePlaceholder, contentUrl = contentUrl, indexContent = indexContent, order = order, type = if (type == "clinical_cases") TopicType.CLINICAL_CASES else TopicType.LESSONS, conversationId = conversationId, lessonsCount = lessonsCount, completedLessonsCount = completedLessonsCount)
internal fun LessonDto.toEntity(isCompleted: Boolean) = LessonEntity(id = id, topicId = topicId, title = title, imageUrl = imageUrl, imagePlaceholder = imagePlaceholder, description = description, content = contentUrl, videoUrl = videoUrl, audioUrl = audioUrl, pdfUrl = pdfUrl, order = order, isCompleted = isCompleted)
internal fun LessonEntity.toDomain() = Lesson(id = id, topicId = topicId, title = title, imageUrl = imageUrl, imagePlaceholder = imagePlaceholder, description = description, contentUrl = content, videoUrl = videoUrl, audioUrl = audioUrl, pdfUrl = pdfUrl, order = order, isCompleted = isCompleted)
internal fun QuizDto.toEntity() = QuizEntity(id = id, topicId = topicId, title = title, description = description)
internal fun QuestionDto.toEntity() = QuestionEntity(id = id, quizId = quizId, text = text, intro = intro, type = type, optionsJson = options.toString(), correctAnswerJson = correctAnswer.toString(), explanation = explanation)
internal fun ClinicalCaseDto.toEntity() = ClinicalCaseEntity(id = id, topicId = topicId, title = title, description = description, imageUrl = imageUrl, quizId = quizId)
internal fun ClinicalCaseEntity.toDomain() = ClinicalCase(id = id, topicId = topicId, title = title, description = description, imageUrl = imageUrl, quizId = quizId)
internal fun ComplementaryResourceDto.toEntity() = ComplementaryResourceEntity(id = id, topicId = topicId, title = title, url = url)
internal fun ComplementaryResourceEntity.toDomain() = ComplementaryResource(id = id, topicId = topicId, title = title, url = url)
internal fun QuizResultDto.toEntity(localTimestamp: Long) = QuizResultEntity(quizId = quizId, score = score, correctAnswers = correctAnswers, totalQuestions = totalQuestions, completedAt = localTimestamp)
internal fun QuizWithQuestions.toDomain() = Quiz(id = quiz.id, topicId = quiz.topicId, title = quiz.title, description = quiz.description, questions = questions.map { it.toDomain() })
internal fun QuestionEntity.toDomain(): Question {
    val qType = QuestionType.valueOf(type.uppercase()); val optionsElem = Json.parseToJsonElement(optionsJson); val answerElem = Json.parseToJsonElement(correctAnswerJson)
    val domainOptions = when (qType) {
        QuestionType.MATCH_DEFINITION -> {
            val obj = optionsElem.jsonObject
            QuestionOptions.Match(terms = obj["terms"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList(), definitions = obj["definitions"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList())
        }
        else -> QuestionOptions.Simple(optionsElem.jsonArray.map { it.jsonPrimitive.content })
    }
    val domainAnswer = when (qType) {
        QuestionType.TRUE_FALSE, QuestionType.ONE_CHOICE -> QuestionAnswer.Single(answerElem.jsonPrimitive.int)
        QuestionType.MULTIPLE_CHOICE -> QuestionAnswer.Multiple(answerElem.jsonArray.map { it.jsonPrimitive.int })
        QuestionType.MATCH_DEFINITION -> QuestionAnswer.Match(answerElem.jsonObject.map { it.key.toInt() to it.value.jsonPrimitive.int }.toMap())
    }
    return Question(id = id, quizId = quizId, text = text, intro = intro, type = qType, options = domainOptions, correctAnswer = domainAnswer, explanation = explanation)
}
internal fun QuizResultEntity.toDomain() = QuizResult(quizId = quizId, score = score, correctAnswers = correctAnswers, totalQuestions = totalQuestions, completedAt = completedAt)