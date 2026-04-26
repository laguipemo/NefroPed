package com.laguipemo.nefroped.core.domain.usecase.course

import com.laguipemo.nefroped.core.domain.model.course.ClinicalCase
import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository

class SaveClinicalCaseUseCase(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(clinicalCase: ClinicalCase): Result<Unit> =
        repository.saveClinicalCase(clinicalCase)
}
