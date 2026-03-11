package com.laguipemo.nefroped.features.profile

sealed interface ProfileUiState {
    object Loading : ProfileUiState
    data class Content(
        val greeting: String,
        val isGuest: Boolean,
        val isLoading: Boolean = false
    ) : ProfileUiState
}