package com.healthterra.helpers

import com.healthterra.data.entities.DailyTrackings
import com.healthterra.data.entities.TodayTrackings

data class AllTimeTrackings(
    val waterProgress: Int,
    val caloriesProgress: Int,
    val exerciseProgress: Int,
    val totalSteps: Int,
    val caloriesBurned: Int
)

fun calculateAllTimeTrackings(dailyTrackingsList: List<DailyTrackings>, todayTrackings: TodayTrackings?): AllTimeTrackings {
    val historyWaterProgress = dailyTrackingsList.sumOf { it.waterProgress }
    val historyCaloriesProgress = dailyTrackingsList.sumOf { it.caloriesProgress }
    val historyCaloriesBurned = dailyTrackingsList.sumOf { it.caloriesBurned }
    val historyExerciseProgress = dailyTrackingsList.sumOf { it.exerciseProgress }
    val historySteps = dailyTrackingsList.sumOf { it.stepsProgress }

    val todayWaterProgress = todayTrackings?.waterProgress?.sum() ?: 0
    val todayCaloriesProgress = todayTrackings?.caloriesProgress?.sum() ?: 0
    val todayCaloriesBurned = todayTrackings?.caloriesBurned ?: 0
    val todayExerciseProgress = todayTrackings?.exerciseProgress?.sum() ?: 0
    val todaySteps = todayTrackings?.stepsProgress ?: 0

    // Combine and return
    return AllTimeTrackings(
        waterProgress = historyWaterProgress + todayWaterProgress,
        caloriesProgress = historyCaloriesProgress + todayCaloriesProgress,
        exerciseProgress = historyExerciseProgress + todayExerciseProgress,
        totalSteps = historySteps + todaySteps,
        caloriesBurned = historyCaloriesBurned + todayCaloriesBurned
    )
}
