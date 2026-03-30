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
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

class SupabaseNotificationRepositoryImpl(
    private val supabase: SupabaseClient
) : NotificationRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun observeNotifications(): Flow<List<Notification>> = flow {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return@flow
        
        // 1. Carga inicial de notificaciones no leídas
        val initialNotifications = supabase.from("notifications")
            .select {
                filter {
                    eq("user_id", userId)
                    eq("is_read", false)
                }
            }
            .decodeList<NotificationDto>()
            .map { it.toDomain() }
            .toMutableList()
            
        emit(initialNotifications.toList())

        // 2. Suscripción en tiempo real a nuevas notificaciones
        val channel = supabase.channel("public:notifications")
        val changes = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "notifications"
        }
        
        channel.subscribe()

        changes.collect { action ->
            val dto = json.decodeFromJsonElement<NotificationDto>(action.record)
            if (dto.userId == userId) {
                initialNotifications.add(0, dto.toDomain())
                emit(initialNotifications.toList())
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
}
