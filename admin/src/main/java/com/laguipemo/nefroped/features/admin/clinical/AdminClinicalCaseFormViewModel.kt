package com.laguipemo.nefroped.features.admin.clinical

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.laguipemo.nefroped.core.domain.model.course.ClinicalCase
import com.laguipemo.nefroped.core.domain.usecase.course.DeleteClinicalCaseUseCase
import com.laguipemo.nefroped.core.domain.usecase.course.ObserveClinicalCasesUseCase
import com.laguipemo.nefroped.core.domain.usecase.course.SaveClinicalCaseUseCase
import com.laguipemo.nefroped.core.domain.usecase.course.UploadClinicalCaseImageUseCase
import com.laguipemo.nefroped.navigation.AuthenticatedRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class AdminClinicalCaseFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val observeClinicalCases: ObserveClinicalCasesUseCase,
    private val saveClinicalCase: SaveClinicalCaseUseCase,
    private val deleteClinicalCase: DeleteClinicalCaseUseCase,
    private val uploadImage: UploadClinicalCaseImageUseCase
) : ViewModel() {

    private val route = savedStateHandle.toRoute<AuthenticatedRoute.AdminClinicalCaseForm>()
    private val topicId = route.topicId
    private val caseId = route.caseId

    private val _uiState = MutableStateFlow(AdminClinicalCaseFormUiState(topicId = topicId))
    val uiState = _uiState.asStateFlow()

    init {
        if (caseId != null) {
            loadClinicalCase(caseId)
        }
    }

    private fun loadClinicalCase(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val cases = observeClinicalCases(topicId).first()
            val clinicalCase = cases.find { it.id == id }
            
            if (clinicalCase != null) {
                _uiState.update { it.copy(
                    id = clinicalCase.id,
                    title = clinicalCase.title,
                    description = clinicalCase.description,
                    imageUrl = clinicalCase.imageUrl,
                    quizId = clinicalCase.quizId,
                    isLoading = false
                ) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Caso clínico no encontrado") }
            }
        }
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onQuizIdChange(quizId: String?) {
        _uiState.update { it.copy(quizId = quizId) }
    }

    fun onImageSelected(byteArray: ByteArray) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val fileName = "case_${UUID.randomUUID()}.jpg"
            uploadImage(byteArray, fileName)
                .onSuccess { url ->
                    _uiState.update { it.copy(imageUrl = url, isSaving = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isSaving = false) }
                }
        }
    }

    fun save() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val currentState = _uiState.value
            val clinicalCase = ClinicalCase(
                id = currentState.id.ifEmpty { UUID.randomUUID().toString() },
                topicId = topicId,
                title = currentState.title,
                description = currentState.description,
                imageUrl = currentState.imageUrl,
                quizId = currentState.quizId
            )
            
            saveClinicalCase(clinicalCase)
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, isSuccess = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSaving = false, error = e.message) }
                }
        }
    }

    fun delete() {
        viewModelScope.launch {
            val id = _uiState.value.id
            if (id.isNotEmpty()) {
                _uiState.update { it.copy(isSaving = true) }
                deleteClinicalCase(id)
                    .onSuccess {
                        _uiState.update { it.copy(isSaving = false, isSuccess = true) }
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(isSaving = false, error = e.message) }
                    }
            }
        }
    }
}
