package com.laguipemo.nefroped.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.laguipemo.nefroped.features.auth.recoverpassword.ResetPasswordScreen
import com.laguipemo.nefroped.features.chat.ChatScreen
import com.laguipemo.nefroped.features.course.CourseScreen
import com.laguipemo.nefroped.features.course.lessons.LessonsListScreen
import com.laguipemo.nefroped.features.course.lessons.detail.LessonDetailScreen
import com.laguipemo.nefroped.features.profile.ProfileScreen

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
                }
            )
        }

        composable<AuthenticatedRoute.Lessons> { backStackEntry ->
            val route = backStackEntry.toRoute<AuthenticatedRoute.Lessons>()
            LessonsListScreen(
                topicId = route.topicId,
                onBackClick = { navController.popBackStack() },
                onLessonClick = { lessonId ->
                    // Siempre permitimos navegar al detalle, esté completada o no
                    navController.navigate(AuthenticatedRoute.LessonDetail(lessonId))
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

        composable<AuthenticatedRoute.Profile> {
            ProfileScreen(
                onOpenChat = {
                    navController.navigate(
                        AuthenticatedRoute.Chat("default")
                    )
                }
            )
        }

        composable<AuthenticatedRoute.Chat> {
            ChatScreen()
        }

        composable<AuthenticatedRoute.ResetPassword>(
            deepLinks = listOf(
                navDeepLink { uriPattern = "nefroped://reset-password" }
            )
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
