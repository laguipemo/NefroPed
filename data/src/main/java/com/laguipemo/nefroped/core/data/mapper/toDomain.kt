package com.laguipemo.nefroped.core.data.mapper

import com.laguipemo.nefroped.core.domain.model.user.User
import io.github.jan.supabase.auth.user.UserInfo

fun UserInfo.toDomain(): User {
    return User(
        id = id,
        email = email ?: "",
        displayName = ""
    )
}