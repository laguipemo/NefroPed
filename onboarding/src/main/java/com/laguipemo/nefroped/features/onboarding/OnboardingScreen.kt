package com.laguipemo.nefroped.features.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.laguipemo.nefroped.designsystem.R
import com.laguipemo.nefroped.designsystem.components.SystemBarsController
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = koinViewModel()
) {
    val pages = listOf(
        OnboardingPage(
            title = stringResource(R.string.onboarding_title_1),
            description = stringResource(R.string.onboarding_desc_1),
            icon = Icons.Default.AutoAwesome,
            color = MaterialTheme.colorScheme.primary
        ),
        OnboardingPage(
            title = stringResource(R.string.onboarding_title_2),
            description = stringResource(R.string.onboarding_desc_2),
            icon = Icons.AutoMirrored.Filled.Chat,
            color = MaterialTheme.colorScheme.secondary
        ),
        OnboardingPage(
            title = stringResource(R.string.onboarding_title_3),
            description = stringResource(R.string.onboarding_desc_3),
            icon = Icons.Default.Timeline,
            color = MaterialTheme.colorScheme.tertiary
        ),
        OnboardingPage(
            title = stringResource(R.string.onboarding_title_4),
            description = stringResource(R.string.onboarding_desc_4),
            icon = Icons.Default.NotificationsActive,
            color = MaterialTheme.colorScheme.errorContainer
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    SystemBarsController(
        useStatusDarkIcons = false,
        useNavigationDarkIcons = false
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to MaterialTheme.colorScheme.primary,
                        0.5f to MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        1.0f to MaterialTheme.colorScheme.background
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(R.dimen.space_m)),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { viewModel.onOnboardingFinished() }) {
                    Text(
                        text = stringResource(R.string.onboarding_skip),
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { position ->
                OnboardingPageContent(page = pages[position])
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(R.dimen.space_xl))
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(pages.size) { iteration ->
                        val isSelected = pagerState.currentPage == iteration
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (isSelected) 24.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color.White else Color.White.copy(alpha = 0.3f))
                        )
                    }
                }

                Button(
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        } else {
                            viewModel.onOnboardingFinished()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage < pages.size - 1) 
                            stringResource(R.string.onboarding_next) 
                        else 
                            stringResource(R.string.onboarding_start),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimensionResource(R.dimen.space_xl)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier
                .size(200.dp)
                .padding(bottom = 32.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.2f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color.White
                )
            }
        }

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.White.copy(alpha = 0.9f),
            lineHeight = 24.sp
        )
    }
}
