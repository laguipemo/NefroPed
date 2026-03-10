package com.laguipemo.nefroped.features.auth.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.laguipemo.nefroped.designsystem.R

@Composable
fun SocialMediaButton(
    onClick: () -> Unit,
    text: String,
    icon: Int,
    color: Color,
) {

    var click by remember { mutableStateOf(false) }

    val spaceS = dimensionResource(R.dimen.space_s)
    val spaceM = dimensionResource(R.dimen.space_m)
    val spaceL = dimensionResource(R.dimen.space_l)
    val btnIconSize = dimensionResource(R.dimen.button_icon_size)

    Surface(
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = spaceM)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = { click = !click }
            ),
        shape = RoundedCornerShape(
            dimensionResource(R.dimen.button_corner_radius)
        ),
        border = BorderStroke(
            width = dimensionResource(R.dimen.border_stroke_width),
            color = if (icon == R.drawable.ic_incognito) color else Color.Gray
        ),
        color = color
    ) {
        Row(
            modifier = Modifier
                .padding(all = spaceS)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                modifier = Modifier.size(btnIconSize),
                contentDescription = text,
                tint = Color.Unspecified
            )
            Spacer(
                modifier = Modifier
                    .width(spaceM)
            )
            Text(
                text = text,
                color = if (icon == R.drawable.ic_incognito) Color.White else Color.Black
            )
            click = true
        }
    }
}