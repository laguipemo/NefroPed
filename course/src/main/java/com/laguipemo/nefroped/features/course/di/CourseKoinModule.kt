package com.laguipemo.nefroped.features.course.di

import com.laguipemo.nefroped.features.course.CourseViewModel
import com.laguipemo.nefroped.features.course.lessons.LessonsViewModel
import com.laguipemo.nefroped.features.course.lessons.detail.LessonDetailViewModel
import com.laguipemo.nefroped.features.course.quiz.QuizViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val courseKoinModule = module {
    // HttpClient para descargar Markdown
    single { HttpClient(OkHttp) }

    viewModelOf(::CourseViewModel)
    viewModel { (topicId: String) -> LessonsViewModel(topicId, get(), get()) }
    
    viewModel { (lessonId: String) ->
        LessonDetailViewModel(
            lessonId,
            get(),
            get(),
            get()
        )
    }

    viewModelOf(::QuizViewModel)
}
