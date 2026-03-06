package com.laguipemo.nefroped.features.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.laguipemo.nefroped.core.domain.model.chat.Message
import com.laguipemo.nefroped.features.chat.util.formatTime

@Composable
fun MessageItem(
    message: Message,
    isMine: Boolean
) {
    // 1. Configuración de colores según el emisor y el estado
    val backgroundColor = when {
        message.isError -> MaterialTheme.colorScheme.errorContainer
        // Mensaje del usuario actual (Derecha)
        isMine -> if (message.id.startsWith("local-")) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) // Un tono más suave mientras envía
        } else {
            MaterialTheme.colorScheme.primaryContainer // Color sólido una vez confirmado
        }
        // Mensaje de otros (Izquierda)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isMine) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    // 2. Esquinas asimétricas (el "rabito" del bocadillo)
    val chatShape = if (isMine) {
        RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 16.dp,
            bottomEnd = 2.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 2.dp,
            bottomEnd = 16.dp
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
        ) {
            // Nombre del remitente (solo si no es mío)
            if (!isMine) {
                Text(
                    text = if (message.email.isNotBlank()) message.email.split("@")[0] else "Invitado",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                )
            }

            Box(
                modifier = Modifier
                    .clip(chatShape)
                    .background(backgroundColor)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Metadatos: Hora + Iconos de estado
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = formatTime(message.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = 0.6f
                            )
                        )

                        if (isMine) {
                            StatusIndicator(message)
                        }
                    }
                }
            }
        }

    }
}

@Composable
private fun StatusIndicator(message: Message) {
    when {
        message.isSending -> {
            CircularProgressIndicator(
                modifier = Modifier.size(12.dp),
                strokeWidth = 1.5.dp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        message.isError -> {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
