package com.laguipemo.nefroped.features.course.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
