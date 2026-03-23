package com.laguipemo.nefroped.features.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.laguipemo.nefroped.core.domain.model.chat.Message
import com.laguipemo.nefroped.features.chat.util.formatTime
import kotlin.math.abs

@Composable
fun MessageItem(
    message: Message,
    isMine: Boolean
) {
    val senderName = remember(message.email, message.userId) {
        when {
            message.email.isNotBlank() -> message.email.split("@")[0]
            !message.userId.isNullOrBlank() -> {
                val shortId = if (message.userId!!.length >= 4) {
                    message.userId!!.substring(0, 4).uppercase()
                } else {
                    message.userId!!
                }
                "Invitado-$shortId"
            }
            else -> "Invitado"
        }
    }

    val nameColor = remember(message.userId) {
        if (message.email.isBlank() && !message.userId.isNullOrBlank()) {
            val hash = message.userId!!.hashCode()
            Color(
                red = (abs(hash * 31) % 180) / 255f,
                green = (abs(hash * 17) % 180) / 255f,
                blue = (abs(hash * 13) % 180) / 255f,
                alpha = 1.0f
            )
        } else {
            Color(0xFF005AC1)
        }
    }

    val backgroundColor = when {
        message.isError -> MaterialTheme.colorScheme.errorContainer
        isMine -> if (message.id.startsWith("local-")) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        } else {
            MaterialTheme.colorScheme.primaryContainer
        }
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isMine) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val chatShape = if (isMine) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp), // Pequeño padding interno extra
        contentAlignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
        ) {
            if (!isMine) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = senderName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp // Ajustado a 12sp para que no sea más ancho que mensajes cortos
                        ),
                        color = nameColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .widthIn(min = 60.dp) // Asegura un ancho mínimo para mensajes muy cortos
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

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = formatTime(message.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
                modifier = Modifier.size(10.dp),
                strokeWidth = 1.dp,
                color = MaterialTheme.colorScheme.primary
            )
        }
        message.isError -> {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
