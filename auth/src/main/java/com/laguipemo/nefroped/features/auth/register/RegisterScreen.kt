package com.laguipemo.nefroped.features.auth.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laguipemo.nefroped.core.domain.model.util.ValidationError
import com.laguipemo.nefroped.features.auth.components.EmailTextField
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
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
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
                        "Email es obligatorio"

                    is ValidationError.InvalidEmailFormat ->
                        "Email no es válido"

                    else -> null
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            PasswordTextField(
                value = uiState.password,
                onValueChange = {
                    viewModel.onEvent(
                        RegisterUserEvent.PasswordChanged(it)
                    )
                },
                isError = uiState.passwordError != null,
                supportingText = when (uiState.passwordError) {
                    is ValidationError.EmptyPassword ->
                        "Password es obligatorio"

                    is ValidationError.PasswordTooShort ->
                        "El password debe tener al menos ${(uiState.passwordError as ValidationError.PasswordTooShort).minLength} caracteres"

                    else -> null
                },
                onImeDone = { }
            )
            Spacer(modifier = Modifier.height(8.dp))
            PasswordTextField(
                value = uiState.confirmPassword,
                onValueChange = {
                    viewModel.onEvent(
                        RegisterUserEvent.ConfirmPasswordChanged(it)
                    )
                },
                isError = uiState.confirmPasswordError != null,
                supportingText = when (uiState.confirmPasswordError) {
                    is ValidationError.EmptyPassword ->
                        "Password es obligatorio"

                    is ValidationError.PasswordDoNotMatch ->
                        "El password no coincide"

                    else -> null
                }
            ) {
                viewModel.onEvent(RegisterUserEvent.Submit)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.onEvent(RegisterUserEvent.Submit)
                },
                enabled = !uiState.isLoading
            ) {
                Text("Registar")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator()
            }
        }
    }

}