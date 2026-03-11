package com.laguipemo.nefroped.features.profile

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.laguipemo.nefroped.designsystem.R
import com.laguipemo.nefroped.designsystem.components.SocialMediaButton
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinViewModel(),
    onOpenChat: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = uiState) {
                ProfileUiState.Loading -> {
                    CircularProgressIndicator()
                }

                is ProfileUiState.Content -> {
                    Text(
                        text = state.greeting,
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    if (state.isGuest) {
                        Text(
                            text = "Estás usando una cuenta de invitado. Vincula tu cuenta para no perder tu progreso.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (state.isLoading) {
                            CircularProgressIndicator()
                        } else {
                            SocialMediaButton(
                                onClick = {
                                    scope.launch {
                                        handleGoogleLink(context, viewModel)
                                    }
                                },
                                text = "Vincular con Google",
                                icon = R.drawable.ic_google,
                                color = colorResource(R.color.bg_btn_google)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    Button(
                        onClick = { onOpenChat() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Abrir Chat")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.onLogoutClicked() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar sesión")
                    }
                }
            }
        }
    }
}

private suspend fun handleGoogleLink(context: Context, viewModel: ProfileViewModel) {
    val credentialManager = CredentialManager.create(context)
    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId("439824105960-rto1l6vlrkp59kplrm243dlvamf1ek4v.apps.googleusercontent.com")
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val result = credentialManager.getCredential(context = context, request = request)
        handleCredential(result, viewModel)
    } catch (e: GetCredentialException) {
        // Manejar error
    } catch (e: Exception) {
        // Manejar error
    }
}

private fun handleCredential(result: GetCredentialResponse, viewModel: ProfileViewModel) {
    val credential = result.credential
    
    // Usamos el método robusto que ya probamos en LoginScreen
    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        try {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            viewModel.onLinkWithGoogle(googleIdTokenCredential.idToken)
        } catch (_: Exception) { }
    }
}
