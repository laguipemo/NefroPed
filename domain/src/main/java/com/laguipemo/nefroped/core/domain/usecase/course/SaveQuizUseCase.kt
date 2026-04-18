package com.laguipemo.nefroped.core.domain.usecase.course

import com.laguipemo.nefroped.core.domain.model.course.Quiz
import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository

class SaveQuizUseCase(private val repository: CourseRepository) {
    suspend operator fun invoke(quiz: Quiz) = repository.saveQuiz(quiz)
}
