package com.learn.easy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learn.easy.data.repository.UserPreferencesRepository
import com.learn.easy.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val startDestination: StateFlow<String?> = userPreferencesRepository.isOnboardingCompleted
        .map { completed ->
            if (completed) Screen.Home.route else Screen.Onboarding.route
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}
