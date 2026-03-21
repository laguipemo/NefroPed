package com.laguipemo.nefroped.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laguipemo.nefroped.core.domain.model.app.AppEntryState
import com.laguipemo.nefroped.features.auth.recoverpassword.ResetPasswordScreen
import com.laguipemo.nefroped.navigation.OnboardingNavGraph
import com.laguipemo.nefroped.navigation.UnauthenticatedNavGraph
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppRoot(
    viewModel: AppEntryViewModel = koinViewModel()
) {
    val appEntryState: AppEntryState by viewModel.appEntryState.collectAsStateWithLifecycle()

    val backgroundGradient = Brush.verticalGradient(
        0.0f to MaterialTheme.colorScheme.primary,
        0.4f to MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
        1.0f to MaterialTheme.colorScheme.background
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        AnimatedContent(
            targetState = appEntryState,
            transitionSpec = {
                fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
            },
            label = "AppEntryTransition"
        ) { state ->
            when (state) {
                AppEntryState.Loading -> {
                    // Pantalla de carga integrada
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
                            // El estado cambiará automáticamente
                        }
                    )
                }

                AppEntryState.Ready -> {
                    MainScreen()
                }

                AppEntryState.Error -> {
                    // ErrorScreen()
                }
            }
        }
    }
}
