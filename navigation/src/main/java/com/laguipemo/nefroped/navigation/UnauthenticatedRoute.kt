package com.laguipemo.nefroped.navigation

import kotlinx.serialization.Serializable


@Serializable
sealed interface UnauthenticatedRoute {

    @Serializable
    data object Login: UnauthenticatedRoute

    @Serializable
    data object RecoverPassword: UnauthenticatedRoute

    @Serializable
    data object Register: UnauthenticatedRoute

    @Serializable
    data object ResetPassword: UnauthenticatedRoute
}