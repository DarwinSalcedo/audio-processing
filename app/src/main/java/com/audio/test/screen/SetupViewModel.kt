package com.audio.test.screen

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor() : ViewModel() {
    var rawText: String = ""

    fun addText(inputText: String) {
        rawText = inputText
    }




}