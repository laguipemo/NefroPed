package com.laguipemo.nefroped.features.course.quiz.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.laguipemo.nefroped.core.domain.model.course.*
import com.laguipemo.nefroped.designsystem.R
import com.laguipemo.nefroped.features.course.quiz.UserSelection

@Composable
fun QuestionCard(
    question: Question,
    currentSelection: UserSelection,
    onOptionSelected: (Int) -> Unit,
    onMatchSelected: (Int, Int) -> Unit,
    onUnmatch: (Int) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.space_m))) {
            question.intro?.takeIf { it.isNotBlank() }?.let { 
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_m)))
            }

            if (question.type != QuestionType.MATCH_DEFINITION) {
                Text(text = question.text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, lineHeight = 28.sp)
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_l)))
            }

            when (question.type) {
                QuestionType.TRUE_FALSE, QuestionType.ONE_CHOICE, QuestionType.MULTIPLE_CHOICE -> {
                    val options = (question.options as? QuestionOptions.Simple)?.list ?: emptyList()
                    options.forEachIndexed { index, text ->
                        val isSelected = when (currentSelection) { 
                            is UserSelection.Single -> currentSelection.index == index
                            is UserSelection.Multiple -> currentSelection.indices.contains(index)
                            else -> false 
                        }
                        OptionItem(text = text, isSelected = isSelected, onClick = { onOptionSelected(index) })
                    }
                }
                QuestionType.MATCH_DEFINITION -> {
                    val opts = question.options as? QuestionOptions.Match ?: return@Column
                    MatchDefinitionContent(
                        instructionText = question.text, 
                        terms = opts.terms, 
                        definitions = opts.definitions, 
                        currentMapping = (currentSelection as? UserSelection.Match)?.mapping ?: emptyMap(), 
                        onMatchSelected = onMatchSelected, 
                        onUnmatch = onUnmatch
                    )
                }
            }
        }
    }
}
