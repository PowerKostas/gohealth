package com.healthterra.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.healthterra.data.UserDatabase
import com.healthterra.data.daos.AchievementsDao
import com.healthterra.data.daos.CharacteristicsDao
import com.healthterra.data.daos.DailyTrackingsDao
import com.healthterra.data.daos.TodayTrackingsDao
import com.healthterra.data.entities.Achievements
import com.healthterra.helpers.OtherAchievements
import com.healthterra.helpers.calculateOtherAchievements
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

    val leaderboardsAchievements: StateFlow<List<Achievements>> = achievementsDao.getAll().stateIn(
        scope = viewModelScope,
        initialValue = emptyList(),
        started = SharingStarted.WhileSubscribed(5000)
    )

    // Some of the Achievements fields (total goals met, streaks and max steps) can be derived from the other tables, for that reason they are
    // not included in the Achievements table, but they are fetched and provided from this view model
    val otherAchievements: StateFlow<OtherAchievements?> = combine(
        dailyTrackingsDao.getDailyTrackings(),
        todayTrackingsDao.getAll(),
        characteristicsDao.getAll().map { it.firstOrNull() }
    ) { dailyTrackingsList, todayTrackings, userCharacteristics ->
        if (userCharacteristics == null) {
            return@combine null
        }

        calculateOtherAchievements(
            dailyTrackingsList = dailyTrackingsList,
            todayTrackings = todayTrackings.firstOrNull(),
            userCharacteristics = userCharacteristics
        )
    }.stateIn(
        scope = viewModelScope,
        initialValue = null,
        started = SharingStarted.WhileSubscribed(5000)
    )

    // The leaderboards achievements are handled only by the cloud functions, for that reason a listener to the user's Firestore document
    // is set up, and it's waiting for any changes in the achievements field to update the local database
    fun startFirestoreAchievementsListener() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewModelScope.launch {
            // If the user has unlocked all leaderboards achievements, there is no reason to set up the listener
            val leaderboardsAchievements = achievementsDao.getAll().first().firstOrNull()

            val hasAllAchievements = leaderboardsAchievements?.appearWaterLeaderboards ?: false &&
                leaderboardsAchievements.appearCaloriesLeaderboards && leaderboardsAchievements.appearExerciseLeaderboards &&
                leaderboardsAchievements.appearStepsLeaderboards && leaderboardsAchievements.appearTotalStepsLeaderboards &&
                leaderboardsAchievements.secret

            if (hasAllAchievements) {
                return@launch
            }

            FirebaseFirestore.getInstance().collection("users").document(uid).addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val firestoreUserAchievements = snapshot.get("achievements") as? Map<*, *> ?: return@addSnapshotListener

                viewModelScope.launch {
                    val localAchievementsData = achievementsDao.getAll().first().firstOrNull() ?: return@launch

                    // The first part of the OR statement guarantees that if a field is true, it will never go to false
                    val updatedAchievements = localAchievementsData.copy(
                        appearWaterLeaderboards = localAchievementsData.appearWaterLeaderboards || firestoreUserAchievements["appearWaterLeaderboards"] as? Boolean ?: false,
                        appearCaloriesLeaderboards = localAchievementsData.appearCaloriesLeaderboards ||firestoreUserAchievements["appearCaloriesLeaderboards"] as? Boolean ?: false,
                        appearExerciseLeaderboards = localAchievementsData.appearExerciseLeaderboards || firestoreUserAchievements["appearExerciseLeaderboards"] as? Boolean ?: false,
                        appearStepsLeaderboards = localAchievementsData.appearStepsLeaderboards|| firestoreUserAchievements["appearStepsLeaderboards"] as? Boolean ?: false,
                        appearTotalStepsLeaderboards = localAchievementsData.appearTotalStepsLeaderboards || firestoreUserAchievements["appearTotalStepsLeaderboards"] as? Boolean ?: false,
                        secret = localAchievementsData.secret || firestoreUserAchievements["secret"] as? Boolean ?: false
                    )

                    if (localAchievementsData != updatedAchievements) {
                        achievementsDao.update(updatedAchievements)
                    }
                }
            }
        }
    }

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
