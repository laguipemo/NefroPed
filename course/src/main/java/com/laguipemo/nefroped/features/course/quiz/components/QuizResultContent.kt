package com.laguipemo.nefroped.features.course.quiz.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.laguipemo.nefroped.core.domain.model.course.QuizResult
import com.laguipemo.nefroped.designsystem.R
import java.util.Locale

@Composable
fun QuizResultContent(
    result: QuizResult,
    onFinish: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.size(160.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.1f)
            ) {}
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format(Locale.getDefault(), "%.1f", result.score),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = when (result.score) {
                        in 0.0..5.9 -> MaterialTheme.colorScheme.error
                        in 6.0..7.9 -> Color(0xFFFFA000)
                        in 8.0..8.9 -> MaterialTheme.colorScheme.primary
                        else -> Color(0xFF4CAF50)
                    }
                )
                Text(
                    text = stringResource(R.string.quiz_score_unit),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Image(
            painter = painterResource(id = R.drawable.ic_nefroped_logo),
            contentDescription = null,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.quiz_results_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            text = stringResource(R.string.quiz_correct_answers_label, result.correctAnswers, result.totalQuestions),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(stringResource(R.string.quiz_finish_button), fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onRetry) {
            Text(
                text = stringResource(R.string.quiz_retry_button),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
