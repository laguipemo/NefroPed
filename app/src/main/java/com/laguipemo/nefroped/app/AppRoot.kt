package com.laguipemo.nefroped.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laguipemo.nefroped.core.domain.model.app.AppEntryState
import com.laguipemo.nefroped.features.auth.recoverpassword.ResetPasswordScreen
import com.laguipemo.nefroped.navigation.AuthenticatedNavGraph
import com.laguipemo.nefroped.navigation.OnboardingNavGraph
import com.laguipemo.nefroped.navigation.UnauthenticatedNavGraph
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppRoot(
    viewModel: AppEntryViewModel = koinViewModel()
) {
    val appEntryState: AppEntryState by viewModel.appEntryState.collectAsStateWithLifecycle()

    // Usamos AnimatedContent para que la transición entre estados de la app sea fluida
    AnimatedContent(
        targetState = appEntryState,
        transitionSpec = {
            fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
        },
        label = "AppEntryTransition"
    ) { state ->
        when (state) {
            AppEntryState.Loading -> {
                // Pantalla de carga persistente mientras se decide el estado
            }

            AppEntryState.RequireLogin -> {
                UnauthenticatedNavGraph()
            }

            AppEntryState.RequireOnboarding -> {
                OnboardingNavGraph()
            }

            AppEntryState.ResetPassword -> {
                ResetPasswordScreen(
                    onResetSuccess = {
                        // El estado cambiará automáticamente por el signOut del repositorio
                    }
                )
            }

            AppEntryState.Ready -> {
                AuthenticatedNavGraph()
            }

            AppEntryState.Error -> {
                // ErrorScreen()
            }
        }
    }
}