package com.paestudiar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.paestudiar.data.ContentRepository
import com.paestudiar.data.ContentState
import com.paestudiar.data.Graph
import com.paestudiar.data.Progress
import com.paestudiar.data.ProgressRepository
import com.paestudiar.data.todayEpochDay
import com.paestudiar.data.withAnswer
import com.paestudiar.data.withLessonToggled
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(
    private val contentRepository: ContentRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _content = MutableStateFlow<ContentState>(ContentState.Loading)
    val content: StateFlow<ContentState> = _content.asStateFlow()

    val progress: StateFlow<Progress> = progressRepository.progress
        .stateIn(viewModelScope, SharingStarted.Eagerly, Progress())

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _content.value = ContentState.Loading
            _content.value = try {
                ContentState.Ready(contentRepository.loadAll())
            } catch (e: Exception) {
                ContentState.Error(e.message ?: "No se pudo cargar el contenido")
            }
        }
    }

    fun recordAnswer(questionId: String, correct: Boolean) {
        viewModelScope.launch {
            progressRepository.update { it.withAnswer(questionId, correct, todayEpochDay()) }
        }
    }

    fun toggleLessonDone(topicId: String) {
        viewModelScope.launch {
            progressRepository.update { it.withLessonToggled(topicId, todayEpochDay()) }
        }
    }

    fun resetProgress() {
        viewModelScope.launch { progressRepository.reset() }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { AppViewModel(Graph.content, Graph.progress) }
        }
    }
}
