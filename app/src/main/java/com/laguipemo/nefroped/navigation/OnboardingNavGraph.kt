package com.laguipemo.nefroped.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.laguipemo.nefroped.features.onboarding.OnboardingScreen

@Composable
fun OnboardingNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "onboarding_main"
    ) {
        composable("onboarding_main") {
            OnboardingScreen()
        }
    }
}
