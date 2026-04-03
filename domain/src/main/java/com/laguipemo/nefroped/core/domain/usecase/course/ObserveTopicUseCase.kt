package com.laguipemo.nefroped.core.domain.usecase.course

import com.laguipemo.nefroped.core.domain.model.course.Topic
import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository
import kotlinx.coroutines.flow.Flow

class ObserveTopicUseCase(
    private val repository: CourseRepository
) {
    operator fun invoke(id: String): Flow<Topic?> = repository.observeTopic(id)
}
