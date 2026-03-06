package com.laguipemo.nefroped.features.chat.util

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Locale.getDefault
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.minus


fun formatTime(instant: Instant): String {
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = localDateTime.hour.toString().padStart(2, '0')
    val minute = localDateTime.minute.toString().padStart(2, '0')
    return "$hour:$minute"
}

fun formatDateHeader(instant: Instant): String {
    val now =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val messageDate =
        instant.toLocalDateTime(TimeZone.currentSystemDefault()).date

    return when (messageDate) {
        now -> "Hoy"
        now.minus(1, DateTimeUnit.DAY) -> "Ayer"
        else -> "${messageDate.dayOfMonth} de ${
            messageDate.month.name.lowercase().replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(getDefault()) else it.toString()
            }
        }"
    }
}