package com.healthterra.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.healthterra.data.UserDatabase
import com.healthterra.helpers.calculateCaloriesGoal
import com.healthterra.helpers.calculateExerciseGoal
import com.healthterra.helpers.calculateStepsGoal
import com.healthterra.helpers.calculateWaterGoal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

suspend fun syncAllTrackingsToFirestore(userDatabase: UserDatabase) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()

    val dailyTrackingsList = userDatabase.dailyTrackingsDao().getDailyTrackings().first()
    val userTodayTrackings = userDatabase.todayTrackingsDao().getAll().first().firstOrNull()
    val userCharacteristics = userDatabase.characteristicsDao().getAll().first().firstOrNull()

    // Pair of Date, (Category, Progress)
    val allTrackings = mutableListOf<Pair<String, Map<String, Any>>>()

    // Adds all daily trackings
    for (tracking in dailyTrackingsList) {
        allTrackings.add(
            tracking.date to mapOf(
                "waterProgress" to tracking.waterProgress,
                "caloriesProgress" to tracking.caloriesProgress,
                "exerciseProgress" to tracking.exerciseProgress,
                "stepsProgress" to tracking.stepsProgress,
                "waterGoal" to tracking.waterGoal,
                "caloriesGoal" to tracking.caloriesGoal,
                "exerciseGoal" to tracking.exerciseGoal,
                "stepsGoal" to tracking.stepsGoal
            )
        )
    }

    // Adds today trackings
    if (userTodayTrackings != null && userCharacteristics != null) {
        allTrackings.add(
            LocalDate.now().toString() to mapOf(
                "waterProgress" to userTodayTrackings.waterProgress.sum(),
                "caloriesProgress" to userTodayTrackings.caloriesProgress.sum(),
                "exerciseProgress" to userTodayTrackings.exerciseProgress.sum(),
                "stepsProgress" to userTodayTrackings.stepsProgress,
                "waterGoal" to calculateWaterGoal(userCharacteristics),
                "caloriesGoal" to calculateCaloriesGoal(userCharacteristics),
                "exerciseGoal" to calculateExerciseGoal(userCharacteristics),
                "stepsGoal" to calculateStepsGoal(userCharacteristics)
            )
        )
    }

    // Chunks of 500 documents to respect Firestore batch limits
    val batches = allTrackings.chunked(500)

    for (batch in batches) {
        val writeBatch = db.batch()
        for ((date, data) in batch) {
            val docRef = db.collection("users").document(uid).collection("daily_trackings").document(date)
            writeBatch.set(docRef, data, SetOptions.merge())
        }

        writeBatch.commit().await()
    }
}
