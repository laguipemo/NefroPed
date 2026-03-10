package com.laguipemo.nefroped.features.auth.recoverpassword

interface RecoverPasswordUserEvent {
    data class EmailChanged(val value: String): RecoverPasswordUserEvent
    data object Submit: RecoverPasswordUserEvent
}