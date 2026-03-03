package com.laguipemo.nefroped.core.data.repository

import com.laguipemo.nefroped.core.data.dto.MessageDto
import com.laguipemo.nefroped.core.data.dto.toDomain
import com.laguipemo.nefroped.core.domain.model.chat.Message
import com.laguipemo.nefroped.core.domain.repository.chat.ChatRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

class SupabaseChatRepositoryImpl(
    private val supabase: SupabaseClient
) : ChatRepository {

    @OptIn(SupabaseInternal::class)
    override fun observeMessages(
        conversationId: String
    ): Flow<List<Message>> = channelFlow {

        val json = Json { ignoreUnknownKeys = true }

        // Carga inicial ordenada
        val initialMessages = supabase
            .from("messages")
            .select {
                filter { eq("conversation_id", conversationId) }
                order("created_at", Order.ASCENDING)
            }
            .decodeList<MessageDto>()
            .map { it.toDomain() }

        val messages = initialMessages.toMutableList()
        send(messages.toList())

        // Canal realtime
        val channelName = "messages-$conversationId"
        // 2. LIMPIEZA TOTAL (v3.3.0)
        // Recuperamos el canal si ya existe en la caché global de Supabase
        val existingChannel =
            supabase.realtime.subscriptions.getOrDefault(channelName, null)
        if (existingChannel != null) {
            // Forzamos la salida y borrado para que el estado pase a UNSUBSCRIBED
            existingChannel.unsubscribe()
            supabase.realtime.removeChannel(existingChannel)
        }
        // 3. CREACIÓN LIMPIA
        val channel = supabase.realtime.channel(channelName)

        // 4. REGISTRO (Debe ser antes de subscribe)
        val realtimeFlow =
            channel.postgresChangeFlow<PostgresAction.Insert>(
                schema = "public"
            ) {
                table = "messages"
                filter(
                    "conversation_id",
                    FilterOperator.EQ,
                    conversationId
                )
            }
        // 5. SUSCRIPCIÓN
        channel.subscribe()

        // 6. RECOLECCIÓN (Siempre activa mientras el Flow de la UI viva)
        val job = launch {
            realtimeFlow.collect { action ->
                val dto =
                    json.decodeFromJsonElement<MessageDto>(action.record)
                val newMessage = dto.toDomain()

                // 2. Lógica de "UPSERT" local (Update or Insert)
                // Buscamos si ya existe un mensaje con ese clientId (enviado por nosotros)
                // o con ese id (enviado por otros).
                val index = messages.indexOfFirst {
                    it.clientId == newMessage.clientId || (it.id != null && it.id == newMessage.id)
                }

                if (index != -1) {
                    // Si ya existe (ej: era un mensaje local "enviando"), lo actualizamos con los datos de DB
                    messages[index] = newMessage
                } else {
                    // Si es totalmente nuevo (de otro usuario), lo añadimos
                    messages.add(newMessage)
                }

                // 3. Reordenamos y emitimos una copia de la lista
                messages.sortBy { it.createdAt }
                send(messages.toList())
            }
        }

        awaitClose {
            job.cancel()
            // Cleanup asíncrono para evitar el crash de Garbage Collection
            runBlocking {
                channel.unsubscribe()
                supabase.realtime.removeChannel(channel)
            }
        }
    }

    override suspend fun sendMessage(
        conversationId: String,
        content: String,
        clientId: String
    ) {
        val user =
            supabase.auth.currentUserOrNull()
                ?: error("User not authenticated")
        supabase
            .from("messages")
            .insert(
                mapOf(
                    "client_id" to clientId,
                    "conversation_id" to conversationId,
                    "user_id" to user.id,
                    "email" to user.email,
                    "role" to "user",
                    "content" to content
                )
            )
    }
}