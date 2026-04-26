package com.laguipemo.nefroped.features.admin.di

import com.laguipemo.nefroped.features.admin.topics.AdminTopicsViewModel
import com.laguipemo.nefroped.features.admin.topics.AdminTopicFormViewModel
import com.laguipemo.nefroped.features.admin.lessons.AdminLessonFormViewModel
import com.laguipemo.nefroped.features.admin.quizzes.AdminQuizFormViewModel
import com.laguipemo.nefroped.features.admin.quizzes.AdminQuizListViewModel
import com.laguipemo.nefroped.features.admin.clinical.AdminClinicalCaseFormViewModel
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
            observeExternalLinks = get(),
            syncExternalLinks = get(),
            saveExternalLink = get(),
            deleteExternalLink = get(),
            observeClinicalCases = get(),
            syncClinicalData = get(),
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

    viewModel { params ->
        AdminQuizFormViewModel(
            topicId = params.get<String>(),
            quizId = params.getOrNull<String>(),
            observeTopicUseCase = get(),
            observeQuizByTopicUseCase = get(),
            observeQuizByIdUseCase = get(),
            saveQuizUseCase = get(),
            saveQuestionUseCase = get(),
            deleteQuestionUseCase = get(),
            syncQuizUseCase = get(),
            syncQuizByIdUseCase = get()
        )
    }

    viewModelOf(::AdminQuizListViewModel)
    viewModelOf(::AdminClinicalCaseFormViewModel)
}
