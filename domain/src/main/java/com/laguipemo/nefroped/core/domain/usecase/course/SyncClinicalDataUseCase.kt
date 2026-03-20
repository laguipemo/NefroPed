package com.laguipemo.nefroped.core.domain.usecase.course

import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository

class SyncClinicalDataUseCase(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(topicId: String): Result<Unit> = repository.syncClinicalData(topicId)
}
