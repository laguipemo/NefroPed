package com.laguipemo.nefroped.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laguipemo.nefroped.core.domain.model.notification.Notification
import com.laguipemo.nefroped.core.domain.usecase.notification.DeleteNotificationUseCase
import com.laguipemo.nefroped.core.domain.usecase.notification.MarkConversationAsReadUseCase
import com.laguipemo.nefroped.core.domain.usecase.notification.MarkNotificationAsReadUseCase
import com.laguipemo.nefroped.core.domain.usecase.notification.ObserveNotificationsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val observeNotifications: ObserveNotificationsUseCase,
    private val markAsRead: MarkNotificationAsReadUseCase,
    private val markConversationAsRead: MarkConversationAsReadUseCase,
    private val deleteNotification: DeleteNotificationUseCase
) : ViewModel() {

    val notifications: StateFlow<List<Notification>> = observeNotifications()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val unreadCount: StateFlow<Int> = notifications
        .map { list -> list.count { !it.isRead } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    val generalUnreadCount: StateFlow<Int> = notifications
        .map { list -> 
            list.count { notification -> 
                !notification.isRead && 
                notification.payload["conversation_id"]?.trim() == "general" 
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    fun onNotificationRead(id: String) {
        viewModelScope.launch {
            markAsRead(id)
        }
    }

    fun onEnterConversation(conversationId: String) {
        viewModelScope.launch {
            markConversationAsRead(conversationId)
        }
    }

    fun onNotificationDelete(id: String) {
        viewModelScope.launch {
            deleteNotification(id)
        }
    }
}
