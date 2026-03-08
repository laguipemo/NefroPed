package com.laguipemo.nefroped.features.auth.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laguipemo.nefroped.core.domain.model.util.ValidationError
import com.laguipemo.nefroped.designsystem.R
import com.laguipemo.nefroped.features.auth.components.EmailTextField
import com.laguipemo.nefroped.features.auth.components.HeaderAuth
import com.laguipemo.nefroped.features.auth.components.PasswordTextField
import com.laguipemo.nefroped.features.auth.components.SocialMediaButton
import com.laguipemo.nefroped.features.auth.util.toMessage
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

    val horizontalPadding = dimensionResource(
        R.dimen.screen_horizontal_padding
    )
    val verticalPadding = dimensionResource(
        R.dimen.screen_vertical_padding
    )
    val spaceS = dimensionResource(R.dimen.space_s)
    val spaceM = dimensionResource(R.dimen.space_m)
    val spaceL = dimensionResource(R.dimen.space_l)
    val buttonHeight = dimensionResource(R.dimen.button_height)

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
                .fillMaxSize()
                .padding(
                    horizontal = horizontalPadding,
                    vertical = verticalPadding
                ),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // HEADER: título + logo + subtítulo
            HeaderAuth(
                stringResource(R.string.auth_title_login)
            )

            // FORM + BOTONES
            Column(
                modifier = Modifier.fillMaxWidth(),
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
                            stringResource(R.string.auth_error_email_required)

                        ValidationError.InvalidEmailFormat ->
                            stringResource(R.string.auth_error_email_invalid)

                        else -> null
                    },
                )

                Spacer(modifier = Modifier.height(spaceS))

                PasswordTextField(
                    value = uiState.password,
                    onValueChange = {
                        viewModel.onEvent(
                            LoginUserEvent.PasswordChanged(it)
                        )
                    },
                    isError = uiState.passwordError != null,
                    supportingText = when (val error = uiState.passwordError) {
                        ValidationError.EmptyPassword ->
                            stringResource(R.string.auth_error_password_required)

                        is ValidationError.PasswordTooShort ->
                            stringResource(
                                R.string.auth_error_password_too_short,
                                error.minLength
                            )

                        else -> null
                    },
                    onImeDone = { viewModel.onEvent(LoginUserEvent.Submit) }
                )

                Spacer(modifier = Modifier.height(spaceS))

                // ¿Olvidaste tu contraseña? Recupérala
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    val recoverText = buildAnnotatedString {
                        append(
                            stringResource(
                                com.laguipemo.nefroped.designsystem.R.string
                                    .auth_forgot_password_question
                            ) + " "
                        )
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
                        append(
                            stringResource(
                                com.laguipemo.nefroped.designsystem.R.string
                                    .auth_forgot_password_action
                            )
                        )
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
                    Text(
                        text = stringResource(R.string.auth_login_button),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Normal
                        )
                    )
                }

                Spacer(modifier = Modifier.height(spaceL))

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.space_48))
                )

                Spacer(modifier = Modifier.height(spaceL))

                SocialMediaButton(
                    onClick = { viewModel.onEvent(LoginUserEvent.ContinueAsGuest) },
                    text = stringResource(R.string.auth_continue_guest),
                    icon = R.drawable.ic_incognito,
                    color = colorResource(R.color.bg_btn_incognito)
                )

                Spacer(modifier = Modifier.height(spaceS))

                SocialMediaButton(
                    onClick = onContinueWithGoogle,
                    text = stringResource(R.string.auth_continue_google),
                    icon = R.drawable.ic_google,
                    color = colorResource(R.color.bg_btn_google)
                )
            }

            // FOOTER
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val createAccount = buildAnnotatedString {
                    append(
                        stringResource(
                            com.laguipemo.nefroped.designsystem.R.string
                                .auth_no_account_question
                        ) + " "
                    )
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
                    append(
                        stringResource(
                            com.laguipemo.nefroped.designsystem.R.string
                                .auth_register_action
                        )
                    )
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