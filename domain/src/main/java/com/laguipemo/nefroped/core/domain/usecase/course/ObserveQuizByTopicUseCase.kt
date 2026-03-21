package com.laguipemo.nefroped.core.domain.usecase.course

import com.laguipemo.nefroped.core.domain.model.course.Quiz
import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository
import kotlinx.coroutines.flow.Flow

class ObserveQuizByTopicUseCase(
    private val repository: CourseRepository
) {
    operator fun invoke(topicId: String): Flow<Quiz?> = repository.observeQuizByTopic(topicId)
}
