package com.kostas.gohealth.services

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kostas.gohealth.data.DatabaseProvider
import com.kostas.gohealth.helpers.calculateCaloriesGoal
import com.kostas.gohealth.helpers.calculatePushUpsGoal
import com.kostas.gohealth.helpers.calculateWaterGoal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// It updates the remote Firestore database with the user's needed details, total water, calories, push-ups goals completed and total steps
class LeaderboardSyncWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val database = DatabaseProvider.getDatabase(applicationContext)
            val userTrackings = database.trackingsDao().getAll().first().firstOrNull()
            val userCharacteristics = database.characteristicsDao().getAll().first().firstOrNull()
            val userSettings = database.settingsDao().getAll().first().firstOrNull()
            val firestore = FirebaseFirestore.getInstance()

            try {
                // Will probably never trigger, just for the compiler
                if (userTrackings == null || userCharacteristics == null || userSettings == null) {
                    return@withContext Result.failure()
                }

                val waterProgressSum = userTrackings.waterProgress.sum()
                val caloriesProgressSum = userTrackings.caloriesProgress.sum()
                val pushUpsProgressSum = userTrackings.pushUpsProgress.sum()
                val stepsProgress = userTrackings.stepsProgress.toLong()

                val waterGoal = calculateWaterGoal(userCharacteristics)
                val caloriesGoal = calculateCaloriesGoal(userCharacteristics)
                val pushUpsGoal = calculatePushUpsGoal(userCharacteristics)

                val updateData = hashMapOf(
                    "username" to (userSettings.username ?: ""),
                    "profilePictureString" to userSettings.profilePictureString,
                    "waterGoalsCompleted" to FieldValue.increment(if (waterProgressSum >= waterGoal) 1L else 0L),
                    "caloriesGoalsCompleted" to FieldValue.increment(if (caloriesProgressSum >= caloriesGoal) 1L else 0L),
                    "pushUpsGoalsCompleted" to FieldValue.increment(if (pushUpsProgressSum >= pushUpsGoal) 1L else 0L),
                    "totalSteps" to FieldValue.increment(stepsProgress)
                )

                firestore.collection("leaderboard")
                    .document(userSettings.firestoreId)
                    .set(updateData, SetOptions.merge())
                    .await()

                Result.success()
            }

            // If there is no internet connection, try again later
            catch (e: Exception) {
                e.printStackTrace()
                Result.retry()
            }
        }
    }
}
