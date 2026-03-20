package com.laguipemo.nefroped.features.course.quiz.components

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.laguipemo.nefroped.designsystem.R

@Composable
fun ClinicalHistoryCard(history: String) {
    var expanded by remember { mutableStateOf(false) }
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.HistoryEdu, null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.clinical_history_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = history,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}
