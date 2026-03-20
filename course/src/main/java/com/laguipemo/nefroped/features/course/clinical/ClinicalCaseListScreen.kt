package com.laguipemo.nefroped.features.course.clinical

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laguipemo.nefroped.designsystem.R
import com.laguipemo.nefroped.designsystem.components.SystemBarsController
import com.laguipemo.nefroped.features.course.clinical.components.ClinicalCaseCard
import com.laguipemo.nefroped.features.course.clinical.components.ResourceItem
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicalCaseListScreen(
    onBackClick: () -> Unit,
    onCaseClick: (String, String) -> Unit,
    viewModel: ClinicalCaseListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val darkTheme = isSystemInDarkTheme()
    val context = LocalContext.current

    SystemBarsController(
        useStatusDarkIcons = false,
        useNavigationDarkIcons = !darkTheme
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.clinical_cases_section_title), 
                        fontWeight = FontWeight.Bold, 
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back), tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                ClinicalCaseListUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
                is ClinicalCaseListUiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                is ClinicalCaseListUiState.Content -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(dimensionResource(R.dimen.space_m)),
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.space_l))
                    ) {
                        item {
                            Text(
                                text = stringResource(R.string.clinical_cases_subtitle),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = dimensionResource(R.dimen.space_xs))
                            )
                        }

                        items(state.cases) { clinicalCase ->
                            ClinicalCaseCard(
                                clinicalCase = clinicalCase,
                                onClick = { clinicalCase.quizId?.let { onCaseClick(it, clinicalCase.title) } }
                            )
                        }

                        if (state.resources.isNotEmpty()) {
                            item {
                                Column {
                                    HorizontalDivider(
                                        color = Color.White.copy(alpha = 0.2f), 
                                        modifier = Modifier.padding(vertical = dimensionResource(R.dimen.space_s))
                                    )
                                    Text(
                                        text = stringResource(R.string.clinical_resources_title),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = stringResource(R.string.clinical_resources_subtitle),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            items(state.resources) { resource ->
                                ResourceItem(
                                    resource = resource,
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resource.url))
                                        context.startActivity(intent)
                                    }
                                )
                            }
                        }
                        
                        item { Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_48))) }
                    }
                }
            }
        }
    }
}
