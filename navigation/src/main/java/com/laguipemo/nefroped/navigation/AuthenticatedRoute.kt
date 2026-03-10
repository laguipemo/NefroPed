package com.laguipemo.nefroped.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AuthenticatedRoute {

    @Serializable
    data object Profile : AuthenticatedRoute

    @Serializable
    data class Chat(val conversationId: String) : AuthenticatedRoute

    @Serializable
    data object ResetPassword : AuthenticatedRoute
}