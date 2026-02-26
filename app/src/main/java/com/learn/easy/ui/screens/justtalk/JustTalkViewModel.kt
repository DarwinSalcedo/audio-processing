package com.learn.easy.ui.screens.justtalk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learn.easy.data.repository.WordProcess
import com.learn.easy.domain.repository.SpeechRecognitionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JustTalkViewModel @Inject constructor(
    private val speechRepository: SpeechRecognitionRepository
) : ViewModel() {

    private val _spokenText = MutableStateFlow("")
    val spokenText = _spokenText.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening = _isListening.asStateFlow()

    private val _interimText = MutableStateFlow("")
    val interimText = _interimText.asStateFlow()

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
            speechRepository.startListening().collectLatest { result ->
                when (result) {
                    is WordProcess.Final -> {
                        _spokenText.value += "${result.words} "
                        _interimText.value = ""
                    }
                    is WordProcess.Partial -> {
                        _interimText.value = result.words
                    }
                }
            }
        }
    }

    private fun stopRecording() {
        viewModelScope.launch {
            speechRepository.stopListening()
            _isListening.value = false
            _interimText.value = ""
        }
    }
    
    fun clearText() {
        _spokenText.value = ""
        _interimText.value = ""
    }
}
