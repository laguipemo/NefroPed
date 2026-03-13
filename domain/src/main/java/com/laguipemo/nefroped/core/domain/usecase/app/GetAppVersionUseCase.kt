package com.laguipemo.nefroped.core.domain.usecase.app

import com.laguipemo.nefroped.core.domain.repository.app.AppInfoRepository

class GetAppVersionUseCase(
    private val repository: AppInfoRepository
) {
    operator fun invoke(): String = repository.getAppVersion()
}