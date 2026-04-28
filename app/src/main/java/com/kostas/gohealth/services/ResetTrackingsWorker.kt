package com.kostas.gohealth.services

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.kostas.gohealth.data.DatabaseProvider
import com.kostas.gohealth.helpers.calculateCaloriesGoal
import com.kostas.gohealth.helpers.calculatePushUpsGoal
import com.kostas.gohealth.helpers.calculateWaterGoal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

// Resets the normal fields in the trackings table, also increases the unsynced fields, in case there is no network at midnight to send the
// data at the remote database, this function is chained with the leaderboard sync one to ensure that they both happen at midnight and the
// reset executes first
class ResetTrackingsWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val database = DatabaseProvider.getDatabase(applicationContext)
                val trackingsDao = database.trackingsDao()
                val userTrackings = trackingsDao.getAll().first().firstOrNull()
                val userCharacteristics = database.characteristicsDao().getAll().first().firstOrNull()

                // Triggers on a fresh install
                if (userTrackings == null || userCharacteristics == null) {
                    return@withContext Result.retry()
                }

                val waterGoal = calculateWaterGoal(userCharacteristics)
                val caloriesGoal = calculateCaloriesGoal(userCharacteristics)
                val pushUpsGoal = calculatePushUpsGoal(userCharacteristics)

                val waterGoalCompleted = if (userTrackings.waterProgress.sum() >= waterGoal) 1 else 0
                val caloriesGoalCompleted = if (userTrackings.caloriesProgress.sum() >= caloriesGoal) 1 else 0
                val pushUpsGoalCompleted = if (userTrackings.pushUpsProgress.sum() >= pushUpsGoal) 1 else 0
                val totalSteps = userTrackings.stepsProgress

                val updateUserTrackings = userTrackings.copy(
                    unsyncedWaterGoalsCompleted = userTrackings.unsyncedWaterGoalsCompleted + waterGoalCompleted,
                    unsyncedCaloriesGoalsCompleted = userTrackings.unsyncedCaloriesGoalsCompleted + caloriesGoalCompleted,
                    unsyncedPushUpsGoalsCompleted = userTrackings.unsyncedPushUpsGoalsCompleted + pushUpsGoalCompleted,
                    unsyncedTotalSteps = userTrackings.unsyncedTotalSteps + totalSteps,

                    waterProgress = emptyList(),
                    caloriesProgress = emptyList(),
                    pushUpsProgress = emptyList(),
                    stepsProgress = 0
                )

                trackingsDao.update(updateUserTrackings)

                // Every new request replaces the old one, since all the data needed is added up in the local database
                val workRequest = OneTimeWorkRequestBuilder<LeaderboardSyncWorker>()
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )

                    .build()

                WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                    "leaderboard_sync",
                    androidx.work.ExistingWorkPolicy.REPLACE,
                    workRequest
                )

                Result.success()
            }

            catch (e: Exception) {
                e.printStackTrace()
                Result.retry()
            }
        }
    }
}
