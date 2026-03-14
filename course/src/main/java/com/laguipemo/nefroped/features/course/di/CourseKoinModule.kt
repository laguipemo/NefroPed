package com.laguipemo.nefroped.features.course.di

import com.laguipemo.nefroped.features.course.CourseViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val courseKoinModule = module {
    viewModelOf(::CourseViewModel)
}
