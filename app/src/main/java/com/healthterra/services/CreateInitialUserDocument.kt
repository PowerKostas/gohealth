package com.healthterra.services

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.time.LocalDate

fun createInitialUserDocument(uid: String, username: String, profilePictureString: String) {
    val initialUserData = mapOf(
        "activityLevel" to null,
        "age" to null,
        "appearance" to "Light",
        "daysGoal" to 0,
        "gender" to null,
        "height" to null,
        "initialWeightGoalDate" to null,
        "kgGoal" to 0,
        "lastSavedDate" to LocalDate.now().toString(),
        "profilePictureString" to profilePictureString,
        "stepTracking" to "Enabled",
        "username" to username,
        "weight" to null,
        "weightGoal" to "Maintain",
        "leaderboardsVisibility" to "Anonymous",
        "notifiedAchievements" to "",

        "achievements" to mapOf(
            "appearWaterLeaderboards" to false,
            "appearCaloriesLeaderboards" to false,
            "appearExerciseLeaderboards" to false,
            "appearStepsLeaderboards" to false,
            "appearTotalStepsLeaderboards" to false,
            "appearHealthiestUser" to false,
            "secret" to false
        )
    )

    FirebaseFirestore.getInstance()
        .collection("users")
        .document(uid)
        .set(initialUserData, SetOptions.merge())
}
