package com.laguipemo.nefroped.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laguipemo.nefroped.core.domain.model.app.AppEntryState
import com.laguipemo.nefroped.navigation.AuthenticatedNavGraph
import com.laguipemo.nefroped.navigation.OnboardingNavGraph
import com.laguipemo.nefroped.navigation.UnauthenticatedNavGraph
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppRoot(
    viewModel: AppEntryViewModel = koinViewModel()
) {
    val appEntryState: AppEntryState by viewModel.appEntryState.collectAsStateWithLifecycle()

    when (appEntryState) {
        AppEntryState.Loading -> {
            //SplashScreen()
        }

        AppEntryState.RequireLogin -> {
            UnauthenticatedNavGraph()
        }

        AppEntryState.RequireOnboarding -> {
            OnboardingNavGraph()
        }


        AppEntryState.Ready -> {
            AuthenticatedNavGraph()
        }

        AppEntryState.Error -> {
            //ErrorScreen()
        }
    }
}