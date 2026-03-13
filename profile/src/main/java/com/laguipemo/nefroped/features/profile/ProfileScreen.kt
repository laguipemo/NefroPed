package com.laguipemo.nefroped.features.profile

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = uiState) {
                ProfileUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is ProfileUiState.Content -> {
                    // 1. Header con Avatar
                    UserHeader(state)

                    Spacer(modifier = Modifier.height(24.dp))

                    // 2. Sección de Cuenta
                    ProfileSection(title = "Cuenta") {
                        if (state.isGuest) {
                            ProfileOptionItem(
                                icon = Icons.Default.Link,
                                title = "Vincular cuenta",
                                subtitle = "No pierdas tu progreso",
                                onClick = { viewModel.onShowBottomSheet(true) },
                                iconColor = MaterialTheme.colorScheme.primary
                            )
                        }
                        ProfileOptionItem(
                            icon = Icons.AutoMirrored.Filled.Logout,
                            title = "Cerrar sesión",
                            onClick = { viewModel.onLogoutClicked() },
                            iconColor = MaterialTheme.colorScheme.error,
                            showChevron = false
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. Sección de App
                    ProfileSection(title = "Asistencia") {
                        ProfileOptionItem(
                            icon = Icons.AutoMirrored.Filled.Chat,
                            title = "Abrir Chat de ayuda",
                            subtitle = "Consulta tus dudas",
                            onClick = onOpenChat
                        )
                    }

                    // 4. Sección de Información
                    Spacer(modifier = Modifier.height(16.dp))
                    ProfileSection(title = "Sobre NefroPed") {
                        ProfileOptionItem(
                            icon = Icons.Default.AccountCircle,
                            title = "Versión de la app",
                            subtitle = "1.0.0 (BETA)",
                            showChevron = false,
                            onClick = {}
                        )
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
fun UserHeader(state: ProfileUiState.Content) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = state.greeting,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        if (state.isGuest) {
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = CircleShape,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "Modo Invitado",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@Composable
fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun ProfileOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    iconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    showChevron: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (showChevron) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp)
            )
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
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
