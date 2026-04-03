package com.laguipemo.nefroped.core.domain.usecase.course

import com.laguipemo.nefroped.core.domain.model.course.Topic
import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository

class SaveTopicUseCase(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(topic: Topic): Result<Unit> = repository.saveTopic(topic)
}
