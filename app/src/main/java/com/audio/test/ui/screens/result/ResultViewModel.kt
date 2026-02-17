package com.audio.test.ui.screens.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audio.test.domain.model.Session
import com.audio.test.domain.usecase.GetSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val getSessionUseCase: GetSessionUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sessionId: Long = checkNotNull(savedStateHandle["sessionId"]).toString().toLong()

    private val _session = MutableStateFlow<Session?>(null)
    val session = _session.asStateFlow()

    init {
        loadSession()
    }

    private fun loadSession() {
        viewModelScope.launch {
            _session.value = getSessionUseCase(sessionId)
        }
    }
}
