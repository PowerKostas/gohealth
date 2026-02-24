package com.example.gohealth.data.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

// Saves the state of the theme option
class ThemeViewModel : ViewModel() {
    var selectedTheme by mutableStateOf("Light")
        private set

    fun update(newTheme: String) {
        selectedTheme = newTheme
    }
}
