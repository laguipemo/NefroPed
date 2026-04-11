package com.laguipemo.nefroped.core.domain.repository.course

import com.laguipemo.nefroped.core.domain.model.course.*
import kotlinx.coroutines.flow.Flow

interface CourseRepository {
    fun observeTopics(): Flow<List<Topic>>
    fun observeTopicsAdmin(): Flow<List<Topic>> // Versión sin filtros para admin
    fun observeTopic(id: String): Flow<Topic?>
    fun observeLessons(topicId: String): Flow<List<Lesson>>
    fun observeLesson(lessonId: String): Flow<Lesson?>
    
    // Clinical Cases & Resources
    fun observeClinicalCases(topicId: String): Flow<List<ClinicalCase>>
    fun observeComplementaryResources(topicId: String): Flow<List<ComplementaryResource>>
    
    // Quiz methods
    fun observeQuizByTopic(topicId: String): Flow<Quiz?>
    fun observeQuizById(quizId: String): Flow<Quiz?>
    fun observeQuizResult(quizId: String): Flow<QuizResult?>
    fun observeAllQuizResults(): Flow<List<QuizResult>>
    
    suspend fun syncTopics(): Result<Unit>
    suspend fun syncLessons(topicId: String): Result<Unit>
    suspend fun syncQuiz(topicId: String): Result<Unit>
    suspend fun syncQuizById(quizId: String): Result<Unit>
    suspend fun syncClinicalData(topicId: String): Result<Unit>
    
    suspend fun markLessonAsCompleted(lessonId: String): Boolean
    suspend fun saveQuizResult(result: QuizResult): Boolean

    // --- MÉTODOS DE ADMINISTRACIÓN ---
    suspend fun saveTopic(topic: Topic): Result<Unit>
    suspend fun deleteTopic(id: String): Result<Unit>
    suspend fun uploadTopicImage(byteArray: ByteArray, fileName: String): Result<String>
    
    suspend fun saveLesson(lesson: Lesson): Result<Unit>
    suspend fun deleteLesson(id: String): Result<Unit>
    suspend fun uploadLessonImage(byteArray: ByteArray, fileName: String): Result<String>
    suspend fun uploadLessonResource(byteArray: ByteArray, fileName: String, folder: String): Result<String>
}
