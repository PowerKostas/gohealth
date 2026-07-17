package com.healthterra.helpers

import com.healthterra.data.entities.Characteristics
import kotlin.math.roundToInt

fun calculateWaterGoal(userCharacteristics: Characteristics?): Int {
    val weight = userCharacteristics?.weight ?: 70f

    val genderValue = when (userCharacteristics?.gender) {
        "Male" -> 35
        "Female" -> 31
        else -> 33
    }

    val activityLevelValue = when (userCharacteristics?.activityLevel) {
        "Sedentary" -> 1.0f
        "Moderate" -> 1.2f
        "High" -> 1.4f
        else -> 1.2f
    }

    val waterGoal = ((weight * genderValue) * activityLevelValue).roundToInt()
    return roundValue(waterGoal)
}


fun calculateCaloriesGoal(userCharacteristics: Characteristics?): Int {
    val weight = userCharacteristics?.weight ?: 70f
    val height = userCharacteristics?.height ?: 170f
    val age = userCharacteristics?.age ?: 30f
    val kgGoal = userCharacteristics?.kgGoal ?: 0
    val daysGoal = userCharacteristics?.daysGoal ?: 0

    val genderValue = when (userCharacteristics?.gender) {
        "Male" -> 5
        "Female" -> -161
        else -> -78
    }

    val activityLevelValue = when (userCharacteristics?.activityLevel) {
        "Sedentary" -> 1.2f
        "Moderate" -> 1.55f
        "High" -> 1.725f
        else -> 1.55f
    }

    val bmr = (10f * weight) + (6.25f * height) - (5f * age) + genderValue
    val tdee = bmr * activityLevelValue

    if (kgGoal != 0 && daysGoal != 0) { // If the user has set a calories lose/gain goal
        val maxDailyCaloriesDeficit = -(tdee * 0.25f)
        val maxDailyCaloriesSurplus = tdee * 0.20f

        // You need to eat 7700 less/more kcal to lose/gain 1kg, divide that number by the timeframe the user selected and get the daily
        // calories adjustment
        val caloriesChange = kgGoal * 7700
        val dailyCaloriesAdjustment = (caloriesChange / daysGoal.toFloat()).coerceIn(maxDailyCaloriesDeficit, maxDailyCaloriesSurplus)

        return (tdee + dailyCaloriesAdjustment).roundToInt()
    }

    else {
        return roundValue(tdee.roundToInt())
    }
}


fun calculateExerciseGoal(userCharacteristics: Characteristics?): Int {
    val weight = userCharacteristics?.weight ?: 70f
    val height = userCharacteristics?.height ?: 170f

    val bmi = weight / ((height / 100) * (height / 100))

    val bmiBasedReps = when {
        bmi < 25 -> 40
        bmi < 30 -> 30
        else -> 20
    }

    val age = userCharacteristics?.age ?: 30f

    val ageValue = when {
        age < 30 -> 2.0f
        age < 50 -> 1.5f
        else -> 1.0f
    }

    val genderValue = when (userCharacteristics?.gender) {
        "Male" -> 1.0f
        "Female" -> 0.65f
        else -> 0.825f
    }

    val activityLevelValue = when (userCharacteristics?.activityLevel) {
        "Sedentary" -> 1.0f
        "Moderate" -> 1.5f
        "High" -> 2f
        else -> 1.5f
    }

    val weightGoalValue = when (userCharacteristics?.weightGoal) {
        "Lose" -> 1.2f
        "Maintain" -> 1.0f
        "Gain" -> 1.0f
        else -> 1.0f
    }

    val repsGoal = (bmiBasedReps * activityLevelValue * ageValue * genderValue * weightGoalValue).roundToInt()
    return roundValue(repsGoal)
}


fun calculateStepsGoal(userCharacteristics: Characteristics?): Int {
    val activityLevelBasedSteps = when (userCharacteristics?.activityLevel) {
        "Sedentary" -> 6000
        "Moderate" -> 8000
        "High" -> 11000
        else -> 8000
    }

    val weightGoalValue = when (userCharacteristics?.weightGoal) {
        "Lose" -> 2000
        "Maintain" -> 0
        "Gain" -> 0
        else -> 0
    }

    val age = userCharacteristics?.age ?: 30f

    val ageValue = when {
        age < 30 -> 1000
        age < 50 -> 0
        else -> -1000
    }

    return activityLevelBasedSteps + weightGoalValue + ageValue
}


fun calculateCaloriesBurned(userCharacteristics: Characteristics?, reps: Int, steps: Int): Int {
    val weight = userCharacteristics?.weight ?: 70f
    val height = userCharacteristics?.height ?: 170f

    val genderModifier = when (userCharacteristics?.gender) {
        "Male" -> 1.05f
        "Female" -> 0.95f
        else -> 1f
    }

    val strideLengthMeters = (height * 0.415f) / 100f
    val distanceKm = (steps * strideLengthMeters) / 1000f
    val caloriesBurnedFromSteps = (distanceKm * weight * 0.735f) * genderModifier

    val exerciseMET = 6.0f
    val secondsPerRep = 3f
    val exerciseDurationMinutes = (reps * secondsPerRep) / 60f
    val caloriesBurnedFromExercise = (exerciseMET * weight * 0.0175f * exerciseDurationMinutes) * genderModifier

    return (caloriesBurnedFromSteps + caloriesBurnedFromExercise).roundToInt()
}
