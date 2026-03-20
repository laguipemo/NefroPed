package com.laguipemo.nefroped.features.course.quiz.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.laguipemo.nefroped.designsystem.R
import com.laguipemo.nefroped.features.course.quiz.QuizUiState

@Composable
fun QuizPlayContent(
    state: QuizUiState.Content,
    onOptionSelected: (Int) -> Unit,
    onMatchSelected: (Int, Int) -> Unit,
    onUnmatch: (Int) -> Unit,
    onNextClick: () -> Unit,
    onPrevClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.space_m))
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        state.quiz.description?.takeIf { it.isNotBlank() }?.let { history ->
             ClinicalHistoryCard(history = history)
             Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_m)))
        }

        LinearProgressIndicator(
            progress = { state.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_s)))

        Text(
            text = stringResource(R.string.quiz_question_count, state.currentQuestionIndex + 1, state.quiz.questions.size),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_m)))

        QuestionCard(
            question = state.currentQuestion ?: return@Column,
            currentSelection = state.currentSelection,
            onOptionSelected = onOptionSelected,
            onMatchSelected = onMatchSelected,
            onUnmatch = onUnmatch
        )

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_xl)))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.space_m))
        ) {
            if (state.currentQuestionIndex > 0) {
                OutlinedButton(
                    onClick = onPrevClick,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.space_m)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                ) {
                    Text(stringResource(R.string.quiz_previous), fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = onNextClick,
                enabled = state.canContinue && !state.isSubmitting,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(dimensionResource(R.dimen.space_m)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.primary)
                } else {
                    Text(
                        text = if (state.isLastQuestion) stringResource(R.string.quiz_submit) else stringResource(R.string.quiz_next),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
