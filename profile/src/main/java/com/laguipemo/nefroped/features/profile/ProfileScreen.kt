package com.laguipemo.nefroped.features.profile

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
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
import com.laguipemo.nefroped.designsystem.components.*
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = Color.Unspecified,
                    actionIconContentColor = Color.Unspecified
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
                    UserHeader(state)

                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_l)))

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

                    ProfileSection(title = stringResource(R.string.profile_section_assistance)) {
                        ProfileOptionItem(
                            icon = Icons.AutoMirrored.Filled.Chat,
                            title = stringResource(R.string.profile_action_chat),
                            subtitle = stringResource(R.string.profile_action_chat_desc),
                            onClick = onOpenChat
                        )
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_m)))
                    ProfileSection(title = stringResource(R.string.profile_section_about)) {
                        ProfileOptionItem(
                            icon = Icons.Default.AccountCircle,
                            title = stringResource(R.string.profile_app_version),
                            subtitle = "1.0.0 (BETA)",
                            showChevron = false,
                            onClick = {}
                        )
                    }

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

@Composable
fun UserHeader(state: ProfileUiState.Content) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.space_l)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(dimensionResource(R.dimen.avatar_profile_size))
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(dimensionResource(R.dimen.avatar_icon_size)),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_m)))
        Text(
            text = state.greeting,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        if (state.isGuest) {
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = CircleShape,
                modifier = Modifier.padding(top = dimensionResource(R.dimen.space_s))
            ) {
                Text(
                    text = stringResource(R.string.profile_guest_mode),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@Composable
fun LinkAccountSheetContent(
    state: ProfileUiState.Content,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLinkEmailPassword: () -> Unit,
    onLinkGoogle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.space_l))
            .padding(bottom = 48.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.profile_link_sheet_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.profile_link_sheet_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_xl)))

        SocialMediaButton(
            onClick = onLinkGoogle,
            text = stringResource(R.string.profile_link_google),
            icon = R.drawable.ic_google,
            color = colorResource(R.color.bg_btn_google)
        )

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_l)))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = " " + stringResource(R.string.profile_link_or) + " ",
                modifier = Modifier.padding(horizontal = 8.dp),
                style = MaterialTheme.typography.bodySmall
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_l)))

        EmailTextField(
            value = state.email,
            onValueChange = onEmailChanged,
            isError = state.emailError != null,
            supportingText = when (state.emailError) {
                ValidationError.EmptyEmail -> stringResource(R.string.auth_error_email_required)
                ValidationError.InvalidEmailFormat -> stringResource(R.string.auth_error_email_invalid)
                else -> null
            }
        )

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_m)))

        PasswordTextField(
            value = state.password,
            onValueChange = onPasswordChanged,
            isError = state.passwordError != null,
            supportingText = when (val error = state.passwordError) {
                ValidationError.EmptyPassword -> stringResource(R.string.auth_error_password_required)
                is ValidationError.PasswordTooShort -> stringResource(R.string.auth_error_password_too_short, error.minLength)
                else -> null
            },
            onImeDone = onLinkEmailPassword,
            label = stringResource(R.string.auth_new_password_label)
        )

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_xl)))

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
                Text(stringResource(R.string.profile_link_email))
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
