package com.healthterra.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.healthterra.data.UserDatabase
import com.healthterra.data.daos.DailyTrackingsDao
import com.healthterra.data.daos.TodayTrackingsDao
import com.healthterra.data.entities.DailyTrackings
import com.healthterra.helpers.calculateAllTimeTrackings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// This view model doesn't get initialized because a new row doesn't need to be created, if the user doesn't log anything that day
class DailyTrackingsViewModel(
    private val dailyTrackingsDao: DailyTrackingsDao,
    todayTrackingsDao: TodayTrackingsDao
) : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[APPLICATION_KEY])
                val database = UserDatabase.getDatabase(application)
                return DailyTrackingsViewModel(database.dailyTrackingsDao(), database.todayTrackingsDao()) as T
            }
        }
    }

    fun dailyTrackings(): Flow<List<DailyTrackings>> {
        return dailyTrackingsDao.getDailyTrackings()
    }

    fun dailyTrackingsFromYearMonth(yearMonth: String): Flow<List<DailyTrackings>> {
        return dailyTrackingsDao.getDailyTrackingsFromYearMonth(yearMonth)
    }

    fun oldestYearMonthUserDailyTrackings(): Flow<String?> {
        return dailyTrackingsDao.getOldestYearMonth()
    }

    fun upsertUserDailyTrackings(dailyTrackings: DailyTrackings) {
        viewModelScope.launch {
            dailyTrackingsDao.upsert(dailyTrackings)
        }
    }

    fun deleteUserDailyTrackings() {
        viewModelScope.launch {
            dailyTrackingsDao.delete()
        }
    }

    val allTimeTrackingsList = combine(dailyTrackingsDao.getDailyTrackings(), todayTrackingsDao.getAll()) { dailyTrackingsList, todayTrackings ->
        val allTimeTrackings = calculateAllTimeTrackings(dailyTrackingsList, todayTrackings.firstOrNull())
        listOf(
            allTimeTrackings.waterProgress,
            allTimeTrackings.caloriesProgress,
            allTimeTrackings.exerciseProgress,
            allTimeTrackings.totalSteps,
            allTimeTrackings.caloriesBurned
        )
    }.stateIn(
        scope = viewModelScope,
        initialValue = listOf(0, 0, 0, 0, 0),
        started = SharingStarted.WhileSubscribed(5000)
    )
}
