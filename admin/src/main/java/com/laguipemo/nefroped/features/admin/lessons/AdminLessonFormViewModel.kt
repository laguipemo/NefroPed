package com.laguipemo.nefroped.features.admin.lessons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laguipemo.nefroped.core.domain.model.course.Lesson
import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class LessonFormUiState(
    val title: String = "",
    val description: String? = null,
    val order: Int = 0,
    val content: String = "",
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val pdfUrl: String? = null,
    val audioUrl: String? = null,
    val isLoading: Boolean = false,
    val isUploadingResource: Boolean = false,
    val isSaveSuccess: Boolean = false,
    val error: String? = null
)

sealed interface LessonFormEvent {
    data class TitleChanged(val title: String) : LessonFormEvent
    data class DescriptionChanged(val description: String) : LessonFormEvent
    data class OrderChanged(val order: Int) : LessonFormEvent
    data class ContentChanged(val content: String) : LessonFormEvent
    data class ImageUrlChanged(val url: String?) : LessonFormEvent
    data class VideoUrlChanged(val url: String) : LessonFormEvent
    data class PdfUrlChanged(val url: String) : LessonFormEvent
    data class AudioUrlChanged(val url: String) : LessonFormEvent
    data class LessonFileContentSelected(val content: String) : LessonFormEvent
    data class UploadResource(val byteArray: ByteArray, val fileName: String, val type: ResourceType) : LessonFormEvent
    data class UploadImage(val byteArray: ByteArray, val fileName: String) : LessonFormEvent
    data object Submit : LessonFormEvent
    data object Delete : LessonFormEvent
}

enum class ResourceType {
    VIDEO, PDF, AUDIO, IMAGE
}

class AdminLessonFormViewModel(
    private val topicId: String,
    private val lessonId: String?,
    private val repository: CourseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LessonFormUiState())
    val uiState = _uiState.asStateFlow()

    private var originalLesson: Lesson? = null

    init {
        if (!lessonId.isNullOrBlank() && lessonId != "null") {
            loadLesson(lessonId)
        } else {
            // Calcular el siguiente orden sugerido
            calculateNextOrder()
        }
    }

    private fun loadLesson(id: String) {
        if (id.isBlank() || id == "null") return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Obtenemos la primera emisión de la lista de lecciones
            val lessons = repository.observeLessons(topicId).firstOrNull() ?: emptyList()
            val lesson = lessons.find { it.id == id }
            
            if (lesson != null) {
                originalLesson = lesson
                _uiState.update {
                    it.copy(
                        title = lesson.title,
                        description = lesson.description,
                        order = lesson.order,
                        content = lesson.content,
                        imageUrl = lesson.imageUrl,
                        videoUrl = lesson.videoUrl,
                        pdfUrl = lesson.pdfUrl,
                        audioUrl = lesson.audioUrl,
                        isLoading = false
                    )
                }
            } else {
                // Si no se encuentra, desactivamos la carga
                // No ponemos error aquí para evitar el mensaje molesto si es un ID recién creado o inválido
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun calculateNextOrder() {
        viewModelScope.launch {
            val lessons = repository.observeLessons(topicId).first()
            val nextOrder = (lessons.maxOfOrNull { it.order } ?: 0) + 1
            _uiState.update { it.copy(order = nextOrder) }
        }
    }

    fun onEvent(event: LessonFormEvent) {
        when (event) {
            is LessonFormEvent.TitleChanged -> _uiState.update { it.copy(title = event.title) }
            is LessonFormEvent.DescriptionChanged -> _uiState.update { it.copy(description = event.description) }
            is LessonFormEvent.OrderChanged -> _uiState.update { it.copy(order = event.order) }
            is LessonFormEvent.ContentChanged -> _uiState.update { it.copy(content = event.content) }
            is LessonFormEvent.ImageUrlChanged -> _uiState.update { it.copy(imageUrl = event.url) }
            is LessonFormEvent.VideoUrlChanged -> _uiState.update { it.copy(videoUrl = event.url.ifBlank { null }) }
            is LessonFormEvent.PdfUrlChanged -> _uiState.update { it.copy(pdfUrl = event.url.ifBlank { null }) }
            is LessonFormEvent.AudioUrlChanged -> _uiState.update { it.copy(audioUrl = event.url.ifBlank { null }) }
            is LessonFormEvent.LessonFileContentSelected -> _uiState.update { it.copy(content = event.content) }
            is LessonFormEvent.UploadResource -> uploadResource(event.byteArray, event.fileName, event.type)
            is LessonFormEvent.UploadImage -> uploadImage(event.byteArray, event.fileName)
            LessonFormEvent.Submit -> saveLesson()
            LessonFormEvent.Delete -> deleteLesson()
        }
    }

    private fun deleteLesson() {
        lessonId?.let { id ->
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                repository.deleteLesson(id)
                _uiState.update { it.copy(isLoading = false, isSaveSuccess = true) }
            }
        }
    }

    private fun uploadImage(byteArray: ByteArray, fileName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingResource = true, error = null) }
            repository.uploadLessonImage(byteArray, fileName)
                .onSuccess { url ->
                    _uiState.update { it.copy(imageUrl = url, isUploadingResource = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isUploadingResource = false, error = "Error al subir imagen: ${e.message}") }
                }
        }
    }

    private fun uploadResource(byteArray: ByteArray, fileName: String, type: ResourceType) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingResource = true, error = null) }
            val folder = when (type) {
                ResourceType.VIDEO -> "videos"
                ResourceType.PDF -> "documents"
                ResourceType.AUDIO -> "audio"
                ResourceType.IMAGE -> "images"
            }
            
            repository.uploadLessonResource(byteArray, fileName, folder)
                .onSuccess { url ->
                    _uiState.update { state ->
                        when (type) {
                            ResourceType.VIDEO -> state.copy(videoUrl = url, isUploadingResource = false)
                            ResourceType.PDF -> state.copy(pdfUrl = url, isUploadingResource = false)
                            ResourceType.AUDIO -> state.copy(audioUrl = url, isUploadingResource = false)
                            ResourceType.IMAGE -> state.copy(imageUrl = url, isUploadingResource = false)
                        }
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isUploadingResource = false, error = "Error al subir: ${e.message}") }
                }
        }
    }

    private fun saveLesson() {
        val currentState = uiState.value
        if (currentState.title.isBlank() || currentState.content.isBlank()) {
            _uiState.update { it.copy(error = "El título y el contenido son obligatorios") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val lesson = Lesson(
                id = if (!lessonId.isNullOrBlank() && lessonId != "null") lessonId else UUID.randomUUID().toString(),
                topicId = topicId,
                title = currentState.title,
                description = currentState.description,
                order = currentState.order,
                content = currentState.content,
                videoUrl = currentState.videoUrl,
                pdfUrl = currentState.pdfUrl,
                audioUrl = currentState.audioUrl,
                imageUrl = currentState.imageUrl,
                isCompleted = originalLesson?.isCompleted ?: false
            )

            repository.saveLesson(lesson)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isSaveSuccess = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }
}
