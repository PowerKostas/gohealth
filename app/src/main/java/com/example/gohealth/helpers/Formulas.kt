package com.example.gohealth.helpers

import com.example.gohealth.data.entities.Characteristics
import kotlin.math.roundToInt

fun calculateWaterGoal(userCharacteristics: Characteristics?): Int {
    val weight = userCharacteristics?.weight ?: 70f

    val genderValues = mapOf(
        "Male" to 35f,
        "Female" to 31f
    ).withDefault { 33f }

    val genderValue = genderValues.getValue(userCharacteristics?.gender ?: "")

    val activityLevelValues = mapOf(
        "Sedentary" to 1.0f,
        "Moderate" to 1.2f,
        "High" to 1.4f
    ).withDefault { 1.2f }

    val activityLevelValue = activityLevelValues.getValue(userCharacteristics?.activityLevel ?: "")

    return ((weight * genderValue) * activityLevelValue).roundToInt()
}


fun calculateCaloriesGoal(userCharacteristics: Characteristics?): Int {
    val weight = userCharacteristics?.weight ?: 70f
    val height = userCharacteristics?.height ?: 170f
    val age = userCharacteristics?.age ?: 30f

    val genderValues = mapOf(
        "Male" to 5f,
        "Female" to -161f
    ).withDefault { -78f }

    val genderValue = genderValues.getValue(userCharacteristics?.gender ?: "")

    val activityLevelValues = mapOf(
        "Sedentary" to 1.2f,
        "Moderate" to 1.55f,
        "High" to 1.725f
    ).withDefault { 1.55f }

    val activityLevelValue = activityLevelValues.getValue(userCharacteristics?.activityLevel ?: "")

    val weightGoalValues = mapOf(
        "Lose" to -500f,
        "Maintain" to 0f,
        "Gain" to 500f
    ).withDefault { 0f }

    val weightGoalValue = weightGoalValues.getValue(userCharacteristics?.weightGoal ?: "")

    val BMR = (10f * weight) + (6.25f * height) - (5f * age) + genderValue
    return ((BMR * activityLevelValue) + weightGoalValue).roundToInt()
}


fun calculatePushUpsGoal(userCharacteristics: Characteristics?): Int {
    val weight = userCharacteristics?.weight ?: 70f
    val height = userCharacteristics?.height ?: 170f

    val BMI = weight / ((height / 100) * (height / 100))

    val BMIbasedPushUps = when {
        BMI < 25 -> 40
        BMI < 30 -> 30
        else -> 20
    }

    val activityLevelValues = mapOf(
        "Sedentary" to 1.0f,
        "Moderate" to 1.5f,
        "High" to 2f
    ).withDefault { 1.5f }

    val activityLevelValue = activityLevelValues.getValue(userCharacteristics?.activityLevel ?: "")

    val age = userCharacteristics?.age ?: 30f

    val ageValue = when {
        age < 30 -> 2.0f
        age < 50 -> 1.5f
        else -> 1.0f
    }

    val genderValues = mapOf(
        "Male" to 1.0f,
        "Female" to 0.65f
    ).withDefault { 0.825f }

    val genderValue = genderValues.getValue(userCharacteristics?.gender ?: "")

    return (BMIbasedPushUps * activityLevelValue * ageValue * genderValue).roundToInt()
}


fun calculateStepsGoal(userCharacteristics: Characteristics?): Int {
    val activityLevelValues = mapOf(
        "Sedentary" to 6000,
        "Moderate" to 8000,
        "High" to 11000
    ).withDefault { 8000 }

    val activityLevelBasedSteps = activityLevelValues.getValue(userCharacteristics?.activityLevel ?: "")

    val weightGoalValues = mapOf(
        "Lose" to 2000,
        "Maintain" to 0,
        "Gain" to 0
    ).withDefault { 0 }

    val weightGoalValue = weightGoalValues.getValue(userCharacteristics?.weightGoal ?: "")

    val age = userCharacteristics?.age ?: 30f

    val ageValue = when {
        age < 30 -> 1000
        age < 50 -> 0
        else -> -1000
    }

    return activityLevelBasedSteps + weightGoalValue + ageValue
}
