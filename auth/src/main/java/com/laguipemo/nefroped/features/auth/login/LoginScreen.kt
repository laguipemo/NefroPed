package com.laguipemo.nefroped.features.auth.login

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.laguipemo.nefroped.core.domain.model.util.ValidationError
import com.laguipemo.nefroped.designsystem.R
import com.laguipemo.nefroped.designsystem.components.*
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

    LaunchedEffect(Unit) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is LoginUiEffect.ShowError -> snackbarHostState.showSnackbar(effect.error.toMessage())
                LoginUiEffect.LoginSuccess -> onLoginSuccess()
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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
                .padding( bottom = dimensionResource(R.dimen.space_m))
                .padding(horizontal = dimensionResource(R.dimen.screen_horizontal_padding))
        ) {
            val minHeight = maxHeight
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.heightIn(min = minHeight),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.auth_header_padding_top)))
                    
                    HeaderAuth(stringResource(R.string.auth_title_login))
                    
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_xl)))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(dimensionResource(R.dimen.quiz_card_corner_radius)),
                        color = Color.White.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(
                            width = dimensionResource(R.dimen.border_stroke_width), 
                            color = Color.White.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(dimensionResource(R.dimen.space_m))) {
                            LoginForm(
                                uiState = uiState,
                                onEvent = viewModel::onEvent,
                                onRecoverPassword = onRecoverPassword
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_m)))
                    HorizontalDiv()
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_m)))

                    SocialLoginSection(
                        onContinueAsGuest = { viewModel.onEvent(LoginUserEvent.ContinueAsGuest) },
                        onGoogleLogin = { scope.launch { handleGoogleLogin(context, viewModel) } }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    CreateAccountFooter(onRegister = onRegister, isLoading = uiState.isLoading)
                    
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_m)))
                }
            }
        }
    }
}

@Composable
private fun LoginForm(
    uiState: LoginUiState,
    onEvent: (LoginUserEvent) -> Unit,
    onRecoverPassword: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.space_s))
    ) {
        EmailTextField(
            value = uiState.email,
            onValueChange = { onEvent(LoginUserEvent.EmailChanged(it)) },
            isError = uiState.emailError != null,
            supportingText = when (uiState.emailError) {
                ValidationError.EmptyEmail -> stringResource(R.string.auth_error_email_required)
                ValidationError.InvalidEmailFormat -> stringResource(R.string.auth_error_email_invalid)
                else -> null
            }
        )

        PasswordTextField(
            value = uiState.password,
            onValueChange = { onEvent(LoginUserEvent.PasswordChanged(it)) },
            isError = uiState.passwordError != null,
            supportingText = when (val error = uiState.passwordError) {
                ValidationError.EmptyPassword -> stringResource(R.string.auth_error_password_required)
                is ValidationError.PasswordTooShort -> stringResource(R.string.auth_error_password_too_short, error.minLength)
                else -> null
            },
            onImeDone = { onEvent(LoginUserEvent.Submit) }
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            val recoverText = buildAnnotatedString {
                append(stringResource(R.string.auth_forgot_password_question) + " ")
                pushLink(
                    LinkAnnotation.Clickable(
                        tag = "recover_password",
                        styles = TextLinkStyles(style = SpanStyle(
                            color = Color.White, 
                            fontWeight = FontWeight.Bold, 
                            textDecoration = TextDecoration.Underline
                        )),
                        linkInteractionListener = { onRecoverPassword() }
                    )
                )
                append(stringResource(R.string.auth_forgot_password_action))
                pop()
            }
            Text(text = recoverText, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_s)))

        Button(
            onClick = { onEvent(LoginUserEvent.Submit) },
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.button_height)),
            shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(text = stringResource(R.string.auth_login_button), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SocialLoginSection(onContinueAsGuest: () -> Unit, onGoogleLogin: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.space_s))) {
        SocialMediaButton(
            onClick = onContinueAsGuest,
            text = stringResource(R.string.auth_continue_guest),
            icon = R.drawable.ic_incognito,
            color = colorResource(R.color.bg_btn_incognito)
        )
        SocialMediaButton(
            onClick = onGoogleLogin,
            text = stringResource(R.string.auth_continue_google),
            icon = R.drawable.ic_google,
            color = Color.White
        )
    }
}

@Composable
private fun CreateAccountFooter(onRegister: () -> Unit, isLoading: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val createAccount = buildAnnotatedString {
            append(stringResource(R.string.auth_no_account_question) + " ")
            pushLink(
                LinkAnnotation.Clickable(
                    tag = "create_account",
                    styles = TextLinkStyles(style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary, 
                        fontWeight = FontWeight.ExtraBold, 
                        textDecoration = TextDecoration.Underline
                    )),
                    linkInteractionListener = { onRegister() }
                )
            )
            append(stringResource(R.string.auth_register_action))
            pop()
        }
        Text(
            text = createAccount, 
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (isLoading) {
            Spacer(Modifier.height(dimensionResource(R.dimen.space_s)))
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}

private suspend fun handleGoogleLogin(context: Context, viewModel: LoginViewModel) {
    val credentialManager = androidx.credentials.CredentialManager.create(context)
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId("439824105960-rto1l6vlrkp59kplrm243dlvamf1ek4v.apps.googleusercontent.com")
        .build()
    val request = androidx.credentials.GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()
    try {
        val result = credentialManager.getCredential(context = context, request = request)
        handleCredential(result, viewModel)
    } catch (e: Exception) {
        Log.e("LoginScreen", "Google Login Exception: ${e.message}", e)
    }
}

private fun handleCredential(result: androidx.credentials.GetCredentialResponse, viewModel: LoginViewModel) {
    val credential = result.credential
    if (credential.type == "com.google.android.libraries.identity.googleid.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL") {
        try {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            viewModel.onEvent(LoginUserEvent.LoginWithGoogle(googleIdTokenCredential.idToken))
        } catch (e: Exception) {
            Log.e("LoginScreen", "Handle Credential Error: ${e.message}", e)
        }
    } else {
        Log.d("LoginScreen", "Unexpected credential type: ${credential.type}")
    }
}
