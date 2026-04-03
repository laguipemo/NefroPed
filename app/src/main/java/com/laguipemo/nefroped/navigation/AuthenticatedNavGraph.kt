package com.laguipemo.nefroped.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.laguipemo.nefroped.features.auth.recoverpassword.ResetPasswordScreen
import com.laguipemo.nefroped.features.chat.ChatScreen
import com.laguipemo.nefroped.features.course.CourseScreen
import com.laguipemo.nefroped.features.course.clinical.ClinicalCaseListScreen
import com.laguipemo.nefroped.features.course.lessons.LessonsListScreen
import com.laguipemo.nefroped.features.course.lessons.detail.LessonDetailScreen
import com.laguipemo.nefroped.features.course.quiz.QuizScreen
import com.laguipemo.nefroped.features.profile.ProfileScreen
import com.laguipemo.nefroped.core.domain.model.notification.NotificationType
import com.laguipemo.nefroped.features.admin.AdminDashboardScreen
import com.laguipemo.nefroped.features.admin.topics.AdminTopicsScreen
import com.laguipemo.nefroped.features.notifications.NotificationsScreen

@Composable
fun AuthenticatedNavGraph(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AuthenticatedRoute.Course,
        modifier = modifier,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { fadeOut() }
    ) {
        composable<AuthenticatedRoute.Course> {
            CourseScreen(
                onTopicClick = { topicId ->
                    navController.navigate(AuthenticatedRoute.Lessons(topicId))
                },
                onChatClick = { conversationId, topicTitle ->
                    navController.navigate(
                        AuthenticatedRoute.Chat(
                            conversationId = conversationId,
                            topicTitle = topicTitle
                        )
                    )
                },
                onClinicalCasesClick = { topicId ->
                    navController.navigate(AuthenticatedRoute.ClinicalCaseList(topicId))
                },
                onNotificationsClick = {
                    navController.navigate(AuthenticatedRoute.Notifications)
                }
            )
        }

        composable<AuthenticatedRoute.Lessons> { backStackEntry ->
            val route = backStackEntry.toRoute<AuthenticatedRoute.Lessons>()
            LessonsListScreen(
                topicId = route.topicId,
                onBackClick = { navController.popBackStack() },
                onLessonClick = { lessonId ->
                    navController.navigate(AuthenticatedRoute.LessonDetail(lessonId))
                },
                onQuizClick = { topicId, title ->
                    navController.navigate(AuthenticatedRoute.Quiz(id = topicId, isTopicId = true, title = title))
                }
            )
        }

        composable<AuthenticatedRoute.ClinicalCaseList> { backStackEntry ->
            val route = backStackEntry.toRoute<AuthenticatedRoute.ClinicalCaseList>()
            ClinicalCaseListScreen(
                onBackClick = { navController.popBackStack() },
                onCaseClick = { quizId, title ->
                    navController.navigate(AuthenticatedRoute.Quiz(id = quizId, isTopicId = false, title = title))
                }
            )
        }

        composable<AuthenticatedRoute.LessonDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<AuthenticatedRoute.LessonDetail>()
            LessonDetailScreen(
                lessonId = route.lessonId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<AuthenticatedRoute.Quiz> {
            QuizScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<AuthenticatedRoute.Profile> {
            ProfileScreen(
                onOpenChat = {
                    navController.navigate(AuthenticatedRoute.Chat(conversationId = "general"))
                },
                onNavigateToAdmin = {
                    navController.navigate(AuthenticatedRoute.Admin)
                }
            )
        }

        composable<AuthenticatedRoute.Chat> { backStackEntry ->
            val route = backStackEntry.toRoute<AuthenticatedRoute.Chat>()
            val showBack = route.conversationId != "general"
            
            ChatScreen(
                onBackClick = if (showBack) { { navController.popBackStack() } } else null
            )
        }

        composable<AuthenticatedRoute.Notifications> {
            NotificationsScreen(
                onBackClick = { navController.popBackStack() },
                onNotificationClick = { notification ->
                    when (notification.type) {
                        NotificationType.CHAT_REPLY -> {
                            val chatId = notification.payload["conversation_id"] ?: "general"
                            val topicTitle = notification.payload["topic_title"] ?: "Tema"
                            navController.navigate(
                                AuthenticatedRoute.Chat(
                                    conversationId = chatId,
                                    topicTitle = topicTitle
                                )
                            )
                        }
                        NotificationType.NEW_CONTENT -> {
                            navController.navigate(AuthenticatedRoute.Course)
                        }
                        else -> { /* Solo marcar como leída */ }
                    }
                }
            )
        }

        composable<AuthenticatedRoute.Admin> {
            AdminDashboardScreen(
                onBackClick = { navController.popBackStack() },
                onManageTopicsClick = { 
                    navController.navigate(AuthenticatedRoute.AdminTopics)
                },
                onManageQuizzesClick = { /* TODO */ },
                onManageClinicalCasesClick = { /* TODO */ }
            )
        }

        composable<AuthenticatedRoute.AdminTopics> {
            AdminTopicsScreen(
                onBackClick = { navController.popBackStack() },
                onAddTopicClick = { 
                    navController.navigate(AuthenticatedRoute.AdminTopicForm(null)) 
                },
                onTopicClick = { topicId ->
                    navController.navigate(AuthenticatedRoute.AdminTopicForm(topicId))
                }
            )
        }

        composable<AuthenticatedRoute.AdminTopicForm> { backStackEntry ->
            val route = backStackEntry.toRoute<AuthenticatedRoute.AdminTopicForm>()
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Formulario de Tema: ${route.topicId ?: "Nuevo"}", color = Color.White)
            }
        }

        composable<AuthenticatedRoute.ResetPassword>(
            deepLinks = listOf(navDeepLink { uriPattern = "nefroped://reset-password" })
        ) {
            ResetPasswordScreen(
                onResetSuccess = {
                    navController.navigate(AuthenticatedRoute.Course) {
                        popUpTo(AuthenticatedRoute.ResetPassword) { inclusive = true }
                    }
                }
            )
        }
    }
}
