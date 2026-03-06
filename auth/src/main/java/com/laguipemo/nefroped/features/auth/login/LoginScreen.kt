package com.laguipemo.nefroped.features.auth.login

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
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laguipemo.nefroped.core.domain.model.util.ValidationError
import com.laguipemo.nefroped.features.auth.components.EmailTextField
import com.laguipemo.nefroped.features.auth.components.PasswordTextField
import com.laguipemo.nefroped.features.auth.util.toMessage
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    onLoginSuccess: () -> Unit,
    onRegister: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is LoginUiEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.error.toMessage())
                }

                LoginUiEffect.LoginSuccess -> {
                    onLoginSuccess() //TODO: Analizar si es necesario algún feedback porque Supabase cambia
                    //      el estado de la sessión y como authState es un reflejo de ese
                    //      estado, pues cambia y este es la fuente de verdad que Auth
                    //      observa.
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
                        LoginUserEvent.EmailChanged(it)
                    )
                },
                isError = uiState.emailError != null,
                supportingText = when (uiState.emailError) {
                    ValidationError.EmptyEmail ->
                        "Email es obligatorio"

                    ValidationError.InvalidEmailFormat ->
                        "Email no es válido"

                    else -> null
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            PasswordTextField(
                value = uiState.password,
                onValueChange = {
                    viewModel.onEvent(
                        LoginUserEvent.PasswordChanged(it)
                    )
                },
                isError = uiState.passwordError != null,
                supportingText = when (uiState.passwordError) {
                    ValidationError.EmptyPassword ->
                        "Password es obligatorio"

                    is ValidationError.PasswordTooShort ->
                        "El password debe tener al menos ${(uiState.passwordError as ValidationError.PasswordTooShort).minLength} caracteres"

                    else -> null
                },
                onImeDone = { viewModel.onEvent(LoginUserEvent.Submit) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.onEvent(
                        LoginUserEvent.Submit
                    )
                },
                enabled = !uiState.isLoading

            ) {
                Text("Login")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.onEvent(
                        LoginUserEvent.ContinueAsGuest
                    )
                },
                enabled = !uiState.isLoading

            ) {
                Text("Continuar como Invitado")
            }

            Spacer(modifier = Modifier.height(16.dp))

            val createAccount = buildAnnotatedString {
                append("¿No tienes una cuenta? ")
                pushLink(
                    LinkAnnotation.Clickable(
                        tag = "create_account",
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline
                            )
                        ),
                        linkInteractionListener = {
                            onRegister()
                        }
                    )
                )
                append("Regístrate")
                pop()
            }
            Text(text = createAccount)

            Spacer(modifier = Modifier.height(16.dp))
            if (uiState.isLoading) {
                CircularProgressIndicator()
            }

        }
    }

}