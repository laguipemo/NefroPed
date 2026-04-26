package com.laguipemo.nefroped.features.admin.quizzes.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.laguipemo.nefroped.core.domain.model.course.*

@Composable
fun AdminQuestionItem(
    index: Int,
    question: Question,
    onUpdate: (Question) -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = index.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = question.type.name.replace("_", " "),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }
                
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }

            OutlinedTextField(
                value = question.text,
                onValueChange = { onUpdate(question.copy(text = it)) },
                label = { Text("Pregunta") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = question.intro ?: "",
                        onValueChange = { onUpdate(question.copy(intro = it.ifBlank { null })) },
                        label = { Text("Introducción / Contexto (opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Opciones y Respuesta Correcta", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                    when (question.type) {
                        QuestionType.TRUE_FALSE -> TrueFalseEditor(question, onUpdate)
                        QuestionType.ONE_CHOICE -> SingleChoiceEditor(question, onUpdate)
                        QuestionType.MULTIPLE_CHOICE -> MultipleChoiceEditor(question, onUpdate)
                        QuestionType.MATCH_DEFINITION -> MatchDefinitionEditor(question, onUpdate)
                    }

                    OutlinedTextField(
                        value = question.explanation ?: "",
                        onValueChange = { onUpdate(question.copy(explanation = it.ifBlank { null })) },
                        label = { Text("Explicación de la respuesta") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun TrueFalseEditor(question: Question, onUpdate: (Question) -> Unit) {
    val isTrue = (question.correctAnswer as? QuestionAnswer.Single)?.index == 0
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = isTrue, onClick = { onUpdate(question.copy(correctAnswer = QuestionAnswer.Single(0))) })
        Text("Verdadero")
        Spacer(modifier = Modifier.width(16.dp))
        RadioButton(selected = !isTrue, onClick = { onUpdate(question.copy(correctAnswer = QuestionAnswer.Single(1))) })
        Text("Falso")
    }
}

@Composable
private fun SingleChoiceEditor(question: Question, onUpdate: (Question) -> Unit) {
    val options = (question.options as? QuestionOptions.Simple)?.list ?: listOf("", "")
    val correctIndex = (question.correctAnswer as? QuestionAnswer.Single)?.index ?: 0

    options.forEachIndexed { index, option ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = correctIndex == index, onClick = { onUpdate(question.copy(correctAnswer = QuestionAnswer.Single(index))) })
            OutlinedTextField(
                value = option,
                onValueChange = { newText ->
                    val newList = options.toMutableList().apply { this[index] = newText }
                    onUpdate(question.copy(options = QuestionOptions.Simple(newList)))
                },
                modifier = Modifier.weight(1f),
                label = { Text("Opción ${index + 1}") }
            )
            IconButton(onClick = {
                if (options.size > 2) {
                    val newList = options.toMutableList().apply { removeAt(index) }
                    onUpdate(question.copy(options = QuestionOptions.Simple(newList)))
                }
            }) {
                Icon(Icons.Default.RemoveCircleOutline, contentDescription = null)
            }
        }
    }
    TextButton(onClick = {
        val newList = options + ""
        onUpdate(question.copy(options = QuestionOptions.Simple(newList)))
    }) {
        Icon(Icons.Default.Add, contentDescription = null)
        Text("Añadir Opción")
    }
}

@Composable
private fun MultipleChoiceEditor(question: Question, onUpdate: (Question) -> Unit) {
    val options = (question.options as? QuestionOptions.Simple)?.list ?: listOf("", "")
    val correctIndices = (question.correctAnswer as? QuestionAnswer.Multiple)?.indices ?: emptyList()

    options.forEachIndexed { index, option ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = index in correctIndices,
                onCheckedChange = { checked ->
                    val newList = if (checked) correctIndices + index else correctIndices - index
                    onUpdate(question.copy(correctAnswer = QuestionAnswer.Multiple(newList)))
                }
            )
            OutlinedTextField(
                value = option,
                onValueChange = { newText ->
                    val newList = options.toMutableList().apply { this[index] = newText }
                    onUpdate(question.copy(options = QuestionOptions.Simple(newList)))
                },
                modifier = Modifier.weight(1f),
                label = { Text("Opción ${index + 1}") }
            )
        }
    }
    TextButton(onClick = {
        val newList = options + ""
        onUpdate(question.copy(options = QuestionOptions.Simple(newList)))
    }) {
        Icon(Icons.Default.Add, contentDescription = null)
        Text("Añadir Opción")
    }
}

@Composable
private fun MatchDefinitionEditor(question: Question, onUpdate: (Question) -> Unit) {
    val options = (question.options as? QuestionOptions.Match) ?: QuestionOptions.Match(emptyList(), emptyList())
    val mapping = (question.correctAnswer as? QuestionAnswer.Match)?.mapping ?: emptyMap()

    options.terms.forEachIndexed { index, term ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = term,
                onValueChange = { newText ->
                    val newTerms = options.terms.toMutableList().apply { this[index] = newText }
                    onUpdate(question.copy(options = options.copy(terms = newTerms)))
                },
                modifier = Modifier.weight(1f),
                label = { Text("Término ${index + 1}") }
            )
            Icon(Icons.Default.ArrowForward, contentDescription = null)
            OutlinedTextField(
                value = options.definitions.getOrElse(index) { "" },
                onValueChange = { newText ->
                    val newDefs = options.definitions.toMutableList()
                    if (index < newDefs.size) newDefs[index] = newText else newDefs.add(newText)
                    onUpdate(question.copy(options = options.copy(definitions = newDefs)))
                },
                modifier = Modifier.weight(1f),
                label = { Text("Definición ${index + 1}") }
            )
        }
    }
    
    TextButton(onClick = {
        val newTerms = options.terms + ""
        val newDefs = options.definitions + ""
        val newMapping = mapping + (options.terms.size to options.terms.size)
        onUpdate(question.copy(
            options = QuestionOptions.Match(newTerms, newDefs),
            correctAnswer = QuestionAnswer.Match(newMapping)
        ))
    }) {
        Icon(Icons.Default.Add, contentDescription = null)
        Text("Añadir Par")
    }
}
