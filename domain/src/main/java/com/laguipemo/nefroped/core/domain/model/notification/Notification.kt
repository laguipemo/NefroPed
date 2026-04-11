package com.laguipemo.nefroped.core.domain.model.notification

import kotlin.time.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())
}

@Serializable
data class Notification(
    val id: String,
    val title: String,
    val body: String,
    val type: NotificationType,
    val isRead: Boolean,
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant,
    val payload: Map<String, String?> = emptyMap()
)

@Serializable
enum class NotificationType {
    NEW_CONTENT,
    CHAT_REPLY,
    SYSTEM,
    OTHER
}
