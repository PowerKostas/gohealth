package com.kostas.gohealth.services

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kostas.gohealth.data.DatabaseProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// It updates the remote Firestore database with the user's needed details, total water, calories, push-ups goals completed and total
// steps, this function is called from ResetTrackingsWorker
class LeaderboardSyncWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val database = DatabaseProvider.getDatabase(applicationContext)
            val userTrackings = database.trackingsDao().getAll().first().firstOrNull()
            val userSettings = database.settingsDao().getAll().first().firstOrNull()
            val firestore = FirebaseFirestore.getInstance()

            try {
                // Triggers on a fresh install
                if (userTrackings == null || userSettings == null) {
                    return@withContext Result.retry()
                }

                val unsyncedWaterGoalsCompleted = userTrackings.unsyncedWaterGoalsCompleted
                val unsyncedCaloriesGoalsCompleted = userTrackings.unsyncedCaloriesGoalsCompleted
                val unsyncedPushUpsGoalsCompleted = userTrackings.unsyncedPushUpsGoalsCompleted
                val unsyncedTotalSteps = userTrackings.unsyncedTotalSteps

                val updateData = hashMapOf(
                    "username" to (userSettings.username ?: ""),
                    "profilePictureString" to userSettings.profilePictureString,
                    "waterGoalsCompleted" to FieldValue.increment(unsyncedWaterGoalsCompleted.toLong()),
                    "caloriesGoalsCompleted" to FieldValue.increment(unsyncedCaloriesGoalsCompleted.toLong()),
                    "pushUpsGoalsCompleted" to FieldValue.increment(unsyncedPushUpsGoalsCompleted.toLong()),
                    "totalSteps" to FieldValue.increment(unsyncedTotalSteps.toLong())
                )

                firestore.collection("leaderboards")
                    .document(Firebase.auth.currentUser?.uid ?: "")
                    .set(updateData, SetOptions.merge())
                    .await()

                // Because there is .await() this function could be on hold for a long time, grabs new user trackings to not update with
                // old data
                val freshUserTrackings = database.trackingsDao().getAll().first().firstOrNull()
                if (freshUserTrackings != null) {
                    database.trackingsDao().update(
                        freshUserTrackings.copy(
                            unsyncedWaterGoalsCompleted = freshUserTrackings.unsyncedWaterGoalsCompleted - unsyncedWaterGoalsCompleted,
                            unsyncedCaloriesGoalsCompleted = freshUserTrackings.unsyncedCaloriesGoalsCompleted - unsyncedCaloriesGoalsCompleted,
                            unsyncedPushUpsGoalsCompleted = freshUserTrackings.unsyncedPushUpsGoalsCompleted - unsyncedPushUpsGoalsCompleted,
                            unsyncedTotalSteps = freshUserTrackings.unsyncedTotalSteps - unsyncedTotalSteps
                        )
                    )
                }

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
