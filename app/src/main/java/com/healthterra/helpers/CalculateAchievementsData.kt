package com.healthterra.helpers

import com.healthterra.data.entities.Characteristics
import com.healthterra.data.entities.DailyTrackings
import com.healthterra.data.entities.TodayTrackings
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class AchievementsData(
    val totalWaterGoals: Int, val totalCaloriesGoals: Int, val totalExerciseGoals: Int, val totalStepsGoals: Int, val totalSteps: Int,
    val activeWaterStreak: Int, val activeCaloriesStreak: Int, val activeExerciseStreak: Int, val activeStepsStreak: Int,
    val maxWaterStreak: Int, val maxCaloriesStreak: Int, val maxExerciseStreak: Int, val maxStepsStreak: Int, val maxSteps: Int
)

// Goes through every row of DailyTrackings and finds the total completed category goals, the total steps, the active categories streaks, the
// max categories streaks and the max steps, also injects today's data to the above
fun calculateAchievementsData(dailyTrackingsList: List<DailyTrackings>, todayTrackings: TodayTrackings?, userCharacteristics: Characteristics): AchievementsData {
    var totalWaterGoals = 0; var totalCaloriesGoals = 0; var totalExerciseGoals = 0; var totalStepsGoals = 0; var totalSteps = 0
    var activeWaterStreak = 0; var activeCaloriesStreak = 0 ; var activeExerciseStreak = 0; var activeStepsStreak = 0
    var maxWaterStreak = 0; var maxCaloriesStreak = 0; var maxExerciseStreak = 0; var maxStepsStreak = 0; var maxSteps = 0
    var previousDate: LocalDate? = null

    for (day in dailyTrackingsList) {
        val date = LocalDate.parse(day.date)
        val isConsecutive = previousDate == null || ChronoUnit.DAYS.between(previousDate, date) == 1L

        if (day.waterProgress >= day.waterGoal) {
            activeWaterStreak = if (isConsecutive) activeWaterStreak + 1 else 1
            maxWaterStreak = maxOf(activeWaterStreak, maxWaterStreak)
            totalWaterGoals += 1
        }

        else {
            activeWaterStreak = 0
        }

        if (day.caloriesProgress >= day.caloriesGoal) {
            activeCaloriesStreak = if (isConsecutive) activeCaloriesStreak + 1 else 1
            maxCaloriesStreak = maxOf(activeCaloriesStreak, maxCaloriesStreak)
            totalCaloriesGoals += 1
        }

        else {
            activeCaloriesStreak = 0
        }

        if (day.exerciseProgress >= day.exerciseGoal) {
            activeExerciseStreak = if (isConsecutive) activeExerciseStreak + 1 else 1
            maxExerciseStreak = maxOf(activeExerciseStreak, maxExerciseStreak)
            totalExerciseGoals += 1
        }

        else {
            activeExerciseStreak = 0
        }

        if (day.stepsProgress >= day.stepsGoal) {
            activeStepsStreak = if (isConsecutive) activeStepsStreak + 1 else 1
            maxStepsStreak = maxOf(activeStepsStreak, maxStepsStreak)
            totalStepsGoals += 1
        }

        else {
            activeStepsStreak = 0
        }

        totalSteps += day.stepsProgress
        maxSteps = maxOf(maxSteps, day.stepsProgress)
        previousDate = date
    }

    // Factors in today's data
    val todayDate = LocalDate.now()
    val isConsecutiveWithToday = previousDate == null || ChronoUnit.DAYS.between(previousDate, todayDate) == 1L
    val isConsecutiveOrSame = isConsecutiveWithToday || (ChronoUnit.DAYS.between(previousDate, todayDate) <= 1L)

    fun processToday(isMetToday: Boolean, activeStreak: Int, maxStreak: Int): Pair<Int, Int> {
        var tempActiveStreak = activeStreak
        var tempMaxStreak = maxStreak

        if (isMetToday) {
            tempActiveStreak = if (isConsecutiveWithToday) tempActiveStreak + 1 else 1
            tempMaxStreak = maxOf(maxStreak, tempActiveStreak)
        }

        else if (!isConsecutiveOrSame) { // To not reset streak if the user hasn't yet hit his goal today
            tempActiveStreak = 0
        }

        return Pair(tempActiveStreak, tempMaxStreak)
    }

    val waterGoalToday = (todayTrackings?.waterProgress?.sum() ?: 0) >= calculateWaterGoal(userCharacteristics)
    val caloriesGoalToday = (todayTrackings?.caloriesProgress?.sum() ?: 0) >= calculateCaloriesGoal(userCharacteristics)
    val exerciseGoalToday = (todayTrackings?.exerciseProgress?.sum() ?: 0) >= calculateExerciseGoal(userCharacteristics)
    val stepsGoalToday = (todayTrackings?.stepsProgress ?: 0) >= calculateStepsGoal(userCharacteristics)

    val waterStreakFinal = processToday(waterGoalToday, activeWaterStreak, maxWaterStreak)
    val caloriesStreakFinal = processToday(caloriesGoalToday, activeCaloriesStreak, maxCaloriesStreak)
    val exerciseStreakFinal = processToday(exerciseGoalToday, activeExerciseStreak, maxExerciseStreak)
    val stepsStreakFinal = processToday(stepsGoalToday, activeStepsStreak, maxStepsStreak)

    return AchievementsData(
        totalWaterGoals + if (waterGoalToday) 1 else 0, totalCaloriesGoals + if (caloriesGoalToday) 1 else 0,
        totalExerciseGoals + if (exerciseGoalToday) 1 else 0, totalStepsGoals + if (stepsGoalToday) 1 else 0,
        totalSteps + (todayTrackings?.stepsProgress ?: 0), waterStreakFinal.first, caloriesStreakFinal.first, exerciseStreakFinal.first,
        stepsStreakFinal.first, waterStreakFinal.second, caloriesStreakFinal.second, exerciseStreakFinal.second, stepsStreakFinal.second,
        maxOf(maxSteps, todayTrackings?.stepsProgress ?: 0),
    )
}

