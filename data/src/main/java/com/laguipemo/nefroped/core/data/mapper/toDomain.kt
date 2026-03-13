package com.laguipemo.nefroped.core.data.mapper

import com.laguipemo.nefroped.core.domain.model.user.User
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

fun UserInfo.toDomain(): User {
    val metadata = userMetadata
    val fullName = metadata?.get("full_name")?.jsonPrimitive?.contentOrNull
    val avatarUrl = metadata?.get("avatar_url")?.jsonPrimitive?.contentOrNull
    
    return User(
        id = id,
        email = email,
        displayName = fullName,
        avatarUrl = avatarUrl,
        isAnonymous = identities.isNullOrEmpty()
    )
}