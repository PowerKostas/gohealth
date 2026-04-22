package com.example.gohealth.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.gohealth.data.DatabaseProvider
import com.example.gohealth.data.daos.SettingsDao
import com.example.gohealth.data.daos.TrackingsDao
import com.example.gohealth.data.entities.Settings
import com.example.gohealth.data.entities.Trackings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class TrackingsViewModel(private val trackingsDao: TrackingsDao, private val settingsDao: SettingsDao) : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[APPLICATION_KEY])
                val database = DatabaseProvider.getDatabase(application)
                return TrackingsViewModel(database.trackingsDao(), settingsDao = database.settingsDao()) as T
            }
        }
    }

    val trackings: StateFlow<List<Trackings>> = trackingsDao.getAll().stateIn(
        scope = viewModelScope,
        initialValue = emptyList(),
        started = SharingStarted.WhileSubscribed(5000)
    )

    fun initializeUserTrackings(userId: Int) {
        viewModelScope.launch {
            if (trackingsDao.getAll().first().isEmpty()) {
                val defaultTrackings = Trackings(
                    userId = userId,
                    waterProgress = listOf(500, 200),
                    caloriesProgress = emptyList(),
                    pushUpsProgress = emptyList(),
                    stepsProgress = emptyList()
                )

                trackingsDao.insert(defaultTrackings)
            }
        }
    }

    fun updateUserTrackings(trackings: Trackings) {
        viewModelScope.launch {
            trackingsDao.update(trackings)
        }
    }

    // Resets the tracking table if today is after the last reset date and a specific time has passed, the second part is just for
    // testing, since I just want it to reset at midnight
    fun resetUserTrackings(trackings: Trackings, settings: Settings) {
        if (LocalDate.now() > settings.lastResetDate && LocalTime.now() >= LocalTime.of(24, 0)) {
            viewModelScope.launch {
                val resetTrackings = trackings.copy(
                    waterProgress = emptyList(),
                    caloriesProgress = emptyList(),
                    pushUpsProgress = emptyList(),
                    stepsProgress = emptyList()
                )

                trackingsDao.update(resetTrackings)
                settingsDao.update(settings.copy(lastResetDate = LocalDate.now()))
            }
        }
    }
}
