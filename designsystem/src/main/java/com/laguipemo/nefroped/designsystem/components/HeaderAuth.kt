package com.laguipemo.nefroped.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.laguipemo.nefroped.designsystem.R

@Composable
fun HeaderAuth(
    subtitle: String,
    name: String = stringResource(R.string.app_name),
    painterResource: Painter = painterResource(R.drawable.ic_nefroped_logo),
    contentDescription: String = stringResource(R.string.auth_logo_content_description),
    size: Dp = dimensionResource(R.dimen.logo_header_size)
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_s)))

        Text(
            text = name,
            style = MaterialTheme.typography.displayMedium.copy(
                color = MaterialTheme.colorScheme.primary
            )
        )

        ImageLogo(
            painterResource = painterResource,
            contentDescription = contentDescription,
            size = size
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.headlineSmall.copy(
                color = MaterialTheme.colorScheme.primary
            )
        )

    }
}