package com.laguipemo.nefroped.features.auth.recoverpassword

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
import com.laguipemo.nefroped.features.auth.util.toMessage
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RecoverPasswordScreen(
    viewModel: RecoverPasswordViewModel = koinViewModel(),
    onRecoverPasswordSuccess: () -> Unit
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
    dimensionResource(R.dimen.space_l)
    val buttonHeight = dimensionResource(R.dimen.button_height)

    LaunchedEffect(Unit) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is RecoverPasswordUiEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.error.toMessage())
                }

                RecoverPasswordUiEffect.RecoverPasswordSuccess -> {
                    onRecoverPasswordSuccess()
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
                stringResource(R.string.auth_title_recoverpassword)
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
                            RecoverPasswordUserEvent.EmailChanged(it)
                        )
                    },
                    isError = uiState.emailError != null,
                    supportingText = when (uiState.emailError) {
                        ValidationError.EmptyEmail ->
                            stringResource(R.string.auth_error_email_required)

                        ValidationError.InvalidEmailFormat ->
                            stringResource(R.string.auth_error_email_invalid)

                        else -> null
                    }
                )

                Spacer(Modifier.height(spaceM))

                Button(
                    onClick = { viewModel.onEvent(RecoverPasswordUserEvent.Submit) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .padding(horizontal = spaceM)
                        .fillMaxWidth()
                        .height(buttonHeight)
                ) {
                    Text(
                        text = stringResource(R.string.auth_recoverpassword_button),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Normal
                        )
                    )
                }
            }
        }

    }

}