package com.laguipemo.nefroped.features.profile

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.laguipemo.nefroped.designsystem.R
import com.laguipemo.nefroped.designsystem.components.*
import com.laguipemo.nefroped.features.profile.components.LinkAccountSheetContent
import com.laguipemo.nefroped.features.profile.components.UserHeader
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
                title = { Text(stringResource(R.string.profile_title), fontWeight = FontWeight.Bold) },
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
                    // 1. Cabecera con Avatar (Imagen real si existe)
                    UserHeader(state)

                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_l)))

                    // 2. Sección de Cuenta
                    ProfileSection(title = stringResource(R.string.profile_section_account)) {
                        if (state.isGuest) {
                            ProfileOptionItem(
                                icon = Icons.Default.Link,
                                title = stringResource(R.string.profile_action_link_account),
                                subtitle = stringResource(R.string.profile_action_link_account_desc),
                                onClick = { viewModel.onShowBottomSheet(true) },
                                iconColor = MaterialTheme.colorScheme.primary
                            )
                        }
                        ProfileOptionItem(
                            icon = Icons.AutoMirrored.Filled.Logout,
                            title = stringResource(R.string.profile_action_logout),
                            onClick = { viewModel.onLogoutClicked() },
                            iconColor = MaterialTheme.colorScheme.error,
                            showChevron = false
                        )
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_m)))

                    // 3. Sección de Asistencia
                    ProfileSection(title = stringResource(R.string.profile_section_assistance)) {
                        ProfileOptionItem(
                            icon = Icons.AutoMirrored.Filled.Chat,
                            title = stringResource(R.string.profile_action_chat),
                            subtitle = stringResource(R.string.profile_action_chat_desc),
                            onClick = onOpenChat
                        )
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_m)))

                    // 4. Sección Informativa
                    ProfileSection(title = stringResource(R.string.profile_section_about)) {
                        ProfileOptionItem(
                            icon = Icons.Default.AccountCircle,
                            title = stringResource(R.string.profile_app_version),
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
                            LinkAccountSheetContent(
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
