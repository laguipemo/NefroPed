package com.laguipemo.nefroped.features.admin.lessons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laguipemo.nefroped.designsystem.R
import com.laguipemo.nefroped.designsystem.components.AuthTextField
import com.laguipemo.nefroped.designsystem.components.SystemBarsController
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLessonFormScreen(
    topicId: String,
    lessonId: String? = null,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: AdminLessonFormViewModel = koinViewModel { parametersOf(topicId, lessonId) }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (lessonId == null) "Nueva Lección" else "Editar Lección",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                actions = {
                    if (!uiState.isLoading) {
                        IconButton(onClick = { viewModel.onEvent(LessonFormEvent.Submit) }) {
                            Icon(Icons.Default.Save, contentDescription = "Guardar", tint = Color.White)
                        }
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
                .consumeWindowInsets(padding)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = dimensionResource(R.dimen.screen_horizontal_padding)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Información Básica
                AuthTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.onEvent(LessonFormEvent.TitleChanged(it)) },
                    label = "Título de la Lección",
                    isDarkBackground = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AuthTextField(
                        value = uiState.order.toString(),
                        onValueChange = { 
                            val value = it.toIntOrNull() ?: 0
                            viewModel.onEvent(LessonFormEvent.OrderChanged(value)) 
                        },
                        label = "Orden",
                        isDarkBackground = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    
                    Box(modifier = Modifier.weight(2f)) {
                        AuthTextField(
                            value = uiState.description ?: "",
                            onValueChange = { viewModel.onEvent(LessonFormEvent.DescriptionChanged(it)) },
                            label = "Descripción corta",
                            isDarkBackground = true,
                            singleLine = false,
                            minLines = 2,
                            maxLines = 3
                        )
                    }
                }

                // Editor de Contenido (Markdown)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Contenido (Markdown)",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    AuthTextField(
                        value = uiState.content,
                        onValueChange = { viewModel.onEvent(LessonFormEvent.ContentChanged(it)) },
                        label = "Redacta el contenido educativo aquí...",
                        isDarkBackground = true,
                        singleLine = false,
                        minLines = 10,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Recursos Multimedia (Sección agrupada para ahorrar espacio)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Recursos Adicionales",
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    ResourceField(
                        value = uiState.videoUrl ?: "",
                        onValueChange = { viewModel.onEvent(LessonFormEvent.VideoUrlChanged(it)) },
                        label = "URL Video",
                        icon = Icons.Default.PlayCircle,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    ResourceField(
                        value = uiState.pdfUrl ?: "",
                        onValueChange = { viewModel.onEvent(LessonFormEvent.PdfUrlChanged(it)) },
                        label = "URL Documento PDF",
                        icon = Icons.Default.Description,
                        tint = Color(0xFFE57373)
                    )

                    ResourceField(
                        value = uiState.audioUrl ?: "",
                        onValueChange = { viewModel.onEvent(LessonFormEvent.AudioUrlChanged(it)) },
                        label = "URL Audio",
                        icon = Icons.Default.Audiotrack,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.onEvent(LessonFormEvent.Submit) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(R.dimen.button_height)),
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer, 
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.primary)
                    } else {
                        Text(if (lessonId == null) "GUARDAR LECCIÓN" else "ACTUALIZAR LECCIÓN", fontWeight = FontWeight.ExtraBold)
                    }
                }

                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun ResourceField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    tint: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        AuthTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            isDarkBackground = true,
            modifier = Modifier.weight(1f)
        )
    }
}
