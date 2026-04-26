package com.laguipemo.nefroped.features.admin.topics

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.laguipemo.nefroped.core.domain.model.course.ClinicalCase
import com.laguipemo.nefroped.core.domain.model.course.ExternalLink
import com.laguipemo.nefroped.core.domain.model.course.Lesson
import com.laguipemo.nefroped.core.domain.model.course.TopicType
import com.laguipemo.nefroped.designsystem.R
import com.laguipemo.nefroped.designsystem.components.AuthTextField
import com.laguipemo.nefroped.designsystem.components.SystemBarsController
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTopicFormScreen(
    topicId: String? = null,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    onAddLesson: (String) -> Unit,
    onEditLesson: (String, String) -> Unit,
    onAddClinicalCase: (String) -> Unit,
    onEditClinicalCase: (String, String) -> Unit,
    viewModel: AdminTopicFormViewModel = koinViewModel { parametersOf(topicId) }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val darkTheme = isSystemInDarkTheme()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var selectedLink by remember { mutableStateOf<ExternalLink?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Eliminar Tema") },
            text = { Text("¿Estás seguro de que deseas eliminar este tema y todo su contenido? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        topicId?.let { viewModel.onEvent(TopicFormEvent.DeleteTopic(it)) }
                        showDeleteConfirm = false
                        onBackClick()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

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
                    if (topicId != null && !uiState.isLoading) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.White)
                        }
                    }
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
                                when (uiState.type) {
                                    TopicType.THEORY -> "LECCIONES"
                                    TopicType.PRACTICE -> "CASOS"
                                    TopicType.SUPPORT -> "RECURSOS"
                                },
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
                    TopicInfoForm(
                        uiState = uiState,
                        topicId = topicId,
                        photoPickerLauncher = photoPickerLauncher,
                        onEvent = viewModel::onEvent
                    )
                } else {
                    TopicContentSection(
                        uiState = uiState,
                        topicId = topicId,
                        onAddLesson = onAddLesson,
                        onEditLesson = onEditLesson,
                        onAddClinicalCase = onAddClinicalCase,
                        onEditClinicalCase = onEditClinicalCase,
                        onAddLink = { 
                            selectedLink = null
                            showLinkDialog = true 
                        },
                        onEditLink = { link ->
                            selectedLink = link
                            showLinkDialog = true
                        },
                        onDeleteLink = { linkId ->
                            viewModel.onEvent(TopicFormEvent.DeleteExternalLink(linkId))
                        },
                        onDeleteLesson = { lessonId ->
                            viewModel.onEvent(TopicFormEvent.DeleteLesson(lessonId))
                        },
                        onDeleteClinicalCase = { caseId ->
                            viewModel.onEvent(TopicFormEvent.DeleteClinicalCase(caseId))
                        }
                    )
                }
            }
        }
    }

    if (showLinkDialog) {
        ExternalLinkDialog(
            link = selectedLink,
            topicId = topicId ?: "",
            onDismiss = { showLinkDialog = false },
            onSave = { link ->
                viewModel.onEvent(TopicFormEvent.SaveExternalLink(link))
                showLinkDialog = false
            }
        )
    }
}

@Composable
private fun TopicInfoForm(
    uiState: TopicFormUiState,
    topicId: String?,
    photoPickerLauncher: androidx.activity.result.ActivityResultLauncher<PickVisualMediaRequest>,
    onEvent: (TopicFormEvent) -> Unit
) {
    val scrollState = rememberScrollState()
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
            val displayImage = uiState.selectedImageUri ?: uiState.imageUrl
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
            onValueChange = { onEvent(TopicFormEvent.TitleChanged(it)) },
            label = "Título del Tema",
            isDarkBackground = true
        )

        AuthTextField(
            value = uiState.description,
            onValueChange = { onEvent(TopicFormEvent.DescriptionChanged(it)) },
            label = "Descripción",
            isDarkBackground = true,
            modifier = Modifier.heightIn(min = 120.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.space_m)),
            verticalAlignment = Alignment.Bottom
        ) {
            AuthTextField(
                value = uiState.order.toString(),
                onValueChange = { onEvent(TopicFormEvent.OrderChanged(it.toIntOrNull() ?: 0)) },
                label = "Orden",
                isDarkBackground = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )

            Surface(
                modifier = Modifier
                    .weight(3f)
                    .height(56.dp),
                shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
                color = if (topicId == null) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (topicId == null) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f)
                )
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    TopicTypeOption(
                        text = "Teoría",
                        isSelected = uiState.type == TopicType.THEORY,
                        enabled = topicId == null,
                        modifier = Modifier.weight(1f),
                        onClick = { onEvent(TopicFormEvent.TypeChanged(TopicType.THEORY)) }
                    )
                    TopicTypeOption(
                        text = "Práctica",
                        isSelected = uiState.type == TopicType.PRACTICE,
                        enabled = topicId == null,
                        modifier = Modifier.weight(1f),
                        onClick = { onEvent(TopicFormEvent.TypeChanged(TopicType.PRACTICE)) }
                    )
                    TopicTypeOption(
                        text = "Apoyo",
                        isSelected = uiState.type == TopicType.SUPPORT,
                        enabled = topicId == null,
                        modifier = Modifier.weight(1f),
                        onClick = { onEvent(TopicFormEvent.TypeChanged(TopicType.SUPPORT)) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onEvent(TopicFormEvent.Submit) },
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
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary
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
}

@Composable
private fun TopicContentSection(
    uiState: TopicFormUiState,
    topicId: String,
    onAddLesson: (String) -> Unit,
    onEditLesson: (String, String) -> Unit,
    onAddClinicalCase: (String) -> Unit,
    onEditClinicalCase: (String, String) -> Unit,
    onAddLink: () -> Unit,
    onEditLink: (ExternalLink) -> Unit,
    onDeleteLink: (String) -> Unit,
    onDeleteLesson: (String) -> Unit,
    onDeleteClinicalCase: (String) -> Unit
) {
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
                text = when (uiState.type) {
                    TopicType.THEORY -> "Lecciones del Tema"
                    TopicType.PRACTICE -> "Casos Clínicos"
                    TopicType.SUPPORT -> "Enlaces de Apoyo"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            TextButton(
                onClick = { 
                    when (uiState.type) {
                        TopicType.SUPPORT -> onAddLink()
                        TopicType.PRACTICE -> onAddClinicalCase(topicId)
                        TopicType.THEORY -> onAddLesson(topicId)
                    }
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Añadir")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (uiState.type) {
            TopicType.SUPPORT -> {
                if (uiState.externalLinks.isEmpty()) {
                    EmptyContentPlaceholder("No hay enlaces de apoyo aún")
                } else {
                    uiState.externalLinks.forEach { link ->
                        AdminLinkItem(
                            link = link,
                            onEdit = { onEditLink(link) },
                            onDelete = { onDeleteLink(link.id) }
                        )
                    }
                }
            }
            TopicType.PRACTICE -> {
                if (uiState.clinicalCases.isEmpty()) {
                    EmptyContentPlaceholder("No hay casos clínicos aún")
                } else {
                    uiState.clinicalCases.forEach { clinicalCase ->
                        AdminClinicalCaseItem(
                            clinicalCase = clinicalCase,
                            onClick = { onEditClinicalCase(topicId, clinicalCase.id) },
                            onDelete = { onDeleteClinicalCase(clinicalCase.id) }
                        )
                    }
                }
            }
            TopicType.THEORY -> {
                if (uiState.lessons.isEmpty()) {
                    EmptyContentPlaceholder("No hay lecciones aún")
                } else {
                    uiState.lessons.forEach { lesson ->
                        AdminLessonItem(
                            lesson = lesson,
                            onClick = { onEditLesson(topicId, lesson.id) },
                            onDelete = { onDeleteLesson(lesson.id) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(88.dp))
    }
}

@Composable
private fun AdminClinicalCaseItem(
    clinicalCase: ClinicalCase,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Eliminar Caso Clínico") },
            text = { Text("¿Estás seguro de que deseas eliminar el caso clínico \"${clinicalCase.title}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

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
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(clinicalCase.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    clinicalCase.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }

            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun EmptyContentPlaceholder(message: String = "No hay contenido aún") {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            message,
            color = Color.White.copy(alpha = 0.5f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun AdminLinkItem(
    link: ExternalLink,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(link.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(link.url, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun ExternalLinkDialog(
    link: ExternalLink?,
    topicId: String,
    onDismiss: () -> Unit,
    onSave: (ExternalLink) -> Unit
) {
    var title by remember { mutableStateOf(link?.title ?: "") }
    var description by remember { mutableStateOf(link?.description ?: "") }
    var url by remember { mutableStateOf(link?.url ?: "") }
    var order by remember { mutableIntStateOf(link?.order ?: 0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (link == null) "Nuevo Enlace" else "Editar Enlace") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción (opcional)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("URL") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = order.toString(), onValueChange = { order = it.toIntOrNull() ?: 0 }, label = { Text("Orden") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() && url.isNotBlank(),
                onClick = {
                    onSave(ExternalLink(
                        id = link?.id ?: UUID.randomUUID().toString(),
                        topicId = topicId,
                        title = title,
                        description = description,
                        url = url,
                        order = order
                    ))
                }
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun AdminLessonItem(
    lesson: Lesson,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Eliminar Lección") },
            text = { Text("¿Estás seguro de que deseas eliminar la lección \"${lesson.title}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

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
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Book, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(lesson.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!lesson.videoUrl.isNullOrBlank()) Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    if (!lesson.audioUrl.isNullOrBlank()) Icon(Icons.Default.Audiotrack, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                    if (!lesson.pdfUrl.isNullOrBlank()) Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFE57373))
                }
            }
            
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }

            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun TopicTypeOption(
    text: String,
    isSelected: Boolean,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)))
            .background(
                if (isSelected) {
                    if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                } else {
                    Color.Transparent
                }
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else (if (enabled) Color.White.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.3f))
        )
    }
}
