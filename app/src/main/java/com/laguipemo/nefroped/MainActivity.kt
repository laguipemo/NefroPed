package com.laguipemo.nefroped

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.laguipemo.nefroped.app.AppEntryViewModel
import com.laguipemo.nefroped.app.AppRoot
import com.laguipemo.nefroped.core.domain.model.app.AppEntryState
import com.laguipemo.nefroped.designsystem.theme.NefroPedTheme
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val appEntryViewModel: AppEntryViewModel by viewModel()
    private val supabaseClient: SupabaseClient by inject()
    private var isStarting = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Manejamos el intent inicial
        handleIntent(intent)

        enableEdgeToEdge()

        // Mantenemos el splash un poco más para evitar el parpadeo visual
        // mientras Supabase procesa la sesión del Deep Link
        splashScreen.setKeepOnScreenCondition {
            appEntryViewModel.appEntryState.value is AppEntryState.Loading || isStarting
        }

        // Un pequeño margen para estabilizar el estado inicial
        lifecycleScope.launch {
            delay(500)
            isStarting = false
        }

        setContent {
            NefroPedTheme {
                AppRoot()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.data?.let { _ ->
            supabaseClient.handleDeeplinks(intent)
        }
    }
}