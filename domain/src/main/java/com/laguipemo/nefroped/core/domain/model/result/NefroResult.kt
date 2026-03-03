package com.laguipemo.nefroped.core.domain.model.result

sealed interface NefroResult<out T, out E> {
    data class Success<T>(val data:T) : NefroResult<T, Nothing>
    data class Error<E>(val error: E) : NefroResult<Nothing, E>
}