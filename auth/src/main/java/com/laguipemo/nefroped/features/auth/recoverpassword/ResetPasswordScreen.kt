package com.laguipemo.nefroped.features.auth.recoverpassword

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laguipemo.nefroped.core.domain.model.util.ValidationError
import com.laguipemo.nefroped.designsystem.R
import com.laguipemo.nefroped.features.auth.components.HeaderAuth
import com.laguipemo.nefroped.features.auth.components.PasswordTextField
import com.laguipemo.nefroped.features.auth.util.toMessage
import org.koin.androidx.compose.koinViewModel

@Composable
fun ResetPasswordScreen(
    viewModel: ResetPasswordViewModel = koinViewModel(),
    onResetSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    val spaceM = dimensionResource(R.dimen.space_m)
    val buttonHeight = dimensionResource(R.dimen.button_height)

    LaunchedEffect(Unit) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is ResetPasswordUiEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.error.toMessage())
                }
                ResetPasswordUiEffect.ResetPasswordSuccess -> {
                    onResetSuccess()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(dimensionResource(R.dimen.screen_horizontal_padding)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeaderAuth(stringResource(R.string.auth_title_reset_password))

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_xl)))

            PasswordTextField(
                value = uiState.password,
                onValueChange = { viewModel.onEvent(ResetPasswordUserEvent.PasswordChanged(it)) },
                isError = uiState.passwordError != null,
                supportingText = if (uiState.passwordError is ValidationError.PasswordTooShort) stringResource(R.string.auth_error_password_too_short, 6) else null,
                onImeDone = { },
                modifier = Modifier.semantics { contentType = ContentType.NewPassword }
            )

            Spacer(modifier = Modifier.height(spaceM))

            PasswordTextField(
                value = uiState.confirmPassword,
                onValueChange = { viewModel.onEvent(ResetPasswordUserEvent.ConfirmPasswordChanged(it)) },
                isError = uiState.confirmPasswordError != null,
                supportingText = if (uiState.confirmPasswordError is ValidationError.PasswordsDoNotMatch) stringResource(R.string.auth_error_passwords_do_not_match) else null,
                onImeDone = { 
                    focusManager.clearFocus()
                    viewModel.onEvent(ResetPasswordUserEvent.Submit) 
                },
                modifier = Modifier.semantics { contentType = ContentType.NewPassword }
            )

            Spacer(modifier = Modifier.height(spaceM))

            Button(
                onClick = { 
                    focusManager.clearFocus()
                    viewModel.onEvent(ResetPasswordUserEvent.Submit) 
                },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(buttonHeight)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(dimensionResource(R.dimen.space_l)),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = dimensionResource(R.dimen.space_xs)
                    )
                } else {
                    Text(stringResource(R.string.auth_reset_password_button))
                }
            }
        }
    }
}