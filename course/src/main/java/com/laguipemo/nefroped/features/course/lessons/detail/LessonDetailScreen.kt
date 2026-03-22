package com.laguipemo.nefroped.features.course.lessons.detail

import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laguipemo.nefroped.designsystem.components.SystemBarsController
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonDetailScreen(
    lessonId: String,
    onBackClick: () -> Unit,
    viewModel: LessonDetailViewModel = koinViewModel { parametersOf(lessonId) }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val darkTheme = isSystemInDarkTheme()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                LessonDetailUiEffect.NavigateBack -> onBackClick()
            }
        }
    }

    val isAtBottom by remember {
        derivedStateOf {
            scrollState.value >= (scrollState.maxValue - 50)
        }
    }

    SystemBarsController(
        useStatusDarkIcons = false,
        useNavigationDarkIcons = !darkTheme
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    val title = (uiState as? LessonDetailUiState.Content)?.lesson?.title ?: ""
                    Text(title, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            if (uiState is LessonDetailUiState.Content) {
                val state = uiState as LessonDetailUiState.Content
                AnimatedVisibility(
                    visible = isAtBottom && !state.isCompleted,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    SmallFloatingActionButton(
                        onClick = { viewModel.markAsCompleted() },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Completada")
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (val state = uiState) {
                LessonDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                }

                is LessonDetailUiState.Content -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            tonalElevation = 2.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (state.isMarkdownLoading) {
                                    LinearProgressIndicator(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                MarkdownText(
                                    markdown = state.markdownContent.replace("\\n", "\n"),
                                    modifier = Modifier.fillMaxWidth(),
                                    linkColor = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        lineHeight = 22.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    isTextSelectable = true,
                                    syntaxHighlightTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(64.dp))
                    }
                }

                is LessonDetailUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}
