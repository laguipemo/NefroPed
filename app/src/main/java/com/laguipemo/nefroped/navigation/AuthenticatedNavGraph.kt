package com.laguipemo.nefroped.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.laguipemo.nefroped.features.chat.ChatScreen
import com.laguipemo.nefroped.features.profile.ProfileScreen

@Composable
fun AuthenticatedNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = AuthenticatedRoute.Profile

    ) {
        composable<AuthenticatedRoute.Profile> {
            ProfileScreen(
                onOpenChat = {
                    navController.navigate(
                        AuthenticatedRoute.Chat("default")
                    )
                }
            )

        }

        composable<AuthenticatedRoute.Chat> {
            ChatScreen()
        }

    }
}