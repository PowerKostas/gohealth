package com.kostas.gohealth.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.kostas.gohealth.R
import com.kostas.gohealth.data.documents.LeaderboardEntry
import com.kostas.gohealth.ui.components.screen.LeaderboardBox

@Composable
// Sets up listeners to the Firestore database to continuously fetch the single highest-scoring user and his details for
// the water, calories, push-ups, and steps goals, doesn't work on the background, only when the screen is active
fun getTopCategoryUser(category: String): State<LeaderboardEntry?> {
    return produceState(initialValue = null, key1 = category) {
        val db = Firebase.firestore

        val listenerRegistration = db.collection("leaderboards")
            .orderBy(category, Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val topUserDoc = snapshot.documents[0]
                    value = LeaderboardEntry(
                        userId = topUserDoc.id,
                        username = topUserDoc.getString("username") ?: "",
                        profilePictureString = topUserDoc.getString("profilePictureString") ?: "",
                        waterGoalsCompleted = topUserDoc.getLong("waterGoalsCompleted") ?: 0L,
                        caloriesGoalsCompleted = topUserDoc.getLong("caloriesGoalsCompleted") ?: 0L,
                        pushUpsGoalsCompleted = topUserDoc.getLong("pushUpsGoalsCompleted") ?: 0L,
                        totalSteps = topUserDoc.getLong("totalSteps") ?: 0L
                    )
                }
            }

        awaitDispose {
            listenerRegistration.remove()
        }
    }
}

// Same job as getTopCategoryUser, but gets the actual user's data and not the top user's data for a specific category
@Composable
fun getUserListener(userId: String?): State<LeaderboardEntry?> {
    return produceState(initialValue = null, key1 = userId) {
        if (userId == null) {
            value = null
            return@produceState
        }

        val db = Firebase.firestore

        val listenerRegistration = db.collection("leaderboards")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    value = LeaderboardEntry(
                        userId = snapshot.id,
                        username = snapshot.getString("username") ?: "",
                        profilePictureString = snapshot.getString("profilePictureString") ?: "",
                        waterGoalsCompleted = snapshot.getLong("waterGoalsCompleted") ?: 0L,
                        caloriesGoalsCompleted = snapshot.getLong("caloriesGoalsCompleted") ?: 0L,
                        pushUpsGoalsCompleted = snapshot.getLong("pushUpsGoalsCompleted") ?: 0L,
                        totalSteps = snapshot.getLong("totalSteps") ?: 0L
                    )
                }
            }

        awaitDispose {
            listenerRegistration.remove()
        }
    }
}


@Composable
fun LeaderboardsScreen() {
    val topWaterUser by getTopCategoryUser("waterGoalsCompleted")
    val topCaloriesUser by getTopCategoryUser("caloriesGoalsCompleted")
    val topPushUpsUser by getTopCategoryUser("pushUpsGoalsCompleted")
    val topStepsUser by getTopCategoryUser("totalSteps")

    val currentUserId = Firebase.auth.currentUser?.uid
    val currentUser by getUserListener(currentUserId)

    val avatarMap = mapOf(
        "bear" to R.drawable.bear,
        "cat" to R.drawable.cat,
        "dinosaur" to R.drawable.dinosaur,
        "dog" to R.drawable.dog,
        "dolphin" to R.drawable.dolphin,
        "duck" to R.drawable.duck,
        "eagle" to R.drawable.eagle,
        "elephant" to R.drawable.elephant,
        "horse" to R.drawable.horse,
        "lion" to R.drawable.lion,
        "penguin" to R.drawable.penguin,
        "sheep" to R.drawable.sheep
    )

    if (topWaterUser != null && topCaloriesUser != null && topPushUpsUser != null && topStepsUser != null) { // Loading Screen
        // Draws the screen
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface)

        Column(
            verticalArrangement = Arrangement.spacedBy(40.dp, Alignment.CenterVertically),
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(
                    text = "Daily Goals Completed",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD4AF37)
                )

                topWaterUser?.let { user ->
                    // Checks if the current user is the top user, if he is, make his score null to know not to draw a specific component
                    val currentUserScore = if (currentUserId == user.userId) null else (currentUser?.waterGoalsCompleted?.toString() ?: "0")
                    LeaderboardBox(avatarMap.getValue(user.profilePictureString), user.username, R.drawable.water, "Water", user.waterGoalsCompleted.toString(), currentUserScore)
                }

                topCaloriesUser?.let { user ->
                    val currentUserScore = if (currentUserId == user.userId) null else (currentUser?.caloriesGoalsCompleted?.toString() ?: "0")
                    LeaderboardBox(avatarMap.getValue(user.profilePictureString), user.username, R.drawable.calories, "Calories", user.caloriesGoalsCompleted.toString(), currentUserScore)
                }

                topPushUpsUser?.let { user ->
                    val currentUserScore = if (currentUserId == user.userId) null else (currentUser?.pushUpsGoalsCompleted?.toString() ?: "0")
                    LeaderboardBox(avatarMap.getValue(user.profilePictureString), user.username, R.drawable.push_ups, "Push-ups", user.pushUpsGoalsCompleted.toString(), currentUserScore)
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(
                    text = "Total Steps",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD4AF37)
                )

                topStepsUser?.let { user ->
                    val currentUserScore = if (currentUserId == user.userId) null else (currentUser?.totalSteps?.toString() ?: "0")
                    LeaderboardBox(avatarMap.getValue(user.profilePictureString), user.username, R.drawable.steps, "Steps", user.totalSteps.toString(), currentUserScore)
                }
            }
        }
    }
}
