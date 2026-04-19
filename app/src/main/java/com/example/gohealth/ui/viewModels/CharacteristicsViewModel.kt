package com.example.gohealth.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.gohealth.data.DatabaseProvider
import com.example.gohealth.data.daos.CharacteristicsDao
import com.example.gohealth.data.entities.Characteristics
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CharacteristicsViewModel(private val characteristicsDao: CharacteristicsDao) : ViewModel() {
    // A ViewModel factory, have to use it, otherwise the view model doesn't work
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[APPLICATION_KEY])
                val database = DatabaseProvider.getDatabase(application)
                return CharacteristicsViewModel(database.characteristicsDao()) as T
            }
        }
    }

    // The users variable holds every entry in the users table, because the app only supports a single local user, there will probably only
    // be one entry
    val characteristics: StateFlow<List<Characteristics>> = characteristicsDao.getAll().stateIn(
        scope = viewModelScope,
        initialValue = emptyList(),
        started = SharingStarted.WhileSubscribed(5000)
    )

    // Runs every time the app opens and checks if a user hasn't been created yet (first time the app opens). If there are no users it
    // inserts a default user
    init {
        viewModelScope.launch {
            if (characteristicsDao.getAll().first().isEmpty()) {
                val defaultCharacteristics = Characteristics(
                    gender = "",
                    age = null,
                    height = null,
                    weight = null,
                    activityLevel = "",
                    weightGoal = ""
                )

                characteristicsDao.insert(defaultCharacteristics)
            }
        }
    }

    // Public function that the respective screens call
    fun updateUserCharacteristics(characteristics: Characteristics) {
        viewModelScope.launch {
            characteristicsDao.update(characteristics)
        }
    }
}
