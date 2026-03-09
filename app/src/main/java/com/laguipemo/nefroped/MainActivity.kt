package com.laguipemo.nefroped

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.laguipemo.nefroped.app.AppEntryViewModel
import com.laguipemo.nefroped.app.AppRoot
import com.laguipemo.nefroped.core.data.supabase.DeepLinkFlowManager
import com.laguipemo.nefroped.core.domain.model.app.AppEntryState
import com.laguipemo.nefroped.designsystem.theme.NefroPedTheme
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val appEntryViewModel: AppEntryViewModel by viewModel()
    private val supabaseClient: SupabaseClient by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        handleIntent(intent)

        enableEdgeToEdge()
        splashScreen.setKeepOnScreenCondition {
            appEntryViewModel.appEntryState.value is AppEntryState.Loading
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
        intent?.data?.let { uri ->
            // Si la URL contiene type=recovery, marcamos el flujo de reset
            if (uri.toString().contains("type=recovery") || uri.host == "reset-password") {
                DeepLinkFlowManager.setResetPasswordFlow(true)
            }
            supabaseClient.handleDeeplinks(intent)
        }
    }
}