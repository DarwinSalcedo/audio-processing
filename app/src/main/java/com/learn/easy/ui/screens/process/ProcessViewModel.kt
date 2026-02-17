package com.learn.easy.ui.screens.process

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learn.easy.data.repository.WordProcess
import com.learn.easy.domain.model.Line
import com.learn.easy.domain.model.LineResult
import com.learn.easy.domain.model.Session
import com.learn.easy.domain.model.WordResult
import com.learn.easy.domain.repository.SpeechRecognitionRepository
import com.learn.easy.domain.usecase.ParseTextToLinesUseCase
import com.learn.easy.domain.usecase.ProcessSpeechUseCase
import com.learn.easy.domain.usecase.SaveSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProcessViewModel @Inject constructor(
    private val speechRepository: SpeechRecognitionRepository,
    private val saveSessionUseCase: SaveSessionUseCase,
    private val processSpeechUseCase: ProcessSpeechUseCase,
    private val parseTextToLinesUseCase: ParseTextToLinesUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val originalText: String = checkNotNull(savedStateHandle["text"])

    private val _navigateToResult = Channel<Long>()
    val navigateToResult = _navigateToResult.receiveAsFlow()

    // Accumulate all spoken text for final result
    private val _spokenTextBuilder = StringBuilder()

    private val _isListening = MutableStateFlow(false)
    val isListening = _isListening.asStateFlow()

    private val _lines = MutableStateFlow<List<Line>>(emptyList())
    val lines = _lines.asStateFlow()

    private val _currentLineIndex = MutableStateFlow(0)
    val currentLineIndex = _currentLineIndex.asStateFlow()

    private val _hasStarted = MutableStateFlow(false)
    val hasStarted = _hasStarted.asStateFlow()

    init {
        initializeLines()
    }

    private fun initializeLines() {
        _lines.value = parseTextToLinesUseCase(originalText)
    }

    fun toggleRecording() {
        if (_isListening.value) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        _isListening.value = true
        viewModelScope.launch {
            speechRepository.startListening().collectLatest { partialOrFinal ->
                processSpeech(partialOrFinal)
            }
        }
    }

    private fun processSpeech(spoken: WordProcess) {
        val currentIndex = _currentLineIndex.value
        val currentLines = _lines.value
        
        val result = processSpeechUseCase(spoken, currentLines, currentIndex) ?: return

        _hasStarted.value = result.hasStarted
        _lines.value = result.updatedLines

        result.additionalSpokenText?.let { text ->
            _spokenTextBuilder.append(text).append(" ")
        }

        if (result.shouldAdvanceLine) {
            _currentLineIndex.value = currentIndex + 1
        } else if (result.completedLineIndex == currentLines.lastIndex) {
            stopRecording()
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            speechRepository.stopListening()
            _isListening.value = false
            // Do not save immediately. Wait for user action.
        }
    }

    fun finishSession() {
        saveSession()
    }

    fun resetSession() {
        viewModelScope.launch {
            speechRepository.stopListening()
            _isListening.value = false
            _spokenTextBuilder.clear()
            _currentLineIndex.value = 0
            _hasStarted.value = false
            initializeLines()
        }
    }

    private fun saveSession() {
        viewModelScope.launch {
            val currentLines = _lines.value
            val totalAccuracy = currentLines.map { it.accuracy }.average().toInt()

            val lineResults = currentLines.map { line ->
                LineResult(
                    originalText = line.text,
                    spokenText = "", // Not tracking granular spoken text per line yet
                    accuracy = line.accuracy,
                    words = line.words.map { word ->
                        WordResult(
                            originalWord = word.text,
                            isMatched = word.isMatched
                        )
                    }
                )
            }

            val session = Session(
                timestamp = System.currentTimeMillis(),
                originalText = originalText,
                spokenText = _spokenTextBuilder.toString().ifEmpty { "Incomplete session" },
                score = totalAccuracy,
                audioPath = null,
                lines = lineResults
            )
            val id = saveSessionUseCase(session)
            _navigateToResult.send(id)
        }
    }
}
