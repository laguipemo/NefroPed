package com.laguipemo.nefroped.core.data.supabase

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object DeepLinkFlowManager {
    private val _isResetPasswordFlow = MutableStateFlow(false)
    val isResetPasswordFlow = _isResetPasswordFlow.asStateFlow()

    fun setResetPasswordFlow(active: Boolean) {
        _isResetPasswordFlow.value = active
    }
}