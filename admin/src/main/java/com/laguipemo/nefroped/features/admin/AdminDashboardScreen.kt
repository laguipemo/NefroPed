package com.laguipemo.nefroped.features.admin

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.laguipemo.nefroped.designsystem.R
import com.laguipemo.nefroped.designsystem.components.SystemBarsController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onBackClick: () -> Unit,
    onManageTopicsClick: () -> Unit,
    onManageQuizzesClick: () -> Unit,
    onManageClinicalCasesClick: () -> Unit
) {
    val darkTheme = isSystemInDarkTheme()

    // Configuración inmersiva para que los iconos contrasten con el degradado oscuro
    SystemBarsController(
        useStatusDarkIcons = false,
        useNavigationDarkIcons = !darkTheme
    )

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0), // Inmersión total
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Panel del Profesor", 
                        fontWeight = FontWeight.Bold, 
                        color = Color.White 
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding) // Crucial para que el contenido fluya bajo las barras
                .padding(horizontal = dimensionResource(R.dimen.screen_horizontal_padding))
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.space_m))
        ) {
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_s)))
            
            Text(
                text = "¿Qué deseas gestionar hoy?",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.8f)
            )

            AdminMenuCard(
                title = "Temas y Lecciones",
                description = "Crea nuevos temas, sube Markdowns y recursos multimedia.",
                icon = Icons.Default.Book,
                onClick = onManageTopicsClick
            )

            AdminMenuCard(
                title = "Autoevaluaciones",
                description = "Configura las preguntas y respuestas de los Quizzes.",
                icon = Icons.Default.Quiz,
                onClick = onManageQuizzesClick
            )

            AdminMenuCard(
                title = "Casos Clínicos",
                description = "Diseña escenarios interactivos para los alumnos.",
                icon = Icons.AutoMirrored.Filled.Assignment,
                onClick = onManageClinicalCasesClick
            )
            
            // Añadimos padding para la barra de navegación
            Spacer(modifier = Modifier.navigationBarsPadding())
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_xl)))
        }
    }
}

@Composable
private fun AdminMenuCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
