package com.audio.test.ui.screens.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audio.test.domain.model.Session
import com.audio.test.domain.usecase.GetHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val getHistoryUseCase: GetHistoryUseCase
) : ViewModel() {

    private val _inputText = MutableStateFlow("")
    val inputText = _inputText.asStateFlow()

    private val _sessions = MutableStateFlow<List<Session>>(emptyList())
    val sessions = _sessions.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            getHistoryUseCase().collectLatest {
                _sessions.value = it
            }
        }
    }

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }
}
