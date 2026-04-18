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
    type = when (type) {
        "clinical_cases", "practice" -> TopicType.PRACTICE
        "support" -> TopicType.SUPPORT
        else -> TopicType.THEORY
    },
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
    
    val optionsElem = try { json.parseToJsonElement(optionsJson) } catch(e: Exception) { JsonArray(emptyList()) }
    val answerElem = try { json.parseToJsonElement(correctAnswerJson) } catch(e: Exception) { JsonPrimitive(0) }
    
    val domainOptions = try {
        when (qType) {
            QuestionType.MATCH_DEFINITION -> {
                val obj = optionsElem.jsonObject
                QuestionOptions.Match(
                    terms = obj["terms"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList(),
                    definitions = obj["definitions"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
                )
            }
            else -> QuestionOptions.Simple(optionsElem.jsonArray.map { it.jsonPrimitive.content })
        }
    } catch (e: Exception) {
        QuestionOptions.Simple(emptyList())
    }
    
    val domainAnswer = try {
        when (qType) {
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
    } catch (e: Exception) {
        QuestionAnswer.Single(0)
    }
    
    return Question(id = id, quizId = quizId, text = text, intro = intro, type = qType, options = domainOptions, correctAnswer = domainAnswer, explanation = explanation)
}

internal fun Quiz.toDto() = QuizDto(
    id = id,
    topicId = topicId,
    title = title,
    description = description
)

internal fun Question.toDto(): QuestionDto {
    val json = Json { ignoreUnknownKeys = true }
    
    val optionsElement = when (val opts = options) {
        is QuestionOptions.Simple -> buildJsonArray { opts.list.forEach { add(it) } }
        is QuestionOptions.Match -> buildJsonObject {
            put("terms", buildJsonArray { opts.terms.forEach { add(it) } })
            put("definitions", buildJsonArray { opts.definitions.forEach { add(it) } })
        }
    }
    
    val answerElement = when (val ans = correctAnswer) {
        is QuestionAnswer.Single -> JsonPrimitive(ans.index)
        is QuestionAnswer.Multiple -> buildJsonArray { ans.indices.forEach { add(it) } }
        is QuestionAnswer.Match -> buildJsonObject {
            ans.mapping.forEach { (k, v) -> put(k.toString(), v) }
        }
    }
    
    return QuestionDto(
        id = id,
        quizId = quizId,
        text = text,
        type = type.name,
        options = optionsElement,
        correctAnswer = answerElement,
        explanation = explanation,
        intro = intro
    )
}

internal fun QuizResultEntity.toDomain() = QuizResult(
    quizId = quizId, 
    score = score, 
    correctAnswers = correctAnswers, 
    totalQuestions = totalQuestions, 
    completedAt = completedAt
)
