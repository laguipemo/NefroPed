package com.laguipemo.nefroped.core.domain.usecase.course

import com.laguipemo.nefroped.core.domain.model.course.ExternalLink
import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository

class SaveExternalLinkUseCase(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(link: ExternalLink): Result<Unit> =
        repository.saveExternalLink(link)
}
