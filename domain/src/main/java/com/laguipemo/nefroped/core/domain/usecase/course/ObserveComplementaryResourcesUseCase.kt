package com.laguipemo.nefroped.core.domain.usecase.course

import com.laguipemo.nefroped.core.domain.model.course.ComplementaryResource
import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository
import kotlinx.coroutines.flow.Flow

class ObserveComplementaryResourcesUseCase(
    private val repository: CourseRepository
) {
    operator fun invoke(topicId: String): Flow<List<ComplementaryResource>> = repository.observeComplementaryResources(topicId)
}
