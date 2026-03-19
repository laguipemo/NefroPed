package com.laguipemo.nefroped.core.local.room.dao

import androidx.room.*
import com.laguipemo.nefroped.core.local.room.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {

    // Temas
    @Query("SELECT * FROM topics ORDER BY `order` ASC")
    fun observeTopics(): Flow<List<TopicEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<TopicEntity>)

    @Query("UPDATE topics SET completedLessonsCount = (SELECT COUNT(*) FROM lessons WHERE topicId = :topicId AND isCompleted = 1) WHERE id = :topicId")
    suspend fun refreshTopicProgress(topicId: String)

    // Lecciones
    @Query("SELECT * FROM lessons WHERE topicId = :topicId ORDER BY `order` ASC")
    fun observeLessonsByTopic(topicId: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    fun observeLessonById(lessonId: String): Flow<LessonEntity?>

    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    suspend fun getLessonById(lessonId: String): LessonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonEntity>)

    @Query("UPDATE lessons SET isCompleted = :completed WHERE id = :lessonId")
    suspend fun updateLessonCompletion(lessonId: String, completed: Boolean)

    // Quizzes
    @Transaction
    @Query("SELECT * FROM quizzes WHERE topicId = :topicId")
    fun observeQuizWithQuestionsByTopic(topicId: String): Flow<QuizWithQuestions?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Query("DELETE FROM questions WHERE quizId = :quizId")
    suspend fun deleteQuestionsForQuiz(quizId: String)

    // Quiz Results
    @Query("SELECT * FROM quiz_results WHERE quizId = :quizId")
    fun observeQuizResult(quizId: String): Flow<QuizResultEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizResult(result: QuizResultEntity)

    // Limpieza
    @Query("DELETE FROM topics")
    suspend fun clearTopics()

    @Query("DELETE FROM lessons")
    suspend fun clearLessons()

    @Query("DELETE FROM quizzes")
    suspend fun clearQuizzes()
}
