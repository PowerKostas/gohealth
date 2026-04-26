package com.kostas.gohealth.data.documents

// For the remote Firestore database
data class LeaderboardEntry(
    val userId: Long = 0L,
    val username: String = "",
    val profilePictureString: String = "",
    val waterGoalsCompleted: Long = 0L,
    val caloriesGoalsCompleted: Long = 0L,
    val pushUpsGoalsCompleted: Long = 0L,
    val totalSteps: Long = 0L
)
