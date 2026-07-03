package com.healthterra.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.healthterra.data.UserDatabase
import com.healthterra.data.daos.AchievementsDao
import com.healthterra.data.daos.CharacteristicsDao
import com.healthterra.data.daos.DailyTrackingsDao
import com.healthterra.data.daos.TodayTrackingsDao
import com.healthterra.data.entities.Achievements
import com.healthterra.helpers.AchievementsData
import com.healthterra.helpers.calculateAchievementsData
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AchievementsViewModel(
    private val achievementsDao: AchievementsDao,
    private val dailyTrackingsDao: DailyTrackingsDao,
    private val todayTrackingsDao: TodayTrackingsDao,
    private val characteristicsDao: CharacteristicsDao
) : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[APPLICATION_KEY])
                val database = UserDatabase.getDatabase(application)

                return AchievementsViewModel(
                    achievementsDao = database.achievementsDao(),
                    dailyTrackingsDao = database.dailyTrackingsDao(),
                    todayTrackingsDao = database.todayTrackingsDao(),
                    characteristicsDao = database.characteristicsDao()
                ) as T
            }
        }
    }

    val achievements: StateFlow<List<Achievements>> = achievementsDao.getAll().stateIn(
        scope = viewModelScope,
        initialValue = emptyList(),
        started = SharingStarted.WhileSubscribed(5000)
    )

    // Some of the Achievements fields (total goals met, streaks and max steps) can be derived from the other tables, for that reason they are
    // not included in the Achievements table, but they are fetched and provided from this view model
    val achievementsData: StateFlow<AchievementsData?> = combine(
        dailyTrackingsDao.getDailyTrackings(),
        todayTrackingsDao.getAll(),
        characteristicsDao.getAll().map { it.firstOrNull() }
    ) { dailyTrackingsList, todayTrackings, userCharacteristics ->
        if (userCharacteristics == null) {
            return@combine null
        }

        calculateAchievementsData(
            dailyTrackingsList = dailyTrackingsList,
            todayTrackings = todayTrackings.firstOrNull(),
            userCharacteristics = userCharacteristics
        )
    }.stateIn(
        scope = viewModelScope,
        initialValue = null,
        started = SharingStarted.WhileSubscribed(5000)
    )

    fun initializeUserAchievements(userId: Int) {
        viewModelScope.launch {
            if (achievementsDao.getAll().first().isEmpty()) {
                val defaultAchievements = Achievements(
                    userId = userId,
                )

                achievementsDao.insert(defaultAchievements)
            }
        }
    }

    fun updateUserAchievements(newAchievements: Achievements) {
        viewModelScope.launch {
            achievementsDao.update(newAchievements)
        }
    }
}
