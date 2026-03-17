package com.laguipemo.nefroped.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: Any,
    val title: String,
    val icon: ImageVector
) {
    data object Course : BottomNavItem(AuthenticatedRoute.Course, "Curso", Icons.Default.School)
    data object Chat : BottomNavItem(AuthenticatedRoute.Chat("default"), "Consultas", Icons.AutoMirrored.Filled.Chat)
    data object Profile : BottomNavItem(AuthenticatedRoute.Profile, "Perfil", Icons.Default.Person)
}

val bottomNavItems = listOf(
    BottomNavItem.Course,
    BottomNavItem.Chat,
    BottomNavItem.Profile
)
