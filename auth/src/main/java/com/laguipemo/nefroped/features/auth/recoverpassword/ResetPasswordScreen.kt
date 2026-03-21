package com.laguipemo.nefroped.features.auth.recoverpassword

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laguipemo.nefroped.core.domain.model.util.ValidationError
import com.laguipemo.nefroped.designsystem.R
import com.laguipemo.nefroped.designsystem.components.*
import com.laguipemo.nefroped.designsystem.util.toMessage
import org.koin.androidx.compose.koinViewModel

@Composable
fun ResetPasswordScreen(
    viewModel: ResetPasswordViewModel = koinViewModel(),
    onResetSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is ResetPasswordUiEffect.ShowError -> snackbarHostState.showSnackbar(effect.error.toMessage())
                ResetPasswordUiEffect.ResetPasswordSuccess -> onResetSuccess()
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.error
                )
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
            val minHeight = maxHeight
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = dimensionResource(R.dimen.screen_horizontal_padding)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.heightIn(min = minHeight),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.auth_header_padding_top)))

                    HeaderAuth(stringResource(R.string.auth_title_reset_password))

                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_xl)))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(dimensionResource(R.dimen.quiz_card_corner_radius)),
                        color = Color.White.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(
                            dimensionResource(R.dimen.border_stroke_width), 
                            Color.White.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(dimensionResource(R.dimen.space_m))) {
                            ResetPasswordForm(
                                uiState = uiState,
                                onEvent = viewModel::onEvent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_m)))
                    }
                    
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_m)))
                }
            }
        }
    }
}

@Composable
private fun ResetPasswordForm(
    uiState: ResetPasswordUiState,
    onEvent: (ResetPasswordUserEvent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.space_s))
    ) {
        PasswordTextField(
            value = uiState.password,
            onValueChange = { onEvent(ResetPasswordUserEvent.PasswordChanged(it)) },
            label = stringResource(R.string.auth_new_password_label),
            isError = uiState.passwordError != null,
            supportingText = when (val error = uiState.passwordError) {
                ValidationError.EmptyPassword -> stringResource(R.string.auth_error_password_required)
                is ValidationError.PasswordTooShort -> stringResource(R.string.auth_error_password_too_short, error.minLength)
                else -> null
            },
            onImeDone = { }
        )

        PasswordTextField(
            value = uiState.confirmPassword,
            onValueChange = { onEvent(ResetPasswordUserEvent.ConfirmPasswordChanged(it)) },
            label = stringResource(R.string.auth_confirm_password_label),
            isError = uiState.confirmPasswordError != null,
            supportingText = when (uiState.confirmPasswordError) {
                ValidationError.EmptyPassword -> stringResource(R.string.auth_error_password_required)
                is ValidationError.PasswordsDoNotMatch -> stringResource(R.string.auth_error_passwords_do_not_match)
                else -> null
            },
            onImeDone = { onEvent(ResetPasswordUserEvent.Submit) }
        )

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_m)))

        Button(
            onClick = { onEvent(ResetPasswordUserEvent.Submit) },
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.button_height)),
            shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = stringResource(R.string.auth_reset_password_button),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}
