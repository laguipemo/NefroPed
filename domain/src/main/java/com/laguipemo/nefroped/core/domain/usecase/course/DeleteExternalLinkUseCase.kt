package com.laguipemo.nefroped.core.domain.usecase.course

import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository

class DeleteExternalLinkUseCase(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(linkId: String): Result<Unit> =
        repository.deleteExternalLink(linkId)
}
