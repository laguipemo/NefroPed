package com.laguipemo.nefroped.core.domain.model.session

sealed interface SessionFailure {
    data object Network : SessionFailure
    data object SessionExpired : SessionFailure
    data object CorruptLocalData : SessionFailure
    data class Unknown(val cause: Throwable) : SessionFailure
}