package com.laguipemo.nefroped.core.local.room.dao

import androidx.room.*
import com.laguipemo.nefroped.core.local.room.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {

    // --- TEMAS (TOPICS) ---
    @Query("SELECT * FROM topics ORDER BY `order` ASC")
    fun observeTopics(): Flow<List<TopicEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<TopicEntity>)

    @Query("DELETE FROM topics WHERE id = :id")
    suspend fun deleteTopicById(id: String)

    @Query("UPDATE topics SET completedLessonsCount = (SELECT COUNT(*) FROM lessons WHERE topicId = :topicId AND isCompleted = 1) WHERE id = :topicId")
    suspend fun refreshTopicProgress(topicId: String)

    // --- LECCIONES (LESSONS) ---
    @Query("SELECT * FROM lessons WHERE topicId = :topicId ORDER BY `order` ASC")
    fun observeLessonsByTopic(topicId: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    fun observeLessonById(lessonId: String): Flow<LessonEntity?>

    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    suspend fun getLessonById(lessonId: String): LessonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonEntity>)

    @Query("DELETE FROM lessons WHERE id = :id")
    suspend fun deleteLessonById(id: String)

    @Query("UPDATE lessons SET isCompleted = :completed WHERE id = :lessonId")
    suspend fun updateLessonCompletion(lessonId: String, completed: Boolean)

    // --- TESTS (QUIZZES & QUESTIONS) ---
    @Transaction
    @Query("SELECT * FROM quizzes WHERE topicId = :topicId")
    fun observeQuizWithQuestionsByTopic(topicId: String): Flow<QuizWithQuestions?>

    @Transaction
    @Query("SELECT * FROM quizzes WHERE id = :id")
    fun observeQuizWithQuestionsById(id: String): Flow<QuizWithQuestions?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity)

    @Query("DELETE FROM quizzes WHERE id = :id")
    suspend fun deleteQuizById(id: String)

    @Query("DELETE FROM quizzes WHERE topicId = :topicId")
    suspend fun deleteQuizByTopic(topicId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Query("DELETE FROM questions WHERE id = :id")
    suspend fun deleteQuestionById(id: String)

    @Query("DELETE FROM questions WHERE quizId = :quizId")
    suspend fun deleteQuestionsForQuiz(quizId: String)

    // --- RESULTADOS (RESULTS) ---
    @Query("SELECT * FROM quiz_results WHERE quizId = :quizId")
    fun observeQuizResult(quizId: String): Flow<QuizResultEntity?>

    @Query("SELECT * FROM quiz_results")
    fun observeAllQuizResults(): Flow<List<QuizResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizResult(result: QuizResultEntity)

    // --- CASOS CLÍNICOS & RECURSOS (CLINICAL DATA) ---
    @Query("SELECT * FROM clinical_cases WHERE topicId = :topicId")
    fun observeClinicalCases(topicId: String): Flow<List<ClinicalCaseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClinicalCases(cases: List<ClinicalCaseEntity>)

    @Query("DELETE FROM clinical_cases WHERE id = :id")
    suspend fun deleteClinicalCaseById(id: String)

    @Query("SELECT * FROM complementary_resources WHERE topicId = :topicId")
    fun observeComplementaryResources(topicId: String): Flow<List<ComplementaryResourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplementaryResources(resources: List<ComplementaryResourceEntity>)

    // --- LIMPIEZA (CLEANUP) ---
    @Query("DELETE FROM topics")
    suspend fun clearTopics()

    @Query("DELETE FROM lessons")
    suspend fun clearLessons()

    @Query("DELETE FROM quizzes")
    suspend fun clearQuizzes()

    @Query("DELETE FROM clinical_cases")
    suspend fun clearClinicalCases()

    @Query("DELETE FROM complementary_resources")
    suspend fun clearComplementaryResources()
}
