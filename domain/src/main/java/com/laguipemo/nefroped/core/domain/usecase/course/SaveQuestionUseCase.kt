package com.laguipemo.nefroped.core.domain.usecase.course

import com.laguipemo.nefroped.core.domain.model.course.Question
import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository

class SaveQuestionUseCase(private val repository: CourseRepository) {
    suspend operator fun invoke(question: Question) = repository.saveQuestion(question)
}
