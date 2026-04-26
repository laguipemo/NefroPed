package com.laguipemo.nefroped.features.admin.topics

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laguipemo.nefroped.core.domain.model.course.ClinicalCase
import com.laguipemo.nefroped.core.domain.model.course.ExternalLink
import com.laguipemo.nefroped.core.domain.model.course.Lesson
import com.laguipemo.nefroped.core.domain.model.course.Topic
import com.laguipemo.nefroped.core.domain.model.course.TopicType
import com.laguipemo.nefroped.core.domain.usecase.course.*
import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class TopicFormUiState(
    val title: String = "",
    val description: String = "",
    val order: Int = 0,
    val type: TopicType = TopicType.THEORY,
    val imageUrl: String? = null,
    val contentUrl: String? = null,
    val indexContent: String? = null,
    val conversationId: String? = null,
    val selectedImageUri: ByteArray? = null,
    val lessons: List<Lesson> = emptyList(),
    val clinicalCases: List<ClinicalCase> = emptyList(),
    val externalLinks: List<ExternalLink> = emptyList(),
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
    
    // Eventos para Enlaces Externos
    data class SaveExternalLink(val link: ExternalLink) : TopicFormEvent
    data class DeleteExternalLink(val linkId: String) : TopicFormEvent

    // Eventos para Lecciones
    data class DeleteLesson(val lessonId: String) : TopicFormEvent

    // Eventos para Casos Clínicos
    data class DeleteClinicalCase(val caseId: String) : TopicFormEvent
    data class DeleteTopic(val topicId: String) : TopicFormEvent
}

class AdminTopicFormViewModel(
    private val topicId: String?,
    private val observeTopic: ObserveTopicUseCase,
    private val saveTopic: SaveTopicUseCase,
    private val observeExternalLinks: ObserveExternalLinksUseCase,
    private val syncExternalLinks: SyncExternalLinksUseCase,
    private val saveExternalLink: SaveExternalLinkUseCase,
    private val deleteExternalLink: DeleteExternalLinkUseCase,
    private val observeClinicalCases: ObserveClinicalCasesUseCase,
    private val syncClinicalData: SyncClinicalDataUseCase,
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
            // Primero observamos el tema. Si no está en Room, no emitirá nada.
            observeTopic(id).filterNotNull().collect { topic ->
                Log.d("AdminTopicFormVM", "Tema detectado en Room: ${topic.id}, Tipo: ${topic.type}")
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
        
        // Lanzamos las sincronizaciones y observaciones. 
        // Si el tema no existe en Room, las inserciones de lecciones/casos fallarán por FK.
        // Pero al ser edición, el tema DEBERÍA existir.
        loadLessons(id)
        loadClinicalCases(id)
        loadExternalLinks(id)
    }

    private fun loadLessons(id: String) {
        viewModelScope.launch {
            repository.syncLessons(id)
            repository.observeLessons(id).collect { lessons ->
                Log.d("AdminTopicFormVM", "Lecciones cargadas: ${lessons.size}")
                _uiState.update { it.copy(lessons = lessons.sortedBy { l -> l.order }) }
            }
        }
    }

    private fun loadClinicalCases(id: String) {
        viewModelScope.launch {
            Log.d("AdminTopicFormVM", "Iniciando sincronización de casos para: $id")
            val result = syncClinicalData(id)
            if (result.isFailure) {
                Log.e("AdminTopicFormVM", "Error sincronizando casos clínicos", result.exceptionOrNull())
            }
            
            observeClinicalCases(id)
                .onEach { cases -> 
                    Log.d("AdminTopicFormVM", "Observados ${cases.size} casos clínicos en Room para $id")
                }
                .collect { cases ->
                    _uiState.update { it.copy(clinicalCases = cases.sortedBy { c -> c.title }) }
                }
        }
    }

    private fun loadExternalLinks(id: String) {
        viewModelScope.launch {
            syncExternalLinks(id)
            observeExternalLinks(id).collect { links ->
                Log.d("AdminTopicFormVM", "Enlaces externos cargados: ${links.size}")
                _uiState.update { it.copy(externalLinks = links.sortedBy { l -> l.order }) }
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
            is TopicFormEvent.SaveExternalLink -> saveExternalLink(event.link)
            is TopicFormEvent.DeleteExternalLink -> deleteExternalLink(event.linkId)
            is TopicFormEvent.DeleteLesson -> deleteLesson(event.lessonId)
            is TopicFormEvent.DeleteClinicalCase -> deleteClinicalCase(event.caseId)
            is TopicFormEvent.DeleteTopic -> deleteTopic(event.topicId)
        }
    }

    private fun deleteTopic(topicId: String) {
        viewModelScope.launch {
            repository.deleteTopic(topicId)
        }
    }

    private fun deleteLesson(lessonId: String) {
        viewModelScope.launch {
            repository.deleteLesson(lessonId)
        }
    }

    private fun deleteClinicalCase(caseId: String) {
        viewModelScope.launch {
            repository.deleteClinicalCase(caseId)
        }
    }

    private fun saveExternalLink(link: ExternalLink) {
        viewModelScope.launch {
            saveExternalLink.invoke(link)
        }
    }

    private fun deleteExternalLink(linkId: String) {
        viewModelScope.launch {
            deleteExternalLink.invoke(linkId)
        }
    }

    private fun saveTopic() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                var finalImageUrl = _uiState.value.imageUrl
                
                _uiState.value.selectedImageUri?.let { bytes ->
                    val fileName = "topic_${UUID.randomUUID()}.jpg"
                    val uploadResult = repository.uploadTopicImage(bytes, fileName)
                    finalImageUrl = uploadResult.getOrThrow()
                }

                val topic = Topic(
                    id = topicId ?: UUID.randomUUID().toString(),
                    title = _uiState.value.title,
                    description = _uiState.value.description,
                    imageUrl = finalImageUrl,
                    imagePlaceholder = originalTopic?.imagePlaceholder,
                    contentUrl = _uiState.value.contentUrl,
                    indexContent = originalTopic?.indexContent,
                    order = _uiState.value.order,
                    type = _uiState.value.type,
                    conversationId = _uiState.value.conversationId,
                    lessonsCount = originalTopic?.lessonsCount ?: 0,
                    completedLessonsCount = originalTopic?.completedLessonsCount ?: 0
                )

                saveTopic(topic).getOrThrow()
                _uiState.update { it.copy(isLoading = false, isSaveSuccess = true) }
                
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error al guardar") }
            }
        }
    }
}
