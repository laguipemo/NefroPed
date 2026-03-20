package com.laguipemo.nefroped.core.domain.usecase.course

import com.laguipemo.nefroped.core.domain.model.course.ClinicalCase
import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository
import kotlinx.coroutines.flow.Flow

class ObserveClinicalCasesUseCase(
    private val repository: CourseRepository
) {
    operator fun invoke(topicId: String): Flow<List<ClinicalCase>> = repository.observeClinicalCases(topicId)
}
