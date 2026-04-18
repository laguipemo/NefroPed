package com.laguipemo.nefroped.core.domain.usecase.course

import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository

class DeleteQuizUseCase(private val repository: CourseRepository) {
    suspend operator fun invoke(id: String) = repository.deleteQuiz(id)
}
