package com.laguipemo.nefroped.core.data.repository

import com.laguipemo.nefroped.core.data.dto.NotificationDto
import com.laguipemo.nefroped.core.data.mapper.toDomain
import com.laguipemo.nefroped.core.domain.model.notification.Notification
import com.laguipemo.nefroped.core.domain.repository.notification.NotificationRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import java.util.UUID

class SupabaseNotificationRepositoryImpl(
    private val supabase: SupabaseClient
) : NotificationRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun observeNotifications(): Flow<List<Notification>> = channelFlow {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return@channelFlow
        
        // 1. Carga inicial
        val initialNotifications = supabase.from("notifications")
            .select {
                filter { eq("user_id", userId) }
                order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            }
            .decodeList<NotificationDto>()
            .map { it.toDomain() }
            .toMutableList()
            
        send(initialNotifications.toList())

        // 2. Canal con ID único
        val channelName = "notifications-${userId}-${UUID.randomUUID().toString().take(8)}"
        val channel = supabase.realtime.channel(channelName)
        
        // 3. Escuchamos cambios
        val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "notifications"
        }
        
        channel.subscribe()

        val job = launch {
            changes.collect { action ->
                when (action) {
                    is PostgresAction.Insert -> {
                        val dto = json.decodeFromJsonElement<NotificationDto>(action.record)
                        if (dto.userId == userId) {
                            initialNotifications.add(0, dto.toDomain())
                        }
                    }
                    is PostgresAction.Update -> {
                        val dto = json.decodeFromJsonElement<NotificationDto>(action.record)
                        val index = initialNotifications.indexOfFirst { it.id == dto.id }
                        if (index != -1) {
                            initialNotifications[index] = dto.toDomain()
                        }
                    }
                    is PostgresAction.Delete -> {
                        val id = action.oldRecord["id"]?.toString()?.removeSurrounding("\"")
                        initialNotifications.removeAll { it.id == id }
                    }
                    else -> {}
                }
                send(initialNotifications.sortedByDescending { it.createdAt }.toList())
            }
        }

        awaitClose {
            job.cancel()
            runBlocking {
                channel.unsubscribe()
                supabase.realtime.removeChannel(channel)
            }
        }
    }

    override suspend fun markAsRead(id: String): Result<Unit> {
        return try {
            supabase.from("notifications").update(
                mapOf("is_read" to true)
            ) {
                filter { eq("id", id) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNotification(id: String): Result<Unit> {
        return try {
            supabase.from("notifications").delete {
                filter { eq("id", id) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markConversationAsRead(conversationId: String): Result<Unit> {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return Result.failure(Exception("User not authenticated"))
        return try {
            // Actualizamos solo las notificaciones del chat específico
            supabase.from("notifications").update(
                mapOf("is_read" to true)
            ) {
                filter {
                    eq("user_id", userId)
                    eq("is_read", false)
                    // Filtramos por la clave conversation_id dentro del payload JSONB
                    eq("payload->>conversation_id", conversationId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
