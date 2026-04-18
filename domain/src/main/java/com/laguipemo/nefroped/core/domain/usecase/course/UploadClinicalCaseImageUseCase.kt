package com.laguipemo.nefroped.core.domain.usecase.course

import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository

class UploadClinicalCaseImageUseCase(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(byteArray: ByteArray, fileName: String): Result<String> =
        repository.uploadClinicalCaseImage(byteArray, fileName)
}
