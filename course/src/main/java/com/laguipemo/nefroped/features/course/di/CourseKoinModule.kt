package com.laguipemo.nefroped.features.course.di

import com.laguipemo.nefroped.features.course.CourseViewModel
import com.laguipemo.nefroped.features.course.lessons.LessonsViewModel
import com.laguipemo.nefroped.features.course.lessons.detail.LessonDetailViewModel
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
    // Corregido: Ahora pasamos los 4 parámetros (lessonId, GetLesson, MarkAsCompleted, HttpClient)
    viewModel { (lessonId: String) ->
        LessonDetailViewModel(
            lessonId,
            get(),
            get(),
            get()
        )
    }
}
