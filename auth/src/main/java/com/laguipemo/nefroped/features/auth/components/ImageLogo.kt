package com.laguipemo.nefroped.features.auth.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.laguipemo.nefroped.designsystem.R

@Composable
fun ImageLogo(
    painterResource: Painter = painterResource(R.drawable.ic_nefroped_logo),
    contentDescription: String = stringResource(R.string.auth_logo_content_description),
    size: Dp = dimensionResource(R.dimen.space_80)
) {
    Image(
        painter = painterResource,
        contentDescription = contentDescription,
        modifier = Modifier
            .padding(vertical = dimensionResource(R.dimen.space_s))
            .size(size)
            .clip(CircleShape)
            .border(
                border = BorderStroke(
                    dimensionResource(R.dimen.border_stroke_width),
                    color = MaterialTheme.colorScheme.primary
                ),
                shape = CircleShape
            )
            .background(MaterialTheme.colorScheme.inverseSurface)
    )
}