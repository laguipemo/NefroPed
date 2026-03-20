package com.laguipemo.nefroped.features.course.clinical.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.laguipemo.nefroped.core.domain.model.course.ComplementaryResource
import com.laguipemo.nefroped.designsystem.R

@Composable
fun ResourceItem(
    resource: ComplementaryResource,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(dimensionResource(R.dimen.quiz_option_corner_radius)),
        color = Color.White.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(
            width = dimensionResource(R.dimen.border_stroke_width), 
            color = Color.White.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(dimensionResource(R.dimen.space_m)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Launch,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(dimensionResource(R.dimen.button_icon_size))
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.space_m)))
            Text(
                text = resource.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
