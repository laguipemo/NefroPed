package com.laguipemo.nefroped.core.domain.usecase.course

import com.laguipemo.nefroped.core.domain.model.course.Topic
import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository
import kotlinx.coroutines.flow.Flow

class ObserveTopicsUseCase(
    private val repository: CourseRepository
) {
    operator fun invoke(): Flow<List<Topic>> = repository.observeTopics()
}
