package com.laguipemo.nefroped

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.laguipemo.nefroped.app.AppEntryViewModel
import com.laguipemo.nefroped.app.AppRoot
import com.laguipemo.nefroped.core.domain.model.app.AppEntryState
import com.laguipemo.nefroped.designsystem.theme.NefroPedTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val appEntryViewModel: AppEntryViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
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
}