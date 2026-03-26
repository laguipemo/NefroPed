package com.laguipemo.nefroped.features.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laguipemo.nefroped.designsystem.R
import com.laguipemo.nefroped.designsystem.components.*
import com.laguipemo.nefroped.features.profile.components.CourseProgressCard
import com.laguipemo.nefroped.features.profile.components.LinkAccountSheetContent
import com.laguipemo.nefroped.features.profile.components.UserHeader
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinViewModel(),
    onOpenChat: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val darkTheme = isSystemInDarkTheme()

    SystemBarsController(
        useStatusDarkIcons = false,
        useNavigationDarkIcons = !darkTheme
    )

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                val inputStream = context.contentResolver.openInputStream(it)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                bytes?.let { b ->
                    viewModel.onUpdateAvatar(b, "avatar_${System.currentTimeMillis()}.jpg")
                }
            }
        }
    )

    Scaffold(
        containerColor = Color.Transparent, 
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.profile_title), fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = uiState) {
                ProfileUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                is ProfileUiState.Content -> {
                    UserHeader(
                        state = state,
                        onAvatarClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_m)))

                    // NUEVA: Tarjeta de Progreso
                    CourseProgressCard(
                        completedLessons = state.completedLessons,
                        totalLessons = state.totalLessons,
                        progress = state.overallProgress
                    )

                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_l)))

                    ProfileSection(title = stringResource(R.string.profile_section_account)) {
                        if (state.isGuest) {
                            ProfileOptionItem(
                                icon = Icons.Default.Link,
                                title = stringResource(R.string.profile_action_link_account),
                                subtitle = stringResource(R.string.profile_action_link_account_desc),
                                onClick = { viewModel.onShowBottomSheet(true) },
                                iconColor = MaterialTheme.colorScheme.primary
                            )
                        }
                        ProfileOptionItem(
                            icon = Icons.AutoMirrored.Filled.Logout,
                            title = stringResource(R.string.profile_action_logout),
                            onClick = { viewModel.onLogoutClicked() },
                            iconColor = MaterialTheme.colorScheme.error,
                            showChevron = false
                        )
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_m)))

                    ProfileSection(title = stringResource(R.string.profile_section_assistance)) {
                        ProfileOptionItem(
                            icon = Icons.AutoMirrored.Filled.Chat,
                            title = stringResource(R.string.profile_action_chat),
                            subtitle = stringResource(R.string.profile_action_chat_desc),
                            onClick = onOpenChat
                        )
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_m)))
                    
                    ProfileSection(title = stringResource(R.string.profile_section_about)) {
                        ProfileOptionItem(
                            icon = Icons.Default.AccountCircle,
                            title = stringResource(R.string.profile_app_version),
                            subtitle = state.appVersion,
                            showChevron = false,
                            onClick = {}
                        )
                    }

                    if (state.showBottomSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { viewModel.onShowBottomSheet(false) },
                            sheetState = sheetState
                        ) {
                            LinkAccountSheetContent(
                                state = state,
                                onEmailChanged = viewModel::onEmailChanged,
                                onPasswordChanged = viewModel::onPasswordChanged,
                                onLinkEmailPassword = viewModel::onLinkWithEmailPassword,
                                onLinkGoogle = {
                                    scope.launch {
                                        // TODO: Implementar handleGoogleLink
                                    }
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_xl)))
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.screen_horizontal_padding))
    ) {
        // Título de sección con fondo traslúcido para legibilidad
        Surface(
            color = Color.White.copy(alpha = 0.15f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
        
        ElevatedCard(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(content = content)
        }
    }
}
