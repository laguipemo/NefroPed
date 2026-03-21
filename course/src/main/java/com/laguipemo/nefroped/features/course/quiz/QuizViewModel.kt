package com.laguipemo.nefroped.features.course.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.laguipemo.nefroped.core.domain.model.course.*
import com.laguipemo.nefroped.core.domain.usecase.course.*
import com.laguipemo.nefroped.navigation.AuthenticatedRoute
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class QuizViewModel(
    savedStateHandle: SavedStateHandle,
    private val observeQuizByTopic: ObserveQuizByTopicUseCase,
    private val observeQuizById: ObserveQuizByIdUseCase,
    private val syncQuiz: SyncQuizUseCase,
    private val syncQuizById: SyncQuizByIdUseCase,
    private val submitQuizUseCase: SubmitQuizUseCase
) : ViewModel() {

    private val route = savedStateHandle.toRoute<AuthenticatedRoute.Quiz>()
    private val quizIdArg: String = route.id
    val isTopicId: Boolean = route.isTopicId
    private val initialTitleArg: String? = route.title

    private val _currentQuestionIndex = MutableStateFlow(0)
    private val _currentSelection = MutableStateFlow<UserSelection>(UserSelection.None)
    private val _answers = MutableStateFlow<Map<Int, UserSelection>>(emptyMap())
    private val _isSubmitting = MutableStateFlow(false)
    private val _isFinished = MutableStateFlow(false)
    private val _quizResult = MutableStateFlow<QuizResult?>(null)
    private val _shuffledQuiz = MutableStateFlow<Quiz?>(null)

    val uiState: StateFlow<QuizUiState> = combine(
        _shuffledQuiz,
        _currentQuestionIndex,
        _currentSelection,
        _answers,
        _isSubmitting,
        _isFinished,
        _quizResult
    ) { params: Array<Any?> ->
        val quiz = params[0] as Quiz?
        val index = params[1] as Int
        val selection = params[2] as UserSelection
        val answers = params[3] as Map<Int, UserSelection>
        val submitting = params[4] as Boolean
        val finished = params[5] as Boolean
        val result = params[6] as QuizResult?

        if (quiz == null) {
            QuizUiState.Loading(initialTitleArg)
        } else {
            QuizUiState.Content(
                quiz = quiz,
                currentQuestionIndex = index,
                currentSelection = selection,
                answers = answers,
                isSubmitting = submitting,
                isFinished = finished,
                quizResult = result
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = QuizUiState.Loading(initialTitleArg)
    )

    init {
        loadQuiz()
    }

    private fun loadQuiz() {
        viewModelScope.launch {
            if (isTopicId) {
                syncQuiz(quizIdArg)
                observeQuizByTopic(quizIdArg).filterNotNull().firstOrNull()?.let { quiz ->
                    _shuffledQuiz.value = shuffleQuiz(quiz)
                }
            } else {
                syncQuizById(quizIdArg)
                observeQuizById(quizIdArg).filterNotNull().firstOrNull()?.let { quiz ->
                    _shuffledQuiz.value = shuffleQuiz(quiz)
                }
            }
        }
    }

    private fun shuffleQuiz(quiz: Quiz): Quiz {
        val shuffledQuestions = quiz.questions.shuffled().map { question ->
            if (question.type == QuestionType.MATCH_DEFINITION || question.type == QuestionType.TRUE_FALSE) {
                question
            } else {
                val options = (question.options as? QuestionOptions.Simple)?.list ?: return@map question
                val indexedOptions = options.withIndex().shuffled()
                val newCorrectAnswer = when (val correct = question.correctAnswer) {
                    is QuestionAnswer.Single -> QuestionAnswer.Single(indexedOptions.indexOfFirst { it.index == correct.index })
                    is QuestionAnswer.Multiple -> QuestionAnswer.Multiple(correct.indices.map { oldIdx -> indexedOptions.indexOfFirst { it.index == oldIdx } })
                    else -> correct
                }
                question.copy(options = QuestionOptions.Simple(indexedOptions.map { it.value }), correctAnswer = newCorrectAnswer)
            }
        }
        return quiz.copy(questions = shuffledQuestions)
    }

    fun onOptionSelected(index: Int) {
        val currentQuestion = (uiState.value as? QuizUiState.Content)?.currentQuestion ?: return
        when (currentQuestion.type) {
            QuestionType.TRUE_FALSE, QuestionType.ONE_CHOICE -> _currentSelection.value = UserSelection.Single(index)
            QuestionType.MULTIPLE_CHOICE -> {
                val current = (_currentSelection.value as? UserSelection.Multiple)?.indices ?: emptySet()
                val next = if (current.contains(index)) current - index else current + index
                _currentSelection.value = if (next.isEmpty()) UserSelection.None else UserSelection.Multiple(next)
            }
            else -> {}
        }
    }

    fun onMatchSelected(termIndex: Int, defIndex: Int) {
        val current = (_currentSelection.value as? UserSelection.Match)?.mapping ?: emptyMap()
        val next = current.filterValues { it != defIndex } + (termIndex to defIndex)
        _currentSelection.value = UserSelection.Match(next)
    }

    fun onUnmatch(termIndex: Int) {
        val current = (_currentSelection.value as? UserSelection.Match)?.mapping ?: return
        val next = current - termIndex
        _currentSelection.value = if (next.isEmpty()) UserSelection.None else UserSelection.Match(next)
    }

    fun onNextQuestion() {
        val currentState = uiState.value as? QuizUiState.Content ?: return
        val newAnswers = _answers.value + (currentState.currentQuestionIndex to currentState.currentSelection)
        _answers.value = newAnswers
        if (currentState.isLastQuestion) {
            submitQuiz(currentState.quiz.id, newAnswers, currentState.quiz.questions.size)
        } else {
            val nextIndex = currentState.currentQuestionIndex + 1
            _currentQuestionIndex.value = nextIndex
            _currentSelection.value = _answers.value[nextIndex] ?: UserSelection.None
        }
    }

    fun onPreviousQuestion() {
        val index = _currentQuestionIndex.value
        if (index > 0) {
            val currentState = uiState.value as? QuizUiState.Content
            if (currentState != null && currentState.currentSelection !is UserSelection.None) {
                _answers.value = _answers.value + (index to currentState.currentSelection)
            }
            val prevIndex = index - 1
            _currentQuestionIndex.value = prevIndex
            _currentSelection.value = _answers.value[prevIndex] ?: UserSelection.None
        }
    }

    fun retryQuiz() {
        _currentQuestionIndex.value = 0
        _currentSelection.value = UserSelection.None
        _answers.value = emptyMap()
        _isFinished.value = false
        _quizResult.value = null
        _shuffledQuiz.value?.let { _shuffledQuiz.value = shuffleQuiz(it) }
    }

    private fun submitQuiz(quizId: String, answers: Map<Int, UserSelection>, totalQuestions: Int) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val quiz = _shuffledQuiz.value ?: return@launch
            val correctCount = quiz.questions.indices.count { i ->
                validateAnswer(quiz.questions[i], answers[i] ?: return@count false)
            }
            val result = QuizResult(quizId = quizId, score = (correctCount.toFloat() / totalQuestions) * 10, correctAnswers = correctCount, totalQuestions = totalQuestions, completedAt = System.currentTimeMillis())
            submitQuizUseCase(result)
            _quizResult.value = result
            _isFinished.value = true
            _isSubmitting.value = false
        }
    }

    private fun validateAnswer(question: Question, selection: UserSelection): Boolean {
        return when (val correct = question.correctAnswer) {
            is QuestionAnswer.Single -> (selection as? UserSelection.Single)?.index == correct.index
            is QuestionAnswer.Multiple -> {
                val userIndices = (selection as? UserSelection.Multiple)?.indices ?: emptySet()
                userIndices.size == correct.indices.size && userIndices.containsAll(correct.indices)
            }
            is QuestionAnswer.Match -> (selection as? UserSelection.Match)?.mapping == correct.mapping
        }
    }
}
