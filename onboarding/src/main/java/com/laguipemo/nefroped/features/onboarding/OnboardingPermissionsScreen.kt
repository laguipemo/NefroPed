package com.laguipemo.nefroped.features.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingPermissionsScreen(
    viewModel: OnboardingViewModel = koinViewModel(),
    //onFinish: () -> Unit
) {
    Scaffold() { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Estamos en los Persmisos: Pantalla Persmisos provisional")

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    //onFinish()
                    viewModel.onOnboardingFinished()
                }
            ) {
                Text("Terminar")
            }
        }

    }
}