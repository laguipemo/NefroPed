package com.laguipemo.nefroped.features.admin.topics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laguipemo.nefroped.core.domain.model.course.Topic
import com.laguipemo.nefroped.core.domain.model.course.TopicType
import com.laguipemo.nefroped.core.domain.usecase.course.ObserveTopicUseCase
import com.laguipemo.nefroped.core.domain.usecase.course.SaveTopicUseCase
import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class TopicFormUiState(
    val title: String = "",
    val description: String = "",
    val order: Int = 0,
    val type: TopicType = TopicType.LESSONS,
    val imageUrl: String? = null,
    val contentUrl: String? = null,
    val indexContent: String? = null,
    val conversationId: String? = null,
    val selectedImageUri: ByteArray? = null,
    val isLoading: Boolean = false,
    val isSaveSuccess: Boolean = false,
    val error: String? = null
)

sealed interface TopicFormEvent {
    data class TitleChanged(val title: String) : TopicFormEvent
    data class DescriptionChanged(val description: String) : TopicFormEvent
    data class OrderChanged(val order: Int) : TopicFormEvent
    data class TypeChanged(val type: TopicType) : TopicFormEvent
    data class ImageSelected(val bytes: ByteArray) : TopicFormEvent
    data object Submit : TopicFormEvent
}

class AdminTopicFormViewModel(
    private val topicId: String?,
    private val observeTopic: ObserveTopicUseCase,
    private val saveTopic: SaveTopicUseCase,
    private val repository: CourseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TopicFormUiState())
    val uiState: StateFlow<TopicFormUiState> = _uiState.asStateFlow()

    private var originalTopic: Topic? = null

    init {
        topicId?.let { id ->
            loadTopic(id)
        }
    }

    private fun loadTopic(id: String) {
        viewModelScope.launch {
            observeTopic(id).filterNotNull().first().let { topic ->
                originalTopic = topic
                _uiState.update {
                    it.copy(
                        title = topic.title,
                        description = topic.description,
                        order = topic.order,
                        type = topic.type,
                        imageUrl = topic.imageUrl,
                        contentUrl = topic.contentUrl,
                        indexContent = topic.indexContent,
                        conversationId = topic.conversationId
                    )
                }
            }
        }
    }

    fun onEvent(event: TopicFormEvent) {
        when (event) {
            is TopicFormEvent.TitleChanged -> _uiState.update { it.copy(title = event.title) }
            is TopicFormEvent.DescriptionChanged -> _uiState.update { it.copy(description = event.description) }
            is TopicFormEvent.OrderChanged -> _uiState.update { it.copy(order = event.order) }
            is TopicFormEvent.TypeChanged -> _uiState.update { it.copy(type = event.type) }
            is TopicFormEvent.ImageSelected -> _uiState.update { it.copy(selectedImageUri = event.bytes) }
            TopicFormEvent.Submit -> saveTopic()
        }
    }

    private fun saveTopic() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                var finalImageUrl = _uiState.value.imageUrl
                
                // 1. Subir imagen si se seleccionó una nueva
                _uiState.value.selectedImageUri?.let { bytes ->
                    val fileName = "topic_${UUID.randomUUID()}.jpg"
                    val uploadResult = repository.uploadTopicImage(bytes, fileName)
                    finalImageUrl = uploadResult.getOrThrow()
                }

                // 2. Crear objeto Topic con todos los parámetros
                val topic = Topic(
                    id = topicId ?: UUID.randomUUID().toString(),
                    title = _uiState.value.title,
                    description = _uiState.value.description,
                    imageUrl = finalImageUrl,
                    imagePlaceholder = originalTopic?.imagePlaceholder,
                    contentUrl = _uiState.value.contentUrl,
                    indexContent = originalTopic?.indexContent, // Preservamos si ya estaba descargado
                    order = _uiState.value.order,
                    type = _uiState.value.type,
                    conversationId = _uiState.value.conversationId,
                    lessonsCount = originalTopic?.lessonsCount ?: 0,
                    completedLessonsCount = originalTopic?.completedLessonsCount ?: 0
                )

                // 3. Guardar en Supabase
                saveTopic(topic).getOrThrow()
                android.util.Log.d("AdminTopicForm", "Save successful for topic: ${topic.title}")
                _uiState.update { it.copy(isLoading = false, isSaveSuccess = true) }
                
            } catch (e: Exception) {
                android.util.Log.e("AdminTopicForm", "Error saving topic", e)
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error al guardar") }
            }
        }
    }
}
