package com.laguipemo.nefroped.core.domain.usecase.course

import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository

class SyncTopicsUseCase(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.syncTopics()
}
