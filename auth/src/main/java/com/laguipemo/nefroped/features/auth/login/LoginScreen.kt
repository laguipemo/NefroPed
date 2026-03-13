package com.laguipemo.nefroped.features.auth.login

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
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
import com.laguipemo.nefroped.designsystem.components.HeaderAuth
import com.laguipemo.nefroped.designsystem.components.HorizontalDiv
import com.laguipemo.nefroped.designsystem.components.PasswordTextField
import com.laguipemo.nefroped.designsystem.components.SocialMediaButton
import com.laguipemo.nefroped.designsystem.util.toMessage
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    onLoginSuccess: () -> Unit,
    onRegister: () -> Unit,
    onRecoverPassword: () -> Unit = {},
    onContinueWithGoogle: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val horizontalPadding = dimensionResource(R.dimen.screen_horizontal_padding)
    val verticalPadding = dimensionResource(R.dimen.screen_vertical_padding)
    val spaceS = dimensionResource(R.dimen.space_s)
    val spaceM = dimensionResource(R.dimen.space_m)
    val spaceL = dimensionResource(R.dimen.space_l)
    val spaceXL = dimensionResource(R.dimen.space_xl)
    val buttonHeight = dimensionResource(R.dimen.button_height)

    LaunchedEffect(Unit) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is LoginUiEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.error.toMessage())
                }

                LoginUiEffect.LoginSuccess -> {
                    onLoginSuccess()
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
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column {
                HeaderAuth(stringResource(R.string.auth_title_login))
                Spacer(modifier = Modifier.height(spaceXL))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    EmailTextField(
                        value = uiState.email,
                        onValueChange = {
                            viewModel.onEvent(
                                LoginUserEvent.EmailChanged(
                                    it
                                )
                            )
                        },
                        isError = uiState.emailError != null,
                        supportingText = when (uiState.emailError) {
                            ValidationError.EmptyEmail -> stringResource(R.string.auth_error_email_required)
                            ValidationError.InvalidEmailFormat -> stringResource(
                                R.string.auth_error_email_invalid
                            )

                            else -> null
                        }
                    )
                    Spacer(modifier = Modifier.height(spaceS))
                    PasswordTextField(
                        value = uiState.password,
                        onValueChange = {
                            viewModel.onEvent(
                                LoginUserEvent.PasswordChanged(
                                    it
                                )
                            )
                        },
                        isError = uiState.passwordError != null,
                        supportingText = when (val error =
                            uiState.passwordError) {
                            ValidationError.EmptyPassword -> stringResource(R.string.auth_error_password_required)
                            is ValidationError.PasswordTooShort -> stringResource(
                                R.string.auth_error_password_too_short,
                                error.minLength
                            )

                            else -> null
                        },
                        onImeDone = { viewModel.onEvent(LoginUserEvent.Submit) }
                    )
                    Spacer(modifier = Modifier.height(spaceS))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        val recoverText = buildAnnotatedString {
                            append(stringResource(R.string.auth_forgot_password_question) + " ")
                            pushLink(
                                LinkAnnotation.Clickable(
                                    tag = "recover_password",
                                    styles = TextLinkStyles(
                                        style = SpanStyle(
                                            color = MaterialTheme.colorScheme.primary,
                                            textDecoration = TextDecoration.Underline
                                        )
                                    ),
                                    linkInteractionListener = { onRecoverPassword() }
                                )
                            )
                            append(stringResource(R.string.auth_forgot_password_action))
                            pop()
                        }
                        Text(text = recoverText)
                    }
                    Spacer(Modifier.height(spaceM))
                    Button(
                        onClick = { viewModel.onEvent(LoginUserEvent.Submit) },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .padding(horizontal = spaceM)
                            .fillMaxWidth()
                            .height(buttonHeight)
                    ) {
                        Text(text = stringResource(R.string.auth_login_button))
                    }
                    Spacer(modifier = Modifier.height(spaceL))
                    HorizontalDiv()
                    Spacer(modifier = Modifier.height(spaceL))
                    SocialMediaButton(
                        onClick = { viewModel.onEvent(LoginUserEvent.ContinueAsGuest) },
                        text = stringResource(R.string.auth_continue_guest),
                        icon = R.drawable.ic_incognito,
                        color = colorResource(R.color.bg_btn_incognito)
                    )
                    Spacer(modifier = Modifier.height(spaceS))
                    SocialMediaButton(
                        onClick = {
                            scope.launch {
                                handleGoogleLogin(context, viewModel)
                            }
                        },
                        text = stringResource(R.string.auth_continue_google),
                        icon = R.drawable.ic_google,
                        color = colorResource(R.color.bg_btn_google)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val createAccount = buildAnnotatedString {
                    append(stringResource(R.string.auth_no_account_question) + " ")
                    pushLink(
                        LinkAnnotation.Clickable(
                            tag = "create_account",
                            styles = TextLinkStyles(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.Underline
                                )
                            ),
                            linkInteractionListener = { onRegister() }
                        )
                    )
                    append(stringResource(R.string.auth_register_action))
                    pop()
                }
                Text(text = createAccount)
                Spacer(Modifier.height(spaceS))
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

private suspend fun handleGoogleLogin(
    context: Context,
    viewModel: LoginViewModel
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
    viewModel: LoginViewModel
) {
    val credential = result.credential

    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        try {
            val googleIdTokenCredential =
                GoogleIdTokenCredential.createFrom(credential.data)
            viewModel.onEvent(
                LoginUserEvent.LoginWithGoogle(
                    googleIdTokenCredential.idToken
                )
            )
        } catch (_: Exception) {
        }
    }
}
