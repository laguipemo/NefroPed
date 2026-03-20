package com.laguipemo.nefroped.features.course.quiz

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laguipemo.nefroped.core.domain.model.course.*
import com.laguipemo.nefroped.designsystem.R
import com.laguipemo.nefroped.designsystem.components.SystemBarsController
import com.laguipemo.nefroped.features.course.quiz.components.*
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    onBackClick: () -> Unit,
    viewModel: QuizViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val darkTheme = androidx.compose.foundation.isSystemInDarkTheme()

    SystemBarsController(
        useStatusDarkIcons = false,
        useNavigationDarkIcons = !darkTheme
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { 
                    val title = when (val state = uiState) {
                        is QuizUiState.Loading -> state.initialTitle ?: stringResource(R.string.quiz_title)
                        is QuizUiState.Content -> state.quiz.title
                        is QuizUiState.Error -> stringResource(R.string.quiz_title)
                    }
                    Text(
                        text = title, 
                        fontWeight = FontWeight.Bold, 
                        color = Color.White, 
                        maxLines = 1, 
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back), tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is QuizUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
                is QuizUiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                is QuizUiState.Content -> {
                    if (state.isFinished && state.quizResult != null) {
                        QuizResultContent(
                            result = state.quizResult,
                            onFinish = onBackClick,
                            onRetry = viewModel::retryQuiz
                        )
                    } else {
                        QuizPlayContent(
                            state = state,
                            onOptionSelected = viewModel::onOptionSelected,
                            onMatchSelected = viewModel::onMatchSelected,
                            onUnmatch = viewModel::onUnmatch,
                            onNextClick = viewModel::onNextQuestion,
                            onPrevClick = viewModel::onPreviousQuestion
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuizPlayContent(
    state: QuizUiState.Content,
    onOptionSelected: (Int) -> Unit,
    onMatchSelected: (Int, Int) -> Unit,
    onUnmatch: (Int) -> Unit,
    onNextClick: () -> Unit,
    onPrevClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.space_m))
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        state.quiz.description?.takeIf { it.isNotBlank() }?.let { history ->
             ClinicalHistoryCard(history = history)
             Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_m)))
        }

        LinearProgressIndicator(
            progress = { state.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_s)))

        Text(
            text = stringResource(R.string.quiz_question_count, state.currentQuestionIndex + 1, state.quiz.questions.size),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_m)))

        state.currentQuestion?.let { question ->
            QuestionCard(
                question = question,
                currentSelection = state.currentSelection,
                onOptionSelected = onOptionSelected,
                onMatchSelected = onMatchSelected,
                onUnmatch = onUnmatch
            )
        }

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_xl)))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.space_m))
        ) {
            if (state.currentQuestionIndex > 0) {
                OutlinedButton(
                    onClick = onPrevClick,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.space_m)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                ) {
                    Text(stringResource(R.string.quiz_previous), fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = onNextClick,
                enabled = state.canContinue && !state.isSubmitting,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(dimensionResource(R.dimen.space_m)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.primary)
                } else {
                    Text(
                        text = if (state.isLastQuestion) stringResource(R.string.quiz_submit) else stringResource(R.string.quiz_next),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun QuestionCard(
    question: Question,
    currentSelection: UserSelection,
    onOptionSelected: (Int) -> Unit,
    onMatchSelected: (Int, Int) -> Unit,
    onUnmatch: (Int) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.space_m))) {
            question.intro?.takeIf { it.isNotBlank() }?.let { 
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_m)))
            }

            if (question.type != QuestionType.MATCH_DEFINITION) {
                Text(text = question.text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, lineHeight = 28.sp)
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_l)))
            }

            when (question.type) {
                QuestionType.TRUE_FALSE, QuestionType.ONE_CHOICE, QuestionType.MULTIPLE_CHOICE -> {
                    val options = (question.options as? QuestionOptions.Simple)?.list ?: emptyList()
                    options.forEachIndexed { index, text ->
                        val isSelected = when (currentSelection) { 
                            is UserSelection.Single -> currentSelection.index == index
                            is UserSelection.Multiple -> currentSelection.indices.contains(index)
                            else -> false 
                        }
                        OptionItem(text = text, isSelected = isSelected, onClick = { onOptionSelected(index) })
                    }
                }
                QuestionType.MATCH_DEFINITION -> {
                    val opts = question.options as? QuestionOptions.Match ?: return@Column
                    MatchDefinitionContent(
                        instructionText = question.text, 
                        terms = opts.terms, 
                        definitions = opts.definitions, 
                        currentMapping = (currentSelection as? UserSelection.Match)?.mapping ?: emptyMap(), 
                        onMatchSelected = onMatchSelected, 
                        onUnmatch = onUnmatch
                    )
                }
            }
        }
    }
}

@Composable
fun MatchDefinitionContent(
    instructionText: String,
    terms: List<String>,
    definitions: List<String>,
    currentMapping: Map<Int, Int>,
    onMatchSelected: (Int, Int) -> Unit,
    onUnmatch: (Int) -> Unit
) {
    var selectedTermIndex by remember { mutableStateOf<Int?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        terms.forEachIndexed { index, term ->
            val isUsed = currentMapping.containsKey(index)
            val isSelected = selectedTermIndex == index
            
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        if (isUsed) onUnmatch(index) 
                        else selectedTermIndex = if (isSelected) null else index 
                    },
                shape = RoundedCornerShape(12.dp),
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isUsed -> MaterialTheme.colorScheme.surfaceVariant
                    else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                },
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(if (isSelected) Color.White else MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) { 
                        Text(('A' + index).toString(), color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White, fontWeight = FontWeight.ExtraBold) 
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = term, 
                        style = MaterialTheme.typography.bodyMedium, 
                        modifier = Modifier.weight(1f), 
                        color = if (isSelected) Color.White else if (isUsed) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                    )
                    if (isUsed) {
                        androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.Close, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_m)))
        Text(instructionText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, lineHeight = 24.sp)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        
        definitions.forEachIndexed { defIdx, def ->
            val matchedIdx = currentMapping.entries.find { it.value == defIdx }?.key
            val isMatched = matchedIdx != null
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(selectedTermIndex != null && !isMatched) { 
                        selectedTermIndex?.let { onMatchSelected(it, defIdx); selectedTermIndex = null } 
                    },
                shape = RoundedCornerShape(12.dp),
                color = if (isMatched) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f) else Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isMatched) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (isMatched) {
                        Box(modifier = Modifier.padding(end = 12.dp).size(28.dp).background(MaterialTheme.colorScheme.secondary, CircleShape), contentAlignment = Alignment.Center) { 
                            Text(('A' + (matchedIdx ?: 0)).toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp) 
                        }
                    } else {
                        Box(modifier = Modifier.padding(end = 12.dp).size(28.dp).border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape))
                    }
                    Text(def, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
