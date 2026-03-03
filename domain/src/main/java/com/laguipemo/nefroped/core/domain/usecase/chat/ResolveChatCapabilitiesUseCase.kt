package com.laguipemo.nefroped.core.domain.usecase.chat

import com.laguipemo.nefroped.core.domain.model.chat.ChatCapabilities
import com.laguipemo.nefroped.core.domain.model.session.SessionState

class ResolveChatCapabilitiesUseCase {
    operator fun invoke(sessionState: SessionState): ChatCapabilities =
        when(sessionState) {
            is SessionState.User ->
                if (sessionState.isAnonymous) {
                    ChatCapabilities(
                        canPersistHistory = false,
                        canExport = false,
                        canUseAdvancedModel = false,
                        canAttachFiles = false,
                        messageLimit = 5
                    )
                } else {
                    ChatCapabilities(
                        canPersistHistory = true,
                        canExport = true,
                        canUseAdvancedModel = true,
                        canAttachFiles = true,
                        messageLimit = null
                    )
                }

            else ->
                ChatCapabilities(
                    canPersistHistory = false,
                    canExport = false,
                    canUseAdvancedModel = false,
                    canAttachFiles = false,
                    messageLimit = 0
                )
        }
}