package com.laguipemo.nefroped.core.domain.repository.app

interface AppInfoRepository {
    fun getAppVersion(): String
}