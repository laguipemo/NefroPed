package com.laguipemo.nefroped.core.data.mapper

import com.laguipemo.nefroped.core.data.dto.*
import com.laguipemo.nefroped.core.domain.model.course.*
import com.laguipemo.nefroped.core.local.room.entity.*
import kotlinx.serialization.json.*

internal fun TopicDto.toEntity(lessonsCount: Int, completedCount: Int, downloadedIndexContent: String?) = TopicEntity(
    id = id, 
    title = title, 
    description = description, 
    imageUrl = imageUrl, 
    imagePlaceholder = imagePlaceholder, 
    contentUrl = contentUrl, 
    indexContent = downloadedIndexContent, 
    order = order, 
    type = type ?: "lessons", 
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
    type = if (type == "clinical_cases") TopicType.CLINICAL_CASES else TopicType.LESSONS, 
    conversationId = conversationId, 
    lessonsCount = lessonsCount, 
    completedLessonsCount = completedLessonsCount
)

internal fun LessonDto.toEntity(isCompleted: Boolean, downloadedContent: String) = LessonEntity(
    id = id, 
    topicId = topicId, 
    title = title, 
    imageUrl = imageUrl, 
    imagePlaceholder = imagePlaceholder, 
    description = description, 
    content = downloadedContent, 
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
    content = content, 
    videoUrl = videoUrl, 
    audioUrl = audioUrl, 
    pdfUrl = pdfUrl, 
    order = order, 
    isCompleted = isCompleted
)

internal fun QuizDto.toEntity() = QuizEntity(id = id, topicId = topicId, title = title, description = description)

internal fun QuestionDto.toEntity() = QuestionEntity(
    id = id, 
    quizId = quizId, 
    text = text, 
    intro = intro, 
    type = type, 
    optionsJson = options.toString(), 
    correctAnswerJson = correctAnswer.toString(), 
    explanation = explanation
)

internal fun ClinicalCaseDto.toEntity() = ClinicalCaseEntity(
    id = id, 
    topicId = topicId, 
    title = title, 
    description = description, 
    imageUrl = imageUrl, 
    quizId = quizId
)

internal fun ClinicalCaseEntity.toDomain() = ClinicalCase(
    id = id, 
    topicId = topicId, 
    title = title, 
    description = description, 
    imageUrl = imageUrl, 
    quizId = quizId
)

internal fun ComplementaryResourceDto.toEntity() = ComplementaryResourceEntity(id = id, topicId = topicId, title = title, url = url)

internal fun ComplementaryResourceEntity.toDomain() = ComplementaryResource(id = id, topicId = topicId, title = title, url = url)

internal fun QuizResultDto.toEntity(localTimestamp: Long) = QuizResultEntity(
    quizId = quizId, 
    score = score, 
    correctAnswers = correctAnswers, 
    totalQuestions = totalQuestions, 
    completedAt = localTimestamp
)

internal fun QuizWithQuestions.toDomain() = Quiz(
    id = quiz.id, 
    topicId = quiz.topicId, 
    title = quiz.title, 
    description = quiz.description, 
    questions = questions.map { it.toDomain() }
)

internal fun QuestionEntity.toDomain(): Question {
    val json = Json { ignoreUnknownKeys = true }
    val qType = try { QuestionType.valueOf(type.uppercase()) } catch(e: Exception) { QuestionType.ONE_CHOICE }
    val optionsElem = json.parseToJsonElement(optionsJson)
    val answerElem = json.parseToJsonElement(correctAnswerJson)
    
    val domainOptions = when (qType) {
        QuestionType.MATCH_DEFINITION -> {
            val obj = optionsElem.jsonObject
            QuestionOptions.Match(
                terms = obj["terms"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList(),
                definitions = obj["definitions"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
            )
        }
        else -> QuestionOptions.Simple(optionsElem.jsonArray.map { it.jsonPrimitive.content })
    }
    
    val domainAnswer = when (qType) {
        QuestionType.TRUE_FALSE, QuestionType.ONE_CHOICE -> {
            val idx = try { answerElem.jsonPrimitive.int } catch(e: Exception) { answerElem.jsonPrimitive.content.toIntOrNull() ?: 0 }
            QuestionAnswer.Single(idx)
        }
        QuestionType.MULTIPLE_CHOICE -> {
            val indices = try { 
                answerElem.jsonArray.map { it.jsonPrimitive.int } 
            } catch(e: Exception) { 
                answerElem.jsonArray.mapNotNull { it.jsonPrimitive.content.toIntOrNull() } 
            }
            QuestionAnswer.Multiple(indices)
        }
        QuestionType.MATCH_DEFINITION -> {
            val mapping = answerElem.jsonObject.map { (key, value) ->
                val termIdx = key.toIntOrNull() ?: 0
                val defIdx = try { value.jsonPrimitive.int } catch(e: Exception) { value.jsonPrimitive.content.toIntOrNull() ?: 0 }
                termIdx to defIdx
            }.toMap()
            QuestionAnswer.Match(mapping)
        }
    }
    
    return Question(id = id, quizId = quizId, text = text, intro = intro, type = qType, options = domainOptions, correctAnswer = domainAnswer, explanation = explanation)
}

internal fun QuizResultEntity.toDomain() = QuizResult(
    quizId = quizId, 
    score = score, 
    correctAnswers = correctAnswers, 
    totalQuestions = totalQuestions, 
    completedAt = completedAt
)
