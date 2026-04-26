package com.laguipemo.nefroped.core.domain.usecase.course

import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository

class DeleteClinicalCaseUseCase(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> =
        repository.deleteClinicalCase(id)
}
