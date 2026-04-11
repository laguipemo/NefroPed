package com.laguipemo.nefroped.features.admin.topics

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.laguipemo.nefroped.core.domain.model.course.Lesson
import com.laguipemo.nefroped.core.domain.model.course.TopicType
import com.laguipemo.nefroped.designsystem.R
import com.laguipemo.nefroped.designsystem.components.AuthTextField
import com.laguipemo.nefroped.designsystem.components.SystemBarsController
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTopicFormScreen(
    topicId: String? = null,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    onAddLesson: (String) -> Unit,
    onEditLesson: (String, String) -> Unit,
    viewModel: AdminTopicFormViewModel = koinViewModel { parametersOf(topicId) }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val darkTheme = isSystemInDarkTheme()

    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) {
            onSaveSuccess()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                val inputStream = context.contentResolver.openInputStream(it)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                bytes?.let { b ->
                    viewModel.onEvent(TopicFormEvent.ImageSelected(b))
                }
            }
        }
    )

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
                        text = if (topicId == null) "Nuevo Tema" else "Editar Tema",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
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
                    if (!uiState.isLoading) {
                        IconButton(onClick = { viewModel.onEvent(TopicFormEvent.Submit) }) {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = "Guardar",
                                tint = Color.White
                            )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {
            if (topicId != null) {
                SecondaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primaryContainer,
                    indicator = {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(
                                selectedTabIndex = selectedTab
                            ),
                            color = MaterialTheme.colorScheme.primaryContainer
                        )
                    },
                    divider = { HorizontalDivider(color = Color.White.copy(alpha = 0.1f)) }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "INFORMACIÓN",
                                fontWeight = FontWeight.Bold
                            )
                        },
                        unselectedContentColor = Color.White.copy(alpha = 0.6f)
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                if (uiState.type == TopicType.LESSONS) "LECCIONES" else "CASOS",
                                fontWeight = FontWeight.Bold
                            )
                        },
                        unselectedContentColor = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            ) {
                if (selectedTab == 0 || topicId == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = dimensionResource(R.dimen.screen_horizontal_padding)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(dimensionResource(R.dimen.topic_card_corner_radius)))
                                .background(Color.White.copy(alpha = 0.1f))
                                .border(
                                    1.dp,
                                    Color.White.copy(alpha = 0.2f),
                                    RoundedCornerShape(dimensionResource(R.dimen.topic_card_corner_radius))
                                )
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            val displayImage =
                                uiState.selectedImageUri ?: uiState.imageUrl
                            if (displayImage != null) {
                                AsyncImage(
                                    model = displayImage,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        "Tocar para subir portada",
                                        color = Color.White.copy(alpha = 0.6f),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }

                        AuthTextField(
                            value = uiState.title,
                            onValueChange = {
                                viewModel.onEvent(
                                    TopicFormEvent.TitleChanged(
                                        it
                                    )
                                )
                            },
                            label = "Título del Tema",
                            isDarkBackground = true
                        )

                        AuthTextField(
                            value = uiState.description,
                            onValueChange = {
                                viewModel.onEvent(
                                    TopicFormEvent.DescriptionChanged(
                                        it
                                    )
                                )
                            },
                            label = "Descripción",
                            isDarkBackground = true,
                            modifier = Modifier.heightIn(min = 120.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(
                                dimensionResource(R.dimen.space_m)
                            ),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            AuthTextField(
                                value = uiState.order.toString(),
                                onValueChange = {
                                    val value = it.toIntOrNull() ?: 0
                                    viewModel.onEvent(
                                        TopicFormEvent.OrderChanged(
                                            value
                                        )
                                    )
                                },
                                label = "Orden",
                                isDarkBackground = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )

                            Surface(
                                modifier = Modifier
                                    .weight(2f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
                                color = Color.White.copy(alpha = 0.1f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    Color.White.copy(alpha = 0.2f)
                                )
                            ) {
                                Row(modifier = Modifier.fillMaxSize()) {
                                    TopicTypeOption(
                                        text = "Lecciones",
                                        isSelected = uiState.type == TopicType.LESSONS,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            viewModel.onEvent(
                                                TopicFormEvent.TypeChanged(
                                                    TopicType.LESSONS
                                                )
                                            )
                                        }
                                    )
                                    TopicTypeOption(
                                        text = "Casos",
                                        isSelected = uiState.type == TopicType.CLINICAL_CASES,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            viewModel.onEvent(
                                                TopicFormEvent.TypeChanged(
                                                    TopicType.CLINICAL_CASES
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.onEvent(TopicFormEvent.Submit) },
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
                                CircularProgressIndicator(
                                    modifier = Modifier.size(
                                        24.dp
                                    ), color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    if (topicId == null) "CREAR TEMA" else "GUARDAR CAMBIOS",
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = dimensionResource(R.dimen.screen_horizontal_padding)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (uiState.type == TopicType.LESSONS) "Lecciones del Tema" else "Casos Clínicos",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            TextButton(
                                onClick = { onAddLesson(topicId) },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Añadir")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (uiState.lessons.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No hay contenido aún",
                                    color = Color.White.copy(alpha = 0.5f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            uiState.lessons.forEach { lesson ->
                                AdminLessonItem(
                                    lesson = lesson,
                                    onClick = {
                                        onEditLesson(
                                            topicId,
                                            lesson.id
                                        )
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(88.dp)) // Margen suficiente para el FAB/Suelo de la pantalla
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminLessonItem(
    lesson: Lesson,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    // Video Icon
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (lesson.videoUrl?.isNotBlank() == true)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = 0.2f
                            )
                    )

                    // Audio Icon
                    Icon(
                        imageVector = Icons.Default.Audiotrack,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (lesson.audioUrl?.isNotBlank() == true)
                            MaterialTheme.colorScheme.secondary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = 0.2f
                            )
                    )

                    // PDF Icon
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (lesson.pdfUrl?.isNotBlank() == true)
                            Color(0xFFE57373)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = 0.2f
                            )
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Editar",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun TopicTypeOption(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
        )
    }
}
