package com.laguipemo.nefroped.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.laguipemo.nefroped.features.notifications.NotificationViewModel
import com.laguipemo.nefroped.navigation.AuthenticatedNavGraph
import com.laguipemo.nefroped.navigation.AuthenticatedRoute
import com.laguipemo.nefroped.navigation.bottomNavItems
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController(),
    notificationViewModel: NotificationViewModel = koinViewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // CORRECCIÓN: Usamos el conteo filtrado para el chat general
    val generalUnreadCount by notificationViewModel.generalUnreadCount.collectAsStateWithLifecycle()
    
    val isKeyboardOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    val backgroundGradient = Brush.verticalGradient(
        0.0f to MaterialTheme.colorScheme.primary,
        0.4f to MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
        1.0f to MaterialTheme.colorScheme.background
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                AnimatedVisibility(
                    visible = !isKeyboardOpen,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        bottomNavItems.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any { dest ->
                                if (dest.hasRoute<AuthenticatedRoute.Chat>()) {
                                    val route = navBackStackEntry?.toRoute<AuthenticatedRoute.Chat>()
                                    item.route is AuthenticatedRoute.Chat && route?.conversationId == "general"
                                } else {
                                    dest.hasRoute(item.route::class)
                                }
                            } == true

                            NavigationBarItem(
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            // El badge de la BottomBar ahora es INTELIGENTE:
                                            // Solo aparece si la ruta es el Chat y hay notificaciones GENERALES
                                            if (item.route is AuthenticatedRoute.Chat && generalUnreadCount > 0) {
                                                Badge {
                                                    Text(text = generalUnreadCount.toString())
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.title,
                                            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                selected = selected,
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = false
                                        }
                                        launchSingleTop = true
                                        restoreState = false
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            AuthenticatedNavGraph(
                navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
            )
        }
    }
}
