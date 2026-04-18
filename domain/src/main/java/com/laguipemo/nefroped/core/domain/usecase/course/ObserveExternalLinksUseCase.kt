package com.laguipemo.nefroped.core.domain.usecase.course

import com.laguipemo.nefroped.core.domain.model.course.ExternalLink
import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository
import kotlinx.coroutines.flow.Flow

class ObserveExternalLinksUseCase(
    private val repository: CourseRepository
) {
    operator fun invoke(topicId: String): Flow<List<ExternalLink>> =
        repository.observeExternalLinks(topicId)
}
