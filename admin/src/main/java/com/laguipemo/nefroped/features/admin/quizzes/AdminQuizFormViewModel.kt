package com.laguipemo.nefroped.features.admin.quizzes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laguipemo.nefroped.core.domain.model.course.*
import com.laguipemo.nefroped.core.domain.usecase.course.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class AdminQuizFormUiState(
    val topicId: String = "",
    val topicTitle: String = "",
    val quizId: String = "",
    val title: String = "",
    val description: String = "",
    val questions: List<Question> = emptyList(),
    val isLoading: Boolean = false,
    val isSaveSuccess: Boolean = false,
    val error: String? = null
)

sealed interface AdminQuizFormEvent {
    data class TitleChanged(val title: String) : AdminQuizFormEvent
    data class DescriptionChanged(val description: String) : AdminQuizFormEvent
    data class QuestionAdded(val type: QuestionType) : AdminQuizFormEvent
    data class QuestionRemoved(val questionId: String) : AdminQuizFormEvent
    data class QuestionUpdated(val question: Question) : AdminQuizFormEvent
    data object SaveQuiz : AdminQuizFormEvent
}

class AdminQuizFormViewModel(
    private val topicId: String,
    private val quizId: String?,
    private val observeTopicUseCase: ObserveTopicUseCase,
    private val observeQuizByTopicUseCase: ObserveQuizByTopicUseCase,
    private val observeQuizByIdUseCase: ObserveQuizByIdUseCase,
    private val saveQuizUseCase: SaveQuizUseCase,
    private val saveQuestionUseCase: SaveQuestionUseCase,
    private val deleteQuestionUseCase: DeleteQuestionUseCase,
    private val syncQuizUseCase: SyncQuizUseCase,
    private val syncQuizByIdUseCase: SyncQuizByIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminQuizFormUiState(topicId = topicId))
    val uiState = _uiState.asStateFlow()

    init {
        loadTopic()
        loadQuiz()
    }

    private fun loadTopic() {
        viewModelScope.launch {
            observeTopicUseCase(topicId).collect { topic ->
                if (topic != null) {
                    _uiState.update { it.copy(topicTitle = topic.title) }
                }
            }
        }
    }

    private fun loadQuiz() {
        val sanitizedQuizId = if (quizId == "null" || quizId.isNullOrEmpty()) null else quizId
        android.util.Log.d("AdminQuizVM", "loadQuiz llamado para topicId: $topicId, quizId: $sanitizedQuizId")
        // 1. Observar cambios en Room de forma continua
        viewModelScope.launch {
            val quizFlow = if (sanitizedQuizId != null) {
                observeQuizByIdUseCase(sanitizedQuizId)
            } else {
                observeQuizByTopicUseCase(topicId)
            }

            quizFlow.collectLatest { quiz ->
                android.util.Log.d("AdminQuizVM", "Room emitió quiz: ${quiz?.title ?: "NULL"}. Preguntas: ${quiz?.questions?.size ?: 0}")
                if (quiz != null) {
                    _uiState.update { 
                        it.copy(
                            quizId = quiz.id,
                            title = quiz.title,
                            description = quiz.description ?: "",
                            questions = quiz.questions,
                            isLoading = false
                        )
                    }
                }
            }
        }

        // 2. Ejecutar la sincronización con Supabase
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            android.util.Log.d("AdminQuizVM", "Iniciando sincronización...")
            val result = if (sanitizedQuizId != null) {
                syncQuizByIdUseCase(sanitizedQuizId)
            } else {
                syncQuizUseCase(topicId)
            }
            
            android.util.Log.d("AdminQuizVM", "Sincronización terminada. Éxito: ${result.isSuccess}")
            
            // Sincronización terminada
            _uiState.update { 
                it.copy(
                    quizId = it.quizId.ifEmpty { java.util.UUID.randomUUID().toString() },
                    isLoading = false 
                )
            }
        }
    }

    fun onEvent(event: AdminQuizFormEvent) {
        when (event) {
            is AdminQuizFormEvent.TitleChanged -> {
                _uiState.update { it.copy(title = event.title) }
            }
            is AdminQuizFormEvent.DescriptionChanged -> {
                _uiState.update { it.copy(description = event.description) }
            }
            is AdminQuizFormEvent.QuestionAdded -> addQuestion(event.type)
            is AdminQuizFormEvent.QuestionRemoved -> removeQuestion(event.questionId)
            is AdminQuizFormEvent.QuestionUpdated -> updateQuestion(event.question)
            AdminQuizFormEvent.SaveQuiz -> saveQuiz()
        }
    }

    private fun addQuestion(type: QuestionType) {
        val newQuestion = Question(
            id = UUID.randomUUID().toString(),
            quizId = _uiState.value.quizId,
            text = "",
            type = type,
            options = when (type) {
                QuestionType.MATCH_DEFINITION -> QuestionOptions.Match(emptyList(), emptyList())
                else -> QuestionOptions.Simple(listOf("", ""))
            },
            correctAnswer = when (type) {
                QuestionType.MULTIPLE_CHOICE -> QuestionAnswer.Multiple(emptyList())
                QuestionType.MATCH_DEFINITION -> QuestionAnswer.Match(emptyMap())
                else -> QuestionAnswer.Single(0)
            }
        )
        _uiState.update { it.copy(questions = it.questions + newQuestion) }
    }

    private fun removeQuestion(id: String) {
        viewModelScope.launch {
            deleteQuestionUseCase(id)
            _uiState.update { state ->
                state.copy(questions = state.questions.filter { it.id != id })
            }
        }
    }

    private fun updateQuestion(question: Question) {
        _uiState.update { state ->
            state.copy(questions = state.questions.map { if (it.id == question.id) question else it })
        }
    }

    private fun saveQuiz() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val state = _uiState.value
            
            val quiz = Quiz(
                id = state.quizId,
                topicId = state.topicId,
                title = state.title,
                description = state.description,
                questions = state.questions
            )

            val result = saveQuizUseCase(quiz)
            if (result.isSuccess) {
                // Guardar cada pregunta individualmente para asegurar sincronización completa
                state.questions.forEach { saveQuestionUseCase(it) }
                _uiState.update { it.copy(isLoading = false, isSaveSuccess = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Error al guardar el quiz") }
            }
        }
    }
}
