package com.laguipemo.nefroped.features.admin.clinical

import com.laguipemo.nefroped.core.domain.model.course.Quiz

data class AdminClinicalCaseFormUiState(
    val id: String = "",
    val topicId: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val quizId: String? = null,
    
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    
    val availableQuizzes: List<Quiz> = emptyList()
)
