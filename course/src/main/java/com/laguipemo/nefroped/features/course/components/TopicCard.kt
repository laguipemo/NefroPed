package com.laguipemo.nefroped.features.course.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.laguipemo.nefroped.core.domain.model.course.Topic
import com.laguipemo.nefroped.designsystem.R
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun TopicCard(
    topic: Topic,
    onClick: () -> Unit,
    onChatClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .width(dimensionResource(R.dimen.topic_card_width))
            .height(dimensionResource(R.dimen.topic_card_height))
            .padding(vertical = dimensionResource(R.dimen.space_xs))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(dimensionResource(R.dimen.topic_card_corner_radius)),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = dimensionResource(R.dimen.space_s))
    ) {
        Column {
            Box {
                AsyncImage(
                    model = topic.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(R.dimen.topic_card_image_height))
                        .clip(RoundedCornerShape(topStart = dimensionResource(R.dimen.topic_card_corner_radius), topEnd = dimensionResource(R.dimen.topic_card_corner_radius))),
                    contentScale = ContentScale.Crop
                )
                
                if (topic.conversationId != null) {
                    FilledTonalIconButton(
                        onClick = onChatClick,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(dimensionResource(R.dimen.space_m)),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = stringResource(R.string.chat_title),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Column(modifier = Modifier
                .padding(dimensionResource(R.dimen.space_l))
                .fillMaxSize()) {
                Text(
                    text = topic.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_s)))

                Text(
                    text = topic.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_s)))

                val scrollState = rememberScrollState()
                val canScrollDown by remember { 
                    derivedStateOf { scrollState.value < scrollState.maxValue } 
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(dimensionResource(R.dimen.space_m)))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(dimensionResource(R.dimen.space_m))
                ) {
                    Column(modifier = Modifier.verticalScroll(scrollState)) {
                        MarkdownText(
                            markdown = (topic.indexContent ?: "").replace("\\n", "\n"),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        )
                    }
                    
                    if (scrollState.maxValue > 0) {
                        Icon(
                            imageVector = if (canScrollDown) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(dimensionResource(R.dimen.space_m)),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_s)))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { topic.progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(dimensionResource(R.dimen.topic_card_progress_height))
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.space_s)))
                    Text(
                        text = "${(topic.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = stringResource(R.string.course_lessons_count, topic.completedLessonsCount, topic.lessonsCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.space_s))
                )
            }
        }
    }
}
