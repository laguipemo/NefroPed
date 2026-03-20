package com.laguipemo.nefroped.features.course.quiz.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.laguipemo.nefroped.designsystem.R

@Composable
fun MatchDefinitionContent(
    instructionText: String,
    terms: List<String>,
    definitions: List<String>,
    currentMapping: Map<Int, Int>,
    onMatchSelected: (Int, Int) -> Unit,
    onUnmatch: (Int) -> Unit
) {
    var selectedTermIndex by remember { mutableStateOf<Int?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.space_ms))) {
        terms.forEachIndexed { index, term ->
            val charLabel = ('A' + index).toString()
            val isUsed = currentMapping.containsKey(index)
            val isSelected = selectedTermIndex == index

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        if (isUsed) onUnmatch(index) 
                        else selectedTermIndex = if (isSelected) null else index 
                    },
                shape = RoundedCornerShape(dimensionResource(R.dimen.space_ms)),
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isUsed -> MaterialTheme.colorScheme.surfaceVariant
                    else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                },
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(
                    dimensionResource(R.dimen.border_stroke_width), 
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(dimensionResource(R.dimen.space_ms)), 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(dimensionResource(R.dimen.space_xl))
                            .clip(CircleShape)
                            .background(if (isSelected) Color.White else MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) { 
                        Text(
                            text = charLabel, 
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White, 
                            fontWeight = FontWeight.ExtraBold
                        ) 
                    }
                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.space_ms)))
                    Text(
                        text = term, 
                        style = MaterialTheme.typography.bodyMedium, 
                        modifier = Modifier.weight(1f), 
                        color = if (isSelected) Color.White else if (isUsed) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                    )
                    if (isUsed) {
                        Icon(
                            imageVector = Icons.Default.Close, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.outline, 
                            modifier = Modifier.size(dimensionResource(R.dimen.space_m))
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_m)))
        Text(
            text = instructionText, 
            style = MaterialTheme.typography.titleMedium, 
            fontWeight = FontWeight.Bold, 
            lineHeight = 24.sp
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = dimensionResource(R.dimen.space_s)), 
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
        
        definitions.forEachIndexed { defIndex, definition ->
            val matchedTermIndex = currentMapping.entries.find { it.value == defIndex }?.key
            val isMatched = matchedTermIndex != null
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(selectedTermIndex != null && !isMatched) { 
                        selectedTermIndex?.let { termIndex ->
                            onMatchSelected(termIndex, defIndex)
                            selectedTermIndex = null
                        }
                    },
                shape = RoundedCornerShape(dimensionResource(R.dimen.space_ms)),
                color = if (isMatched) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f) else Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(
                    dimensionResource(R.dimen.border_stroke_width), 
                    if (isMatched) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(dimensionResource(R.dimen.space_ms)), 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isMatched) {
                        Box(
                            modifier = Modifier
                                .padding(end = dimensionResource(R.dimen.space_ms))
                                .size(dimensionResource(R.dimen.quiz_match_indicator_size))
                                .background(MaterialTheme.colorScheme.secondary, CircleShape), 
                            contentAlignment = Alignment.Center
                        ) { 
                            Text(
                                text = ('A' + (matchedTermIndex ?: 0)).toString(), 
                                color = Color.White, 
                                fontWeight = FontWeight.Bold, 
                                fontSize = 14.sp
                            ) 
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .padding(end = dimensionResource(R.dimen.space_ms))
                                .size(dimensionResource(R.dimen.quiz_match_indicator_size))
                                .border(dimensionResource(R.dimen.border_stroke_width), MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                        )
                    }
                    Text(text = definition, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
