package com.laguipemo.nefroped.features.profile.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laguipemo.nefroped.core.domain.model.notification.Notification
import com.laguipemo.nefroped.core.domain.usecase.notification.MarkNotificationAsReadUseCase
import com.laguipemo.nefroped.core.domain.usecase.notification.ObserveNotificationsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val observeNotifications: ObserveNotificationsUseCase,
    private val markAsRead: MarkNotificationAsReadUseCase
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

    fun onNotificationRead(id: String) {
        viewModelScope.launch {
            markAsRead(id)
        }
    }
}
