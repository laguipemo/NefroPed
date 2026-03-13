package com.laguipemo.nefroped.core.domain.model.user

data class User(
    val id: String,
    val email: String?,
    val displayName: String?,
    val avatarUrl: String? = null,
    val role: UserRole = UserRole.STUDENT,
    val isAnonymous: Boolean = false
)

enum class UserRole {
    STUDENT, TEACHER, ADMIN
}