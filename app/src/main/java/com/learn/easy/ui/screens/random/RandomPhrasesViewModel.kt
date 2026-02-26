package com.learn.easy.ui.screens.random

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learn.easy.data.Phrase
import com.learn.easy.data.repository.PhrasesRepository
import com.learn.easy.data.repository.WordProcess
import com.learn.easy.domain.repository.SpeechRecognitionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RandomPhrasesViewModel @Inject constructor(
    private val speechRepository: SpeechRecognitionRepository,
    private val phrasesRepository: PhrasesRepository
) : ViewModel() {

    private val _gameState = MutableStateFlow<GameState>(GameState.Setup)
    val gameState = _gameState.asStateFlow()

    private val _currentPhrase = MutableStateFlow<Phrase?>(null)
    val currentPhrase = _currentPhrase.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score = _score.asStateFlow()

    private val _timeLeft = MutableStateFlow(120)
    val timeLeft = _timeLeft.asStateFlow()

    private val _spokenText = MutableStateFlow("")
    val spokenText = _spokenText.asStateFlow()

    private val _isCurrentPhraseMatched = MutableStateFlow(false)
    val isCurrentPhraseMatched = _isCurrentPhraseMatched.asStateFlow()

    private val _topics = MutableStateFlow<List<String>>(emptyList())
    val topics = _topics.asStateFlow()

    private val _selectedTopic = MutableStateFlow<String?>(null)
    val selectedTopic = _selectedTopic.asStateFlow()

    private val unassertedPhrases = mutableListOf<Phrase>()

    private var timerJob: Job? = null
    private var recognitionJob: Job? = null

    init {
        loadTopics()
    }

    private fun loadTopics() {
        _topics.value = phrasesRepository.getTopics()
    }

    fun selectTopic(topic: String?) {
        _selectedTopic.value = topic
    }

    fun startGame() {
        _score.value = 0
        _timeLeft.value = 120
        _isCurrentPhraseMatched.value = false

        val allPhrases = if (_selectedTopic.value != null) {
            phrasesRepository.getPhrasesByTopic(_selectedTopic.value!!)
        } else {
            phrasesRepository.getAllPhrases()
        }

        unassertedPhrases.clear()
        unassertedPhrases.addAll(allPhrases.shuffled())

        if (unassertedPhrases.isEmpty()) {
            _gameState.value = GameState.Finished
            return
        }

        _gameState.value = GameState.Playing
        nextPhrase()
        startTimer()
        startListening()
    }

    fun goToSetup() {
        stopListening()
        timerJob?.cancel()
        _gameState.value = GameState.Setup
        _currentPhrase.value = null
        _spokenText.value = ""
        _isCurrentPhraseMatched.value = false
        _selectedTopic.value = null
    }

    private fun nextPhrase() {
        if (unassertedPhrases.isEmpty()) {
            endGame()
            return
        }
        _currentPhrase.value = unassertedPhrases.removeFirst()
        _spokenText.value = ""
        _isCurrentPhraseMatched.value = false
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timeLeft.value > 0 && _gameState.value == GameState.Playing) {
                delay(1000)
                _timeLeft.value -= 1
            }
            if (_gameState.value == GameState.Playing) {
                endGame()
            }
        }
    }

    private fun startListening() {
        recognitionJob?.cancel()
        recognitionJob = viewModelScope.launch {
            speechRepository.startListening().collectLatest { result ->
                if (_gameState.value == GameState.Playing) {
                    processSpeech(result)
                }
            }
        }
    }

    private fun processSpeech(result: WordProcess) {
        val spoken = result.words.trim().lowercase()

        when (result) {
            is WordProcess.Partial -> {
                _spokenText.value = result.words
            }
            is WordProcess.Final -> {
                _spokenText.value = result.words

                // Check for "next" command
                if (spoken == "next" || spoken == "siguiente") {
                    nextPhrase()
                    return
                }

                // Don't re-evaluate already matched phrase
                if (_isCurrentPhraseMatched.value) return

                val target = _currentPhrase.value?.en?.lowercase() ?: return

                if (isMatch(spoken, target)) {
                    _isCurrentPhraseMatched.value = true
                    _score.value += 1
                    // Show translation for 2 seconds, then auto-advance
                    viewModelScope.launch {
                        delay(2000)
                        if (_gameState.value == GameState.Playing) {
                            nextPhrase()
                        }
                    }
                }
            }
        }
    }

    private fun isMatch(spoken: String, target: String): Boolean {
        val cleanSpoken = spoken.replace(Regex("[^a-z0-9 ]"), "")
        val cleanTarget = target.replace(Regex("[^a-z0-9 ]"), "")
        return cleanSpoken == cleanTarget || cleanSpoken.contains(cleanTarget)
    }

    private fun endGame() {
        _gameState.value = GameState.Finished
        timerJob?.cancel()
        stopListening()
    }

    fun stopGame() {
        endGame()
    }

    private fun stopListening() {
        recognitionJob?.cancel()
        viewModelScope.launch {
            speechRepository.stopListening()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
        timerJob?.cancel()
    }
}

sealed class GameState {
    object Setup : GameState()
    object Playing : GameState()
    object Finished : GameState()
}
