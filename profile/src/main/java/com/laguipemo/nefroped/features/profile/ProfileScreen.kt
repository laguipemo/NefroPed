package com.laguipemo.nefroped.features.profile

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinViewModel(),
    onOpenChat: ()-> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when(val state = uiState) {
                ProfileUiState.Loading -> {
                    Text("Cargando...")
                }
                is ProfileUiState.Content -> {
                    Text(state.greeting)

                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onOpenChat()
                }
            ) {
                Text("Abrir Chat")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.onLogoutClicked() }
            ) {
                Text("Cerrar sessión")
            }
        }

    }

}