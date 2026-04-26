package com.laguipemo.nefroped.core.data.repository

import android.util.Log
import com.laguipemo.nefroped.core.data.dto.*
import com.laguipemo.nefroped.core.data.mapper.*
import com.laguipemo.nefroped.core.domain.model.course.*
import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository
import com.laguipemo.nefroped.core.local.room.dao.CourseDao
import com.laguipemo.nefroped.core.local.room.dao.SupportDao
import com.laguipemo.nefroped.core.local.room.entity.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

class SupabaseCourseRepositoryImpl(
    private val supabase: SupabaseClient,
    private val courseDao: CourseDao,
    private val supportDao: SupportDao,
    private val httpClient: HttpClient
) : CourseRepository {

    override fun observeTopics(): Flow<List<Topic>> = 
        courseDao.observeTopics().map { entities -> entities.map { it.toDomain() } }

    override fun observeTopicsAdmin(): Flow<List<Topic>> = observeTopics()

    override fun observeTopic(id: String): Flow<Topic?> = 
        courseDao.observeTopics().map { list -> list.find { it.id == id }?.toDomain() }

    override fun observeLessons(topicId: String): Flow<List<Lesson>> = 
        courseDao.observeLessonsByTopic(topicId).map { entities -> entities.map { it.toDomain() } }

    override fun observeLesson(lessonId: String): Flow<Lesson?> = 
        courseDao.observeLessonById(lessonId).map { it?.toDomain() }

    override fun observeQuizByTopic(topicId: String): Flow<Quiz?> = 
        courseDao.observeQuizWithQuestionsByTopic(topicId).map { it?.toDomain() }

    override fun observeQuizById(quizId: String): Flow<Quiz?> =
        courseDao.observeQuizWithQuestionsById(quizId).map { it?.toDomain() }

    override fun observeQuizResult(quizId: String): Flow<QuizResult?> = 
        courseDao.observeQuizResult(quizId).map { it?.toDomain() }

    override fun observeAllQuizResults(): Flow<List<QuizResult>> =
        courseDao.observeAllQuizResults().map { entities -> entities.map { it.toDomain() } }

    override fun observeClinicalCases(topicId: String): Flow<List<ClinicalCase>> =
        courseDao.observeClinicalCases(topicId).map { entities -> entities.map { it.toDomain() } }

    override fun observeComplementaryResources(topicId: String): Flow<List<ComplementaryResource>> =
        courseDao.observeComplementaryResources(topicId).map { entities -> entities.map { it.toDomain() } }

    override fun observeExternalLinks(topicId: String): Flow<List<ExternalLink>> =
        supportDao.observeExternalLinks(topicId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun syncTopics(): Result<Unit> {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: ""
            val topicsDto = supabase.from("topics").select().decodeList<TopicDto>()
            val userProgress = if (userId.isNotEmpty()) {
                supabase.from("user_progress").select { filter { eq("user_id", userId) } }.decodeList<UserProgressDto>().map { it.lessonId }.toSet()
            } else emptySet()
            
            val entities = topicsDto.map { dto ->
                val lessons = supabase.from("lessons").select { filter { eq("topic_id", dto.id) } }.decodeList<LessonDto>()
                
                val indexContent = dto.contentUrl?.let { url ->
                    try {
                        httpClient.get(url).bodyAsText()
                    } catch (e: Exception) {
                        Log.e("CourseRepo", "Error downloading topic markdown: ${dto.title}", e)
                        null
                    }
                }
                
                dto.toEntity(
                    lessonsCount = lessons.size, 
                    completedCount = lessons.count { it.id in userProgress },
                    downloadedIndexContent = indexContent
                )
            }
            courseDao.insertTopics(entities)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun syncLessons(topicId: String): Result<Unit> {
        return try {
            Log.d("CourseRepo", "Sincronizando lecciones para topicId: $topicId")
            val userId = supabase.auth.currentUserOrNull()?.id ?: ""
            val lessonsDto = supabase.from("lessons").select { filter { eq("topic_id", topicId) } }.decodeList<LessonDto>()
            Log.d("CourseRepo", "Lecciones encontradas en Supabase: ${lessonsDto.size}")

            val userProgress = if (userId.isNotEmpty()) {
                supabase.from("user_progress").select { filter { eq("user_id", userId) } }.decodeList<UserProgressDto>().map { it.lessonId }.toSet()
            } else emptySet()
            
            val entities = lessonsDto.map { dto ->
                val contentText = if (dto.contentUrl.isNotEmpty()) {
                    try {
                        httpClient.get(dto.contentUrl).bodyAsText()
                    } catch (e: Exception) {
                        Log.e("CourseRepo", "Error downloading lesson markdown from: ${dto.contentUrl}", e)
                        ""
                    }
                } else {
                    Log.w("CourseRepo", "Lesson ${dto.title} has empty contentUrl")
                    ""
                }
                
                dto.toEntity(
                    isCompleted = dto.id in userProgress,
                    downloadedContent = contentText
                )
            }
            
            courseDao.insertLessons(entities)
            courseDao.refreshTopicProgress(topicId)
            Result.success(Unit)
        } catch (e: Exception) { 
            Log.e("CourseRepo", "Error en syncLessons", e)
            Result.failure(e) 
        }
    }

    override suspend fun syncQuiz(topicId: String): Result<Unit> {
        return try {
            Log.d("CourseRepo", "Sincronizando Quiz para topicId: $topicId")
            val quizDto = supabase.from("quizzes").select { 
                filter { eq("topic_id", topicId) } 
            }.decodeSingleOrNull<QuizDto>()
            
            if (quizDto == null) {
                Log.d("CourseRepo", "No se encontró Quiz en Supabase para el tema $topicId. Sincronización finalizada (vío).")
                // No es un error, simplemente no hay quiz para este tema aún
                return Result.success(Unit)
            }
            
            Log.d("CourseRepo", "Quiz encontrado: ${quizDto.title} (ID: ${quizDto.id})")
            syncQuestionsAndResult(quizDto)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CourseRepo", "Error sincronizando Quiz para $topicId", e)
            Result.failure(e)
        }
    }

    override suspend fun syncQuizById(quizId: String): Result<Unit> {
        return try {
            Log.d("CourseRepo", "Sincronizando Quiz por ID: $quizId")
            val quizDto = supabase.from("quizzes").select { 
                filter { eq("id", quizId) } 
            }.decodeSingleOrNull<QuizDto>()
            
            if (quizDto == null) {
                Log.d("CourseRepo", "No se encontró Quiz con ID $quizId")
                return Result.success(Unit)
            }

            syncQuestionsAndResult(quizDto)
            Result.success(Unit)
        } catch (e: Exception) { 
            Log.e("CourseRepo", "Error sincronizando Quiz por ID $quizId", e)
            Result.failure(e) 
        }
    }

    private suspend fun syncQuestionsAndResult(quizDto: QuizDto) {
        Log.d("CourseRepo", "Buscando preguntas para quizId: ${quizDto.id}")
        val questionsDto = supabase.from("questions").select { 
            filter { eq("quiz_id", quizDto.id) } 
        }.decodeList<QuestionDto>()
        
        Log.d("CourseRepo", "Preguntas encontradas en Supabase: ${questionsDto.size}")
        
        // Limpiar datos antiguos del mismo tema para evitar conflictos
        courseDao.deleteQuizByTopic(quizDto.topicId)
        
        // Insertar el nuevo Quiz y sus preguntas
        courseDao.insertQuiz(quizDto.toEntity())
        
        // Asegurar que las preguntas están vinculadas al ID de quiz correcto
        val questionEntities = questionsDto.map { it.toEntity().copy(quizId = quizDto.id) }
        courseDao.insertQuestions(questionEntities)
        
        Log.d("CourseRepo", "Quiz y ${questionEntities.size} preguntas insertados en Room exitosamente")

        val userId = supabase.auth.currentUserOrNull()?.id
        if (userId != null) {
            // ... resto del código de resultados
            val resultDto = supabase.from("quiz_results").select {
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
            Log.d("CourseRepo", "Sincronizando datos clínicos para topic: $topicId")
            val casesDto = supabase.from("clinical_cases").select { filter { eq("topic_id", topicId) } }.decodeList<ClinicalCaseDto>()
            Log.d("CourseRepo", "Casos clínicos recibidos de Supabase: ${casesDto.size}")
            
            val resourcesDto = supabase.from("complementary_resources").select { filter { eq("topic_id", topicId) } }.decodeList<ComplementaryResourceDto>()
            Log.d("CourseRepo", "Recursos complementarios recibidos: ${resourcesDto.size}")
            
            val entities = casesDto.map { it.toEntity() }
            courseDao.insertClinicalCases(entities)
            
            val resourceEntities = resourcesDto.map { it.toEntity() }
            courseDao.insertComplementaryResources(resourceEntities)
            
            Log.d("CourseRepo", "Sincronización clínica completada exitosamente en Room")
            Result.success(Unit)
        } catch (e: Exception) { 
            Log.e("CourseRepo", "Error sincronizando datos clínicos para $topicId", e)
            Result.failure(e) 
        }
    }

    override suspend fun syncExternalLinks(topicId: String): Result<Unit> {
        return try {
            // Nota: El DTO ExternalLinkDto debe ser creado en el módulo :data
            // Por ahora usamos una consulta genérica si no tenemos el DTO aún
            val links = supabase.from("external_links")
                .select { filter { eq("topic_id", topicId) } }
                .decodeList<ExternalLinkDto>()
            
            supportDao.insertExternalLinks(links.map { it.toEntity() })
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CourseRepo", "Error syncing external links", e)
            Result.failure(e)
        }
    }

    override suspend fun markLessonAsCompleted(lessonId: String): Boolean {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return false
        return try {
            supabase.from("user_progress").upsert(UserProgressDto(userId, lessonId))
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
            val dto = QuizResultDto(
                userId = userId, 
                quizId = result.quizId, 
                score = result.score, 
                correctAnswers = result.correctAnswers, 
                totalQuestions = result.totalQuestions, 
                completedAt = instant.toString()
            )
            supabase.from("quiz_results").upsert(dto)
            
            courseDao.insertQuizResult(QuizResultEntity(
                quizId = dto.quizId, 
                score = dto.score, 
                correctAnswers = dto.correctAnswers, 
                totalQuestions = dto.totalQuestions, 
                completedAt = result.completedAt
            ))
            true
        } catch (e: Exception) {
            Log.e("CourseRepo", "Error saving quiz result to Supabase", e)
            false
        }
    }

    // --- IMPLEMENTACIÓN DE ADMINISTRACIÓN ---

    override suspend fun saveTopic(topic: Topic): Result<Unit> {
        return try {
            Log.d("CourseRepo", "--- INICIO GUARDADO TEMA ---")
            Log.d("CourseRepo", "ID: ${topic.id}, Título: ${topic.title}")
            
            var finalContentUrl = topic.contentUrl ?: ""
            
            // 1. Subir el contenido Markdown (indexContent) a Storage si existe
            val indexContent = topic.indexContent
            if (!indexContent.isNullOrEmpty()) {
                val fileName = "topic_${topic.id}.md"
                val path = "topics/content/$fileName"
                val bucket = supabase.storage.from("content")
                
                Log.d("CourseRepo", "Subiendo indexContent MD a $path")
                bucket.upload(path, indexContent.toByteArray()) { upsert = true }
                finalContentUrl = bucket.publicUrl(path)
                Log.d("CourseRepo", "indexContent MD subido. URL: $finalContentUrl")
            }
            
            val dto = TopicDto(
                id = topic.id,
                title = topic.title,
                description = topic.description,
                imageUrl = topic.imageUrl,
                imagePlaceholder = topic.imagePlaceholder,
                contentUrl = finalContentUrl,
                order = topic.order,
                type = when(topic.type) {
                    TopicType.THEORY -> "theory"
                    TopicType.PRACTICE -> "practice"
                    TopicType.SUPPORT -> "support"
                },
                conversationId = topic.conversationId
            )
            
            Log.d("CourseRepo", "Enviando UPSERT a Supabase...")
            supabase.from("topics").upsert(dto)
            Log.d("CourseRepo", "¡UPSERT exitoso en Supabase!")
            
            // Sincronizamos localmente tras el guardado
            Log.d("CourseRepo", "Actualizando base de datos local (Room)...")
            courseDao.insertTopics(listOf(dto.toEntity(topic.lessonsCount, topic.completedLessonsCount, topic.indexContent)))
            Log.d("CourseRepo", "--- FIN GUARDADO TEMA (ÉXITO) ---")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CourseRepo", "!!! ERROR GUARDANDO TEMA !!!", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteTopic(id: String): Result<Unit> {
        return try {
            supabase.from("topics").delete { filter { eq("id", id) } }
            courseDao.deleteTopicById(id)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun uploadTopicImage(byteArray: ByteArray, fileName: String): Result<String> {
        return try {
            Log.d("CourseRepo", "Subiendo imagen a storage: $fileName")
            val bucket = supabase.storage.from("content")
            val path = "topics/images/$fileName"
            bucket.upload(path, byteArray) { upsert = true }
            val url = bucket.publicUrl(path)
            Log.d("CourseRepo", "Imagen subida. URL pública: $url")
            Result.success(url)
        } catch (e: Exception) {
            Log.e("CourseRepo", "Error subiendo imagen", e)
            Result.failure(e)
        }
    }

    override suspend fun saveLesson(lesson: Lesson): Result<Unit> {
        return try {
            Log.d("CourseRepo", "--- INICIO GUARDADO LECCIÓN ---")
            Log.d("CourseRepo", "ID: ${lesson.id}, Title: ${lesson.title}")
            
            var contentUrl = ""
            
            // 1. Subir el contenido Markdown a Storage si existe
            if (lesson.content.isNotEmpty()) {
                val fileName = "lesson_${lesson.id}.md"
                val path = "lessons/content/$fileName"
                val bucket = supabase.storage.from("content")
                
                Log.d("CourseRepo", "Subiendo contenido MD a $path")
                try {
                    bucket.upload(path, lesson.content.toByteArray()) { upsert = true }
                    contentUrl = bucket.publicUrl(path)
                    Log.d("CourseRepo", "Contenido MD subido. URL: $contentUrl")
                } catch (e: Exception) {
                    Log.e("CourseRepo", "Error subiendo a storage, buscando si ya existe...", e)
                    // Si falla por RLS o similar, intentamos obtener la URL de todas formas si no es crítico
                    contentUrl = bucket.publicUrl(path)
                }
            }

            val dto = LessonDto(
                id = lesson.id,
                topicId = lesson.topicId,
                title = lesson.title,
                description = lesson.description,
                order = lesson.order,
                contentUrl = contentUrl,
                imageUrl = lesson.imageUrl,
                videoUrl = lesson.videoUrl,
                audioUrl = lesson.audioUrl,
                pdfUrl = lesson.pdfUrl
            )
            
            Log.d("CourseRepo", "Enviando UPSERT de lección a Supabase...")
            supabase.from("lessons").upsert(dto)
            
            // 2. Actualizar local (Room)
            Log.d("CourseRepo", "Actualizando Room...")
            courseDao.insertLessons(listOf(dto.toEntity(lesson.isCompleted, lesson.content)))
            courseDao.refreshTopicProgress(lesson.topicId)
            
            Log.d("CourseRepo", "--- FIN GUARDADO LECCIÓN (ÉXITO) ---")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CourseRepo", "!!! ERROR GUARDANDO LECCIÓN !!!", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteLesson(id: String): Result<Unit> {
        return try {
            supabase.from("lessons").delete { filter { eq("id", id) } }
            courseDao.deleteLessonById(id)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun uploadLessonImage(byteArray: ByteArray, fileName: String): Result<String> {
        return try {
            val bucket = supabase.storage.from("content")
            val path = "lessons/images/$fileName"
            bucket.upload(path, byteArray) { upsert = true }
            Result.success(bucket.publicUrl(path))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun uploadLessonResource(byteArray: ByteArray, fileName: String, folder: String): Result<String> {
        return try {
            val bucket = supabase.storage.from("content")
            val path = "lessons/$folder/$fileName"
            bucket.upload(path, byteArray) { upsert = true }
            Result.success(bucket.publicUrl(path))
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun saveQuiz(quiz: Quiz): Result<Unit> {
        return try {
            Log.d("CourseRepo", "--- GUARDANDO QUIZ ---")
            Log.d("CourseRepo", "ID: ${quiz.id}, TopicID: ${quiz.topicId}, Title: ${quiz.title}")
            val dto = quiz.toDto()
            supabase.from("quizzes").upsert(dto)
            Log.d("CourseRepo", "Quiz guardado en Supabase")
            courseDao.insertQuiz(dto.toEntity())
            Log.d("CourseRepo", "Quiz guardado en Room")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CourseRepo", "Error guardando quiz", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteQuiz(id: String): Result<Unit> {
        return try {
            Log.d("CourseRepo", "Eliminando Quiz ID: $id")
            supabase.from("quizzes").delete { filter { eq("id", id) } }
            courseDao.deleteQuizById(id)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun saveQuestion(question: Question): Result<Unit> {
        return try {
            Log.d("CourseRepo", "--- GUARDANDO PREGUNTA ---")
            Log.d("CourseRepo", "ID: ${question.id}, QuizID: ${question.quizId}, Text: ${question.text}")
            val dto = question.toDto()
            supabase.from("questions").upsert(dto)
            Log.d("CourseRepo", "Pregunta guardada en Supabase")
            courseDao.insertQuestions(listOf(dto.toEntity()))
            Log.d("CourseRepo", "Pregunta guardada en Room")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CourseRepo", "Error guardando pregunta", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteQuestion(id: String): Result<Unit> {
        return try {
            supabase.from("questions").delete { filter { eq("id", id) } }
            courseDao.deleteQuestionById(id)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun saveExternalLink(link: ExternalLink): Result<Unit> {
        return try {
            val dto = ExternalLinkDto(
                id = link.id,
                topicId = link.topicId,
                title = link.title,
                description = link.description,
                url = link.url,
                order = link.order
            )
            supabase.from("external_links").upsert(dto)
            supportDao.insertExternalLinks(listOf(dto.toEntity()))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteExternalLink(linkId: String): Result<Unit> {
        return try {
            supabase.from("external_links").delete { filter { eq("id", linkId) } }
            supportDao.deleteExternalLink(linkId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveClinicalCase(clinicalCase: ClinicalCase): Result<Unit> {
        return try {
            val dto = ClinicalCaseDto(
                id = clinicalCase.id,
                topicId = clinicalCase.topicId,
                title = clinicalCase.title,
                description = clinicalCase.description,
                imageUrl = clinicalCase.imageUrl,
                quizId = clinicalCase.quizId
            )
            supabase.from("clinical_cases").upsert(dto)
            courseDao.insertClinicalCases(listOf(dto.toEntity()))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteClinicalCase(id: String): Result<Unit> {
        return try {
            supabase.from("clinical_cases").delete { filter { eq("id", id) } }
            courseDao.deleteClinicalCaseById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadClinicalCaseImage(byteArray: ByteArray, fileName: String): Result<String> {
        return try {
            val bucket = supabase.storage.from("content")
            val path = "clinical/images/$fileName"
            bucket.upload(path, byteArray) { upsert = true }
            Result.success(bucket.publicUrl(path))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
