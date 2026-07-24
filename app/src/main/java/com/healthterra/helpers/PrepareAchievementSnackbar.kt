package com.healthterra.helpers

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import com.healthterra.R
import kotlinx.coroutines.launch

// Custom Snackbar to show an ImageVector, a label, a title and a description
class CustomSnackbarData(
    val icon: ImageVector,
    val label: String,
    val title: String,
    val description: String,
    val backgroundBrush: Brush,
    val sound: Int? = null
) : SnackbarVisuals {
    override val message: String = title
    override val actionLabel: String? = null
    override val withDismissAction: Boolean = false
    override val duration: SnackbarDuration = SnackbarDuration.Short
}

// To avoid all the existing user's achievements popping up, if a user signed in on an existing account, the function resets
@Composable
fun PrepareAchievementSnackbar(achievementsData: TieredAchievements, snackbarHostState: SnackbarHostState, notifiedAchievements: String, pendingSyncFirestoreUserToRoom: Boolean, onUpdateNotifiedAchievements: (String) -> Unit) {
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(achievementsData, notifiedAchievements, pendingSyncFirestoreUserToRoom) {
        if (pendingSyncFirestoreUserToRoom) return@LaunchedEffect

        val flatList = achievementsData.flatList
        if (flatList.isEmpty()) return@LaunchedEffect

        val achievementsToNotify = mutableListOf<AchievementData>()
        val notifiedAchievementsSet = notifiedAchievements.split(",").filter { it.isNotEmpty() }.toMutableSet()
        var notifiedAchievementsChanged = false

        // If the current achievement is unlocked and the user hasn't gotten a Snackbar for it yet, it's added to the achievementsToNotify
        // list, if an achievement was lost, it's removed from memory
        for (achievement in flatList) {
            val isNotified = notifiedAchievementsSet.contains(achievement.title)

            if (achievement.isUnlocked && !isNotified) {
                achievementsToNotify.add(achievement)
                notifiedAchievementsSet.add(achievement.title)
                notifiedAchievementsChanged = true
            }

            else if (!achievement.isUnlocked && isNotified) {
                notifiedAchievementsSet.remove(achievement.title)
                notifiedAchievementsChanged = true
            }
        }

        // Updates notifiedAchievements, if needed
        if (notifiedAchievementsChanged) {
            val updatedString = notifiedAchievementsSet.joinToString(",")
            onUpdateNotifiedAchievements(updatedString)
        }

        achievementsToNotify.forEach { achievement ->
            val backgroundBrush = when (achievement.tier) {
                "Common" -> SolidColor(Color(0xFF2883fe))
                "Rare" -> SolidColor(Color(0xFFd97f40))
                "Epic" -> SolidColor(Color(0xFFbe51f1))
                "Legendary" -> Brush.linearGradient(colors = listOf(Color(0xFF89ef82), Color(0xFFedc8a0), Color(0xFFdb449e)))
                else -> SolidColor(Color(0xFFf5a302))
            }

            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    CustomSnackbarData(
                        icon = achievement.icon,
                        label = "🎖️ Achievement Unlocked!",
                        title = achievement.title,
                        description = achievement.description,
                        backgroundBrush = backgroundBrush,
                        sound = R.raw.achievement
                    )
                )
            }
        }
    }
}
