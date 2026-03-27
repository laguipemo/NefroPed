package com.laguipemo.nefroped.features.profile.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.laguipemo.nefroped.core.domain.model.util.ValidationError
import com.laguipemo.nefroped.designsystem.R
import com.laguipemo.nefroped.designsystem.components.EmailTextField
import com.laguipemo.nefroped.designsystem.components.HorizontalDiv
import com.laguipemo.nefroped.designsystem.components.PasswordTextField
import com.laguipemo.nefroped.designsystem.components.SocialMediaButton
import com.laguipemo.nefroped.features.profile.ProfileUiState

@Composable
fun LinkAccountSheetContent(
    state: ProfileUiState.Content,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLinkEmailPassword: () -> Unit,
    onLinkGoogle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding() // Asegura que el contenido no quede bajo los botones del sistema
            .padding(horizontal = dimensionResource(R.dimen.space_l))
            .padding(bottom = 32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.profile_link_sheet_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.profile_link_sheet_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_xl)))

        SocialMediaButton(
            onClick = onLinkGoogle,
            text = stringResource(R.string.profile_link_google),
            icon = R.drawable.ic_google,
            color = colorResource(R.color.bg_btn_google)
        )

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_l)))

        HorizontalDiv()

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_l)))

        EmailTextField(
            value = state.formEmail,
            onValueChange = onEmailChanged,
            isError = state.emailError != null,
            supportingText = when (state.emailError) {
                ValidationError.EmptyEmail -> stringResource(R.string.auth_error_email_required)
                ValidationError.InvalidEmailFormat -> stringResource(R.string.auth_error_email_invalid)
                else -> null
            },
            isDarkBackground = false
        )

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_m)))

        PasswordTextField(
            value = state.formPassword,
            onValueChange = onPasswordChanged,
            isError = state.passwordError != null,
            supportingText = when (val error = state.passwordError) {
                ValidationError.EmptyPassword -> stringResource(R.string.auth_error_password_required)
                is ValidationError.PasswordTooShort -> stringResource(R.string.auth_error_password_too_short, error.minLength)
                else -> null
            },
            onImeDone = onLinkEmailPassword,
            label = stringResource(R.string.auth_new_password_label),
            isDarkBackground = false
        )

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_xl)))

        Button(
            onClick = onLinkEmailPassword,
            modifier = Modifier
                .padding(horizontal = dimensionResource(R.dimen.space_m))
                .fillMaxWidth(),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(4.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(stringResource(R.string.profile_link_email))
            }
        }
    }
}
