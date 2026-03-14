package com.laguipemo.nefroped.features.course.di

import com.laguipemo.nefroped.features.course.CourseViewModel
import com.laguipemo.nefroped.features.course.lessons.LessonsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val courseKoinModule = module {
    viewModelOf(::CourseViewModel)
    viewModel { (topicId: String) -> LessonsViewModel(topicId, get(), get()) }
}
