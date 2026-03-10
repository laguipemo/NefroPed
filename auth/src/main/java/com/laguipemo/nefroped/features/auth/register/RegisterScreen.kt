package com.laguipemo.nefroped.features.auth.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laguipemo.nefroped.core.domain.model.util.ValidationError
import com.laguipemo.nefroped.designsystem.R
import com.laguipemo.nefroped.features.auth.components.EmailTextField
import com.laguipemo.nefroped.features.auth.components.HeaderAuth
import com.laguipemo.nefroped.features.auth.components.PasswordTextField
import com.laguipemo.nefroped.features.auth.util.toMessage
import org.koin.androidx.compose.koinViewModel

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = koinViewModel(),
    onRegisterSuccess: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val horizontalPadding = dimensionResource(
        R.dimen.screen_horizontal_padding
    )
    val verticalPadding = dimensionResource(
        R.dimen.screen_vertical_padding
    )
    val spaceS = dimensionResource(R.dimen.space_s)
    val spaceM = dimensionResource(R.dimen.space_m)
    val spaceXL = dimensionResource(R.dimen.space_xl)
    val buttonHeight = dimensionResource(R.dimen.button_height)

    LaunchedEffect(Unit) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is RegisterUiEffect.ShowError ->
                    snackbarHostState.showSnackbar(effect.error.toMessage())

                RegisterUiEffect.RegisterSuccess -> {
                    onRegisterSuccess()
                }
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

        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(
                    horizontal = horizontalPadding,
                    vertical = verticalPadding
                ),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // HEADER: título + logo + subtítulo
            HeaderAuth(
                stringResource(R.string.auth_title_register)
            )

            Spacer(modifier = Modifier.height(spaceXL))

            // FORM + BOTONES
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EmailTextField(
                    value = uiState.email,
                    onValueChange = {
                        viewModel.onEvent(
                            RegisterUserEvent.EmailChanged(it)
                        )
                    },
                    isError = uiState.emailError != null,
                    supportingText = when (uiState.emailError) {
                        is ValidationError.EmptyEmail ->
                            stringResource(R.string.auth_error_email_required)

                        is ValidationError.InvalidEmailFormat ->
                            stringResource(R.string.auth_error_email_invalid)

                        else -> null
                    }
                )

                Spacer(modifier = Modifier.height(spaceS))

                PasswordTextField(
                    value = uiState.password,
                    onValueChange = {
                        viewModel.onEvent(
                            RegisterUserEvent.PasswordChanged(it)
                        )
                    },
                    isError = uiState.passwordError != null,
                    supportingText = when (val error = uiState.passwordError) {
                        is ValidationError.EmptyPassword ->
                            stringResource(R.string.auth_error_password_required)

                        is ValidationError.PasswordTooShort ->
                            stringResource(
                                R.string.auth_error_password_too_short,
                                error.minLength
                            )

                        else -> null
                    },
                    onImeDone = { }
                )

                Spacer(modifier = Modifier.height(spaceS))

                PasswordTextField(
                    value = uiState.confirmPassword,
                    onValueChange = {
                        viewModel.onEvent(
                            RegisterUserEvent.ConfirmPasswordChanged(it)
                        )
                    },
                    isError = uiState.confirmPasswordError != null,
                    supportingText = when (val error = uiState.passwordError) {
                        is ValidationError.EmptyPassword ->
                            stringResource(R.string.auth_error_password_required)

                        is ValidationError.PasswordTooShort ->
                            stringResource(
                                R.string.auth_error_password_too_short,
                                error.minLength
                            )

                        else -> null
                    },
                    onImeDone = { viewModel.onEvent(RegisterUserEvent.Submit) }
                )

                Spacer(modifier = Modifier.height(spaceM))

                Button(
                    onClick = { viewModel.onEvent(RegisterUserEvent.Submit) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .padding(horizontal = spaceM)
                        .fillMaxWidth()
                        .height(buttonHeight)
                ) {
                    Text(
                        text = stringResource(R.string.auth_register_button),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Normal
                        )
                    )
                }
            }
        }
    }

}