package com.laguipemo.nefroped.features.profile

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.laguipemo.nefroped.core.domain.model.util.ValidationError
import com.laguipemo.nefroped.designsystem.R
import com.laguipemo.nefroped.designsystem.components.EmailTextField
import com.laguipemo.nefroped.designsystem.components.PasswordTextField
import com.laguipemo.nefroped.designsystem.components.SocialMediaButton
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinViewModel(),
    onOpenChat: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

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
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.onShowBottomSheet(true) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Vincular cuenta")
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

                    // Bottom Sheet para vinculación
                    if (state.showBottomSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { viewModel.onShowBottomSheet(false) },
                            sheetState = sheetState
                        ) {
                            LinkAccountContent(
                                state = state,
                                onEmailChanged = viewModel::onEmailChanged,
                                onPasswordChanged = viewModel::onPasswordChanged,
                                onLinkEmailPassword = viewModel::onLinkWithEmailPassword,
                                onLinkGoogle = {
                                    scope.launch {
                                        handleGoogleLink(context, viewModel)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LinkAccountContent(
    state: ProfileUiState.Content,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLinkEmailPassword: () -> Unit,
    onLinkGoogle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 48.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Vincular cuenta",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Elige cómo quieres guardar tu progreso",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        SocialMediaButton(
            onClick = onLinkGoogle,
            text = "Continuar con Google",
            icon = R.drawable.ic_google,
            color = colorResource(R.color.bg_btn_google)
        )

        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = " o ",
                modifier = Modifier.padding(horizontal = 8.dp),
                style = MaterialTheme.typography.bodySmall
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        EmailTextField(
            value = state.email,
            onValueChange = onEmailChanged,
            isError = state.emailError != null,
            supportingText = when (state.emailError) {
                ValidationError.EmptyEmail -> "El email es obligatorio"
                ValidationError.InvalidEmailFormat -> "Formato de email no válido"
                else -> null
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PasswordTextField(
            value = state.password,
            onValueChange = onPasswordChanged,
            isError = state.passwordError != null,
            supportingText = when (val error = state.passwordError) {
                ValidationError.EmptyPassword -> "La contraseña es obligatoria"
                is ValidationError.PasswordTooShort -> "Mínimo ${error.minLength} caracteres"
                else -> null
            },
            onImeDone = onLinkEmailPassword,
            label = "Nueva contraseña"
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onLinkEmailPassword,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(4.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Vincular con Email")
            }
        }
    }
}

private suspend fun handleGoogleLink(
    context: Context,
    viewModel: ProfileViewModel
) {
    val credentialManager = CredentialManager.create(context)
    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId("439824105960-rto1l6vlrkp59kplrm243dlvamf1ek4v.apps.googleusercontent.com")
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val result = credentialManager.getCredential(
            context = context,
            request = request
        )
        handleCredential(result, viewModel)
    } catch (_: GetCredentialException) {
    } catch (_: Exception) {
    }
}

private fun handleCredential(
    result: GetCredentialResponse,
    viewModel: ProfileViewModel
) {
    val credential = result.credential
    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        try {
            val googleIdTokenCredential =
                GoogleIdTokenCredential.createFrom(credential.data)
            viewModel.onLinkWithGoogle(googleIdTokenCredential.idToken)
        } catch (_: Exception) {
        }
    }
}
