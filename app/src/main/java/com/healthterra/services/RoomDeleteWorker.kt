package com.healthterra.services

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.healthterra.data.UserDatabase
import com.healthterra.helpers.generateRandomProfilePictureString
import kotlinx.coroutines.flow.firstOrNull

class RoomDeleteWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val randomUsername = inputData.getString("randomUsername") ?: "Anonymous"
        val database = UserDatabase.getDatabase(applicationContext)

        val characteristicsDao = database.characteristicsDao()
        val userCharacteristics = characteristicsDao.getAll().firstOrNull()?.firstOrNull()

        if (userCharacteristics != null) {
            val resetCharacteristics = userCharacteristics.copy(
                gender = null,
                age = null,
                height = null,
                weight = null,
                activityLevel = null,
                weightGoal = "Maintain",
                kgGoal = 0,
                daysGoal = 0
            )

            characteristicsDao.update(resetCharacteristics)
        }

        val settingsDao = database.settingsDao()
        val userSettings = settingsDao.getAll().firstOrNull()?.firstOrNull()

        if (userSettings != null) {
            val resetSettings = userSettings.copy(
                profilePictureString = generateRandomProfilePictureString(),
                username = randomUsername,
                leaderboardsVisibility = "Anonymous",
                notifiedAchievements = ""
            )

            settingsDao.update(resetSettings)
        }

        val todayTrackingsDao = database.todayTrackingsDao()
        val userTodayTrackings = todayTrackingsDao.getAll().firstOrNull()?.firstOrNull()

        if (userTodayTrackings != null) {
            val resetTodayTrackings = userTodayTrackings.copy(
                waterProgress = emptyList(),
                caloriesProgress = emptyList(),
                exerciseProgress = emptyList(),
                stepsProgress = 0,
                caloriesBurned = 0
            )

            todayTrackingsDao.update(resetTodayTrackings)
        }

        val dailyTrackingsDao = database.dailyTrackingsDao()
        dailyTrackingsDao.delete()

        val achievementsDao = database.achievementsDao()
        val userAchievements = achievementsDao.getAll().firstOrNull()?.firstOrNull()

        if (userAchievements != null) {
            val resetAchievements = userAchievements.copy(
                appearWaterLeaderboards = false,
                appearCaloriesLeaderboards = false,
                appearExerciseLeaderboards = false,
                appearStepsLeaderboards = false,
                appearTotalStepsLeaderboards = false,
                appearHealthiestUser = false,
                secret = false
            )

            achievementsDao.update(resetAchievements)
        }

        return Result.success()
    }
}