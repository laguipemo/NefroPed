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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laguipemo.nefroped.core.domain.model.util.ValidationError
import com.laguipemo.nefroped.designsystem.R
import com.laguipemo.nefroped.designsystem.components.*
import com.laguipemo.nefroped.designsystem.util.toMessage
import org.koin.androidx.compose.koinViewModel

@Composable
fun RecoverPasswordScreen(
    viewModel: RecoverPasswordViewModel = koinViewModel(),
    onRecoverPasswordSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is RecoverPasswordUiEffect.ShowError -> snackbarHostState.showSnackbar(effect.error.toMessage())
                RecoverPasswordUiEffect.RecoverPasswordSuccess -> onRecoverPasswordSuccess()
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = dimensionResource(R.dimen.screen_horizontal_padding))
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.auth_header_padding_top)))

            HeaderAuth(stringResource(R.string.auth_title_recoverpassword))

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_xl)))

            // CAPA DE CONTRASTE
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimensionResource(R.dimen.quiz_card_corner_radius)),
                color = Color.White.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(dimensionResource(R.dimen.space_m)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.space_m))
                ) {
                    EmailTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.onEvent(RecoverPasswordUserEvent.EmailChanged(it)) },
                        isError = uiState.emailError != null,
                        supportingText = when (uiState.emailError) {
                            ValidationError.EmptyEmail -> stringResource(R.string.auth_error_email_required)
                            ValidationError.InvalidEmailFormat -> stringResource(R.string.auth_error_email_invalid)
                            else -> null
                        }
                    )

                    Button(
                        onClick = { viewModel.onEvent(RecoverPasswordUserEvent.Submit) },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(R.dimen.button_height)),
                        shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = stringResource(R.string.auth_recoverpassword_button),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_m)))
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}
