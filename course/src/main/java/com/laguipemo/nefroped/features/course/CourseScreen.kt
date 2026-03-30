package com.laguipemo.nefroped.features.course

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laguipemo.nefroped.core.domain.model.course.TopicType
import com.laguipemo.nefroped.designsystem.R
import com.laguipemo.nefroped.designsystem.components.SystemBarsController
import com.laguipemo.nefroped.features.course.components.TopicCard
import com.laguipemo.nefroped.features.profile.notifications.NotificationViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseScreen(
    modifier: Modifier = Modifier,
    viewModel: CourseViewModel = koinViewModel(),
    notificationViewModel: NotificationViewModel = koinViewModel(), // Inyectado para el badge
    onTopicClick: (String) -> Unit,
    onChatClick: (String, String) -> Unit,
    onClinicalCasesClick: (String) -> Unit,
    onNotificationsClick: () -> Unit // Nuevo parámetro
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery: String by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSearchActive: Boolean by viewModel.isSearchActive.collectAsStateWithLifecycle()
    val unreadNotifications by notificationViewModel.unreadCount.collectAsStateWithLifecycle()

    val keyboardController = LocalSoftwareKeyboardController.current
    val darkTheme = isSystemInDarkTheme()

    SystemBarsController(
        useStatusDarkIcons = false,
        useNavigationDarkIcons = !darkTheme
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            navigationIcon = {
                BadgedBox(
                    modifier = Modifier.padding(start = 8.dp),
                    badge = {
                        if (unreadNotifications > 0) {
                            Badge {
                                Text(text = unreadNotifications.toString())
                            }
                        }
                    }
                ) {
                    IconButton(onClick = onNotificationsClick) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notificaciones",
                            tint = Color.White
                        )
                    }
                }
            },
            actions = {
                IconButton(onClick = { viewModel.onSearchActiveChange(!isSearchActive) }) {
                    Icon(
                        imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = stringResource(R.string.course_search_placeholder),
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White,
                actionIconContentColor = Color.White
            )
        )

        if (isSearchActive) {
            SearchBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                onCloseClick = { viewModel.onSearchActiveChange(false) },
                keyboardController = keyboardController
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                CourseUiState.Loading -> CircularProgressIndicator(color = Color.White)
                is CourseUiState.Content -> {
                    TopicsList(
                        state = state,
                        onTopicClick = onTopicClick,
                        onChatClick = onChatClick,
                        onClinicalCasesClick = onClinicalCasesClick
                    )
                }
                is CourseUiState.Error -> Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onCloseClick: () -> Unit,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                stringResource(R.string.course_search_placeholder),
                color = Color.White.copy(alpha = 0.6f)
            )
        },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White.copy(alpha = 0.5f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
            cursorColor = Color.White,
            focusedContainerColor = Color.White.copy(alpha = 0.1f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
        ),
        shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.screen_horizontal_padding))
            .padding(vertical = dimensionResource(R.dimen.space_s)),
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = {
            onCloseClick()
            keyboardController?.hide()
        })
    )
}

@Composable
private fun TopicsList(
    state: CourseUiState.Content,
    onTopicClick: (String) -> Unit,
    onChatClick: (String, String) -> Unit,
    onClinicalCasesClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.course_topics_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(
                horizontal = dimensionResource(R.dimen.screen_horizontal_padding),
                vertical = dimensionResource(R.dimen.screen_vertical_padding)
            )
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = dimensionResource(R.dimen.screen_horizontal_padding)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.space_l)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(state.topics) { _, topic ->
                TopicCard(
                    topic = topic,
                    onClick = { 
                        if (topic.type == TopicType.CLINICAL_CASES) {
                            onClinicalCasesClick(topic.id)
                        } else {
                            onTopicClick(topic.id)
                        }
                    },
                    onChatClick = { 
                        topic.conversationId?.let { id -> 
                            onChatClick(id, topic.title)
                        } 
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_xl)))
    }
}
