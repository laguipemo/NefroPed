package com.laguipemo.nefroped.features.course.di

import com.laguipemo.nefroped.features.course.CourseViewModel
import com.laguipemo.nefroped.features.course.lessons.LessonsViewModel
import com.laguipemo.nefroped.features.course.lessons.detail.LessonDetailViewModel
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val courseKoinModule = module {
    // HttpClient para descargar Markdown
    single { HttpClient(OkHttp) }

    viewModelOf(::CourseViewModel)
    viewModel { (topicId: String) -> LessonsViewModel(topicId, get(), get()) }
    viewModel { (lessonId: String) -> LessonDetailViewModel(lessonId, get(), get()) }
}
