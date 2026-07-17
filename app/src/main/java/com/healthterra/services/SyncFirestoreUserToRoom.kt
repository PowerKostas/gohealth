package com.healthterra.services

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.healthterra.data.UserDatabase
import com.healthterra.data.entities.Achievements
import com.healthterra.data.entities.Characteristics
import com.healthterra.data.entities.DailyTrackings
import com.healthterra.data.entities.Settings
import com.healthterra.data.entities.TodayTrackings
import com.healthterra.helpers.calculateStepsGoal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import kotlin.math.max

// Signing in to an existing Google account clarification, the cloud data is written in the settings, characteristics and achievements
// tables, local and cloud data is merged for the daily and today trackings tables. When signing in to a new Google account the same
// happens, but the merging is not necessary, it just doesn't hurt to do it
suspend fun syncFirestoreUserToRoom(userDatabase: UserDatabase, context: Context) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()

    try {
        // Fetches the user document and the daily_trackings subcollection
        val userDocument = db.collection("users").document(uid).get().await()
        val dailyTrackingsCollection = db.collection("users").document(uid).collection("daily_trackings").get().await()

        if (!userDocument.exists()) {
            return
        }

        // Syncs local settings table, it's using .copy because it's the only table with local values that aren't stored in Firestore
        val userSettings = userDatabase.settingsDao().getAll().first().firstOrNull() ?: Settings(userId = 1)
        val settings = userSettings.copy(
            profilePictureString = userDocument.getString("profilePictureString") ?: userSettings.profilePictureString,
            username = userDocument.getString("username") ?: userSettings.username,
            appearance = userDocument.getString("appearance") ?: "Light",
            stepTracking = userDocument.getString("stepTracking") ?: "Enabled",
            lastSavedDate = userDocument.getString("lastSavedDate") ?: LocalDate.now().toString(),
            initialWeightGoalDate = userDocument.getString("initialWeightGoalDate"),
            leaderboardsVisibility = userDocument.getString("leaderboardsVisibility") ?: "Anonymous",
            showMandatoryDialog = false
        )

        userDatabase.settingsDao().update(settings)

        // Syncs local characteristics table
        val characteristics = Characteristics(
            userId = 1,
            gender = userDocument.getString("gender"),
            age = userDocument.getDouble("age")?.toFloat(),
            height = userDocument.getDouble("height")?.toFloat(),
            weight = userDocument.getDouble("weight")?.toFloat(),
            activityLevel = userDocument.getString("activityLevel"),
            weightGoal = userDocument.getString("weightGoal") ?: "Maintain",
            kgGoal = userDocument.getLong("kgGoal")?.toInt() ?: 0,
            daysGoal = userDocument.getLong("daysGoal")?.toInt() ?: 0
        )

        userDatabase.characteristicsDao().update(characteristics)

        // Sends new step goal to StepTracker
        val newStepGoal = calculateStepsGoal(characteristics)
        val intent = Intent(context, StepTracker::class.java).apply {
            action = "UPDATE_STEP_GOAL"
            putExtra("NEW_STEP_GOAL", newStepGoal)
        }

        context.startService(intent)

        // Syncs local achievements table
        val achievementsMap = userDocument.get("achievements") as? Map<*, *> ?: emptyMap<Any?, Any?>()
        val achievements = Achievements(
            userId = 1,
            appearWaterLeaderboards = achievementsMap["appearWaterLeaderboards"] as? Boolean ?: false,
            appearCaloriesLeaderboards = achievementsMap["appearCaloriesLeaderboards"] as? Boolean ?: false,
            appearExerciseLeaderboards = achievementsMap["appearExerciseLeaderboards"] as? Boolean ?: false,
            appearStepsLeaderboards = achievementsMap["appearStepsLeaderboards"] as? Boolean ?: false,
            appearTotalStepsLeaderboards = achievementsMap["appearTotalStepsLeaderboards"] as? Boolean ?: false,
            appearHealthiestUser = achievementsMap["appearHealthiestUser"] as? Boolean ?: false,
            secret = achievementsMap["secret"] as? Boolean ?: false
        )

        userDatabase.achievementsDao().update(achievements)

        // Merges every single local and cloud daily tracking document
        val localDailyTrackingsMap = userDatabase.dailyTrackingsDao().getDailyTrackings().first().associateBy { it.date }

        val dailyTrackingsList = dailyTrackingsCollection.documents
            .filter { it.id != LocalDate.now().toString() } // Removes today's document
            .map { dailyDocument ->
                val date = dailyDocument.id
                val cloudWaterProgress = dailyDocument.getLong("waterProgress")?.toInt() ?: 0
                val cloudCaloriesProgress = dailyDocument.getLong("caloriesProgress")?.toInt() ?: 0
                val cloudExerciseProgress = dailyDocument.getLong("exerciseProgress")?.toInt() ?: 0
                val cloudStepsProgress = dailyDocument.getLong("stepsProgress")?.toInt() ?: 0
                val cloudCaloriesBurned = dailyDocument.getLong("caloriesBurned")?.toInt() ?: 0

                val cloudWaterGoal = dailyDocument.getLong("waterGoal")?.toInt() ?: 0
                val cloudCaloriesGoal = dailyDocument.getLong("caloriesGoal")?.toInt() ?: 0
                val cloudExerciseGoal = dailyDocument.getLong("exerciseGoal")?.toInt() ?: 0
                val cloudStepsGoal = dailyDocument.getLong("stepsGoal")?.toInt() ?: 0

                val localDailyTracking = localDailyTrackingsMap[date]

                val localWaterProgress = localDailyTracking?.waterProgress ?: 0
                val localCaloriesProgress = localDailyTracking?.caloriesProgress ?: 0
                val localExerciseProgress = localDailyTracking?.exerciseProgress ?: 0
                val localStepsProgress = localDailyTracking?.stepsProgress ?: 0
                val localCaloriesBurned = localDailyTracking?.caloriesBurned ?: 0

                DailyTrackings(
                    userId = 1,
                    date = date,
                    waterProgress = max(cloudWaterProgress, localWaterProgress),
                    caloriesProgress = max(cloudCaloriesProgress, localCaloriesProgress),
                    exerciseProgress = max(cloudExerciseProgress, localExerciseProgress),
                    stepsProgress = max(cloudStepsProgress, localStepsProgress),

                    // The corresponding goal of the winner of local vs remote is selected
                    waterGoal = if (cloudWaterProgress >= localWaterProgress) cloudWaterGoal else (localDailyTracking?.waterGoal ?: cloudWaterGoal),
                    caloriesGoal = if (cloudCaloriesProgress >= localCaloriesProgress) cloudCaloriesGoal else (localDailyTracking?.caloriesGoal ?: cloudCaloriesGoal),
                    exerciseGoal = if (cloudExerciseProgress >= localExerciseProgress) cloudExerciseGoal else (localDailyTracking?.exerciseGoal ?: cloudExerciseGoal),
                    stepsGoal = if (cloudStepsProgress >= localStepsProgress) cloudStepsGoal else (localDailyTracking?.stepsGoal ?: cloudStepsGoal),

                    caloriesBurned = max(cloudCaloriesBurned, localCaloriesBurned
                    )
                )
            }

        dailyTrackingsList.forEach { tracking ->
            userDatabase.dailyTrackingsDao().upsert(tracking)
        }

        // Merges the local and remote today trackings table, it's just the latest daily tracking document
        val todayDocument = dailyTrackingsCollection.documents.find { it.id == LocalDate.now().toString() }

        if (todayDocument != null) {
            val userTodayTrackings = userDatabase.todayTrackingsDao().getAll().first().firstOrNull() ?: TodayTrackings(userId = 1)

            val cloudWaterProgress = todayDocument.getLong("waterProgress")?.toInt() ?: 0
            val cloudCaloriesProgress = todayDocument.getLong("caloriesProgress")?.toInt() ?: 0
            val cloudExerciseProgress = todayDocument.getLong("exerciseProgress")?.toInt() ?: 0
            val cloudStepsProgress = todayDocument.getLong("stepsProgress")?.toInt() ?: 0
            val cloudCaloriesBurned = todayDocument.getLong("caloriesBurned")?.toInt() ?: 0

            val localWaterSum = userTodayTrackings.waterProgress.sum()
            val localCaloriesSum = userTodayTrackings.caloriesProgress.sum()
            val localExerciseSum = userTodayTrackings.exerciseProgress.sum()
            val localCaloriesBurned = userTodayTrackings.caloriesBurned

            val todayTrackings = TodayTrackings(
                userId = 1,
                waterProgress = if (cloudWaterProgress > localWaterSum) listOf(cloudWaterProgress) else userTodayTrackings.waterProgress,
                caloriesProgress = if (cloudCaloriesProgress > localCaloriesSum) listOf(cloudCaloriesProgress) else userTodayTrackings.caloriesProgress,
                exerciseProgress = if (cloudExerciseProgress > localExerciseSum) listOf(cloudExerciseProgress) else userTodayTrackings.exerciseProgress,
                stepsProgress = max(cloudStepsProgress, userTodayTrackings.stepsProgress),
                caloriesBurned = max(cloudCaloriesBurned, localCaloriesBurned)
            )

            userDatabase.todayTrackingsDao().update(todayTrackings)
        }

    }

    catch (e: Exception) {
        Log.e("Google sign-in", "Error syncing data from Firestore", e)
    }
}
