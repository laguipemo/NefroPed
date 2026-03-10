package com.laguipemo.nefroped.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.laguipemo.nefroped.features.auth.login.LoginScreen
import com.laguipemo.nefroped.features.auth.recoverpassword.RecoverPasswordScreen
import com.laguipemo.nefroped.features.auth.register.RegisterScreen

@Composable
fun UnauthenticatedNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = UnauthenticatedRoute.Login
    ) {
        composable<UnauthenticatedRoute.Login> {
            LoginScreen(
                onLoginSuccess = {

                },
                onRegister = {
                    navController.navigate(UnauthenticatedRoute.Register)
                },
                onRecoverPassword = {
                    navController.navigate(UnauthenticatedRoute.RecoverPassword)
                }
            )
        }

        composable<UnauthenticatedRoute.Register> {
            RegisterScreen(
                onRegisterSuccess = {
                }
            )
        }

        composable<UnauthenticatedRoute.RecoverPassword> {
            RecoverPasswordScreen(
                onRecoverPasswordSuccess = {
                    navController.navigate(UnauthenticatedRoute.Login)
                }
            )
        }
    }
}