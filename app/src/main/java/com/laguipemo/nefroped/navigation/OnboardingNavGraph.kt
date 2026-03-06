package com.laguipemo.nefroped.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.laguipemo.nefroped.features.onboarding.OnboardingPermissionsScreen
import com.laguipemo.nefroped.features.onboarding.OnboardingWelcomeScreen

@Composable
fun OnboardingNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "onboarding_welcome"
    ) {
        composable("onboarding_welcome") {
            OnboardingWelcomeScreen(
                onContinue = {
                    navController.navigate("onboarding_permissions")
                }
            )
        }

        composable("onboarding_permissions") {
            OnboardingPermissionsScreen()
        }
    }
}