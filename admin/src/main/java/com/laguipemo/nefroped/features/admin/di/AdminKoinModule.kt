package com.laguipemo.nefroped.features.admin.di

import com.laguipemo.nefroped.features.admin.topics.AdminTopicsViewModel
import com.laguipemo.nefroped.features.admin.topics.AdminTopicFormViewModel
import com.laguipemo.nefroped.features.admin.lessons.AdminLessonFormViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val adminKoinModule = module {
    viewModelOf(::AdminTopicsViewModel)
    
    // Definición manual explícita para evitar errores de inyección de parámetros
    viewModel { params -> 
        AdminTopicFormViewModel(
            topicId = params.getOrNull<String>(),
            observeTopic = get(),
            saveTopic = get(),
            repository = get()
        )
    }

    viewModel { params ->
        AdminLessonFormViewModel(
            topicId = params.get<String>(),
            lessonId = params.getOrNull<String>(),
            repository = get()
        )
    }
}
