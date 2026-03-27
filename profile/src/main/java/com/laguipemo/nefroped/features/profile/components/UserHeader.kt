package com.laguipemo.nefroped.features.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.laguipemo.nefroped.designsystem.R
import com.laguipemo.nefroped.features.profile.ProfileUiState

@Composable
fun UserHeader(
    state: ProfileUiState.Content,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.screen_horizontal_padding))
            .padding(vertical = dimensionResource(R.dimen.space_l)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar circular con botón de edición
        Box(
            modifier = Modifier
                .size(dimensionResource(R.dimen.avatar_profile_size))
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f))
                .clickable(enabled = !state.isGuest, onClick = onAvatarClick),
            contentAlignment = Alignment.Center
        ) {
            if (state.avatarUrl != null) {
                AsyncImage(
                    model = state.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(dimensionResource(R.dimen.avatar_icon_size)),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            if (!state.isGuest) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier
                            .padding(bottom = dimensionResource(R.dimen.space_s))
                            .size(dimensionResource(R.dimen.button_icon_size))
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_m)))
        
        // Texto en blanco para resaltar sobre el degradado azul
        Text(
            text = state.userDisplayName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center, // Corregido: Centra el texto si hay varias líneas
            modifier = Modifier.fillMaxWidth()
        )
        
        if (!state.isGuest) {
            Text(
                text = state.userEmail,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        if (state.isGuest) {
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f),
                shape = CircleShape,
                modifier = Modifier.padding(top = dimensionResource(R.dimen.space_s))
            ) {
                Text(
                    text = stringResource(R.string.profile_guest_mode),
                    modifier = Modifier.padding(
                        horizontal = dimensionResource(R.dimen.space_m), 
                        vertical = dimensionResource(R.dimen.space_xs)
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}
