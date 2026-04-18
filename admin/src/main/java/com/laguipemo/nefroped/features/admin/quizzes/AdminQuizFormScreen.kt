package com.laguipemo.nefroped.features.admin.quizzes

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laguipemo.nefroped.core.domain.model.course.QuestionType
import com.laguipemo.nefroped.designsystem.R
import com.laguipemo.nefroped.designsystem.components.AuthTextField
import com.laguipemo.nefroped.designsystem.components.SystemBarsController
import com.laguipemo.nefroped.features.admin.quizzes.components.AdminQuestionItem
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminQuizFormScreen(
    topicId: String,
    quizId: String? = null,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: AdminQuizFormViewModel = koinViewModel { 
        parametersOf(
            topicId, 
            if (quizId == "null" || quizId.isNullOrEmpty()) null else quizId
        ) 
    }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val darkTheme = isSystemInDarkTheme()

    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) {
            onSaveSuccess()
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Configurar Quiz",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        if (uiState.topicTitle.isNotEmpty()) {
                            Text(
                                uiState.topicTitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(AdminQuizFormEvent.SaveQuiz) }) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            var showTypeMenu by remember { mutableStateOf(false) }
            Box {
                FloatingActionButton(
                    onClick = { showTypeMenu = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir pregunta")
                }
                DropdownMenu(
                    expanded = showTypeMenu,
                    onDismissRequest = { showTypeMenu = false }
                ) {
                    QuestionType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name.replace("_", " ")) },
                            onClick = {
                                viewModel.onEvent(AdminQuizFormEvent.QuestionAdded(type))
                                showTypeMenu = false
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(dimensionResource(R.dimen.screen_horizontal_padding)),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AuthTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.onEvent(AdminQuizFormEvent.TitleChanged(it)) },
                    label = "Título del Quiz",
                    isDarkBackground = true
                )
            }

            item {
                AuthTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.onEvent(AdminQuizFormEvent.DescriptionChanged(it)) },
                    label = "Instrucciones / Historia Clínica",
                    isDarkBackground = true,
                    modifier = Modifier.heightIn(min = 100.dp)
                )
            }

            item {
                Text(
                    "Preguntas (${uiState.questions.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            itemsIndexed(uiState.questions) { index, question ->
                AdminQuestionItem(
                    index = index + 1,
                    question = question,
                    onUpdate = { updatedQuestion -> 
                        viewModel.onEvent(AdminQuizFormEvent.QuestionUpdated(updatedQuestion)) 
                    },
                    onDelete = { viewModel.onEvent(AdminQuizFormEvent.QuestionRemoved(question.id)) }
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}
