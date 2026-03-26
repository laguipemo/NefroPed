package com.laguipemo.nefroped.features.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
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
                red = (abs(hash * 31) % 150) / 255f,
                green = (abs(hash * 17) % 150) / 255f,
                blue = (abs(hash * 13) % 150) / 255f,
                alpha = 1.0f
            )
        } else {
            Color(0xFF00458D)
        }
    }

    // ESTRATEGIA DE CONTRASTE: 
    // Usamos tertiaryContainer para MI (que suele ser un tono distinto al azul primario)
    val backgroundColor = when {
        message.isError -> MaterialTheme.colorScheme.errorContainer
        isMine -> MaterialTheme.colorScheme.tertiaryContainer 
        else -> MaterialTheme.colorScheme.surface
    }

    val textColor = if (isMine) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val chatShape = if (isMine) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .widthIn(max = 280.dp),
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
        ) {
            if (!isMine) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = senderName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        ),
                        color = nameColor,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // Globo del mensaje con SOMBRA para despegar del degradado
            Surface(
                tonalElevation = if (isMine) 4.dp else 1.dp,
                shadowElevation = 2.dp,
                shape = chatShape,
                color = backgroundColor,
                border = if (isMine) borderStroke(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)) else null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = formatTime(message.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor.copy(alpha = 0.6f),
                            fontSize = 10.sp
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

// Función auxiliar para el borde sutil
@Composable
private fun borderStroke(color: Color) = androidx.compose.foundation.BorderStroke(0.5.dp, color)

@Composable
private fun StatusIndicator(message: Message) {
    when {
        message.isSending -> {
            CircularProgressIndicator(
                modifier = Modifier.size(10.dp),
                strokeWidth = 1.2.dp,
                color = if (MaterialTheme.colorScheme.onTertiaryContainer != Color.White) MaterialTheme.colorScheme.primary else Color.White
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
