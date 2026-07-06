package com.healthterra.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.FollowTheSigns
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Blind
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.HotelClass
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.NaturePeople
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NordicWalking
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shower
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthterra.R
import com.healthterra.ui.components.screen.AchievementItem
import com.healthterra.ui.viewModels.AchievementsViewModel
import com.healthterra.ui.viewModels.SettingsViewModel

@Composable
fun AchievementsScreen() {
    val achievementsViewModel = viewModel<AchievementsViewModel>(factory = AchievementsViewModel.Factory)
    val otherAchievementsState by achievementsViewModel.otherAchievements.collectAsState()
    val otherAchievements = otherAchievementsState ?: return

    val leaderboardsAchievementsList by achievementsViewModel.leaderboardsAchievements.collectAsState()
    val leaderboardsAchievements = leaderboardsAchievementsList.firstOrNull() ?: return

    val settingsViewModel = viewModel<SettingsViewModel>(factory = SettingsViewModel.Factory)
    val userSettingsList by settingsViewModel.settings.collectAsState()
    val userSettings = userSettingsList.firstOrNull() ?: return

    val achievementsStatus = remember(otherAchievements, leaderboardsAchievements) {
        listOf(
            // Common
            otherAchievements.totalWaterGoals >= 10, otherAchievements.totalCaloriesGoals >= 10, otherAchievements.totalExerciseGoals >= 10,
            otherAchievements.totalStepsGoals >= 10, otherAchievements.totalSteps >= 100000, otherAchievements.maxWaterStreak >= 5,
            otherAchievements.maxCaloriesStreak >= 5, otherAchievements.maxExerciseStreak >= 5, otherAchievements.maxStepsStreak >= 5,

            // Rare
            otherAchievements.totalWaterGoals >= 100, otherAchievements.totalCaloriesGoals >= 100, otherAchievements.totalExerciseGoals >= 100,
            otherAchievements.totalStepsGoals >= 100, otherAchievements.totalSteps >= 1000000, otherAchievements.maxWaterStreak >= 50,
            otherAchievements.maxCaloriesStreak >= 50, otherAchievements.maxExerciseStreak >= 50, otherAchievements.maxStepsStreak >= 50,

            // Epic
            otherAchievements.totalWaterGoals >= 365, otherAchievements.totalCaloriesGoals >= 365, otherAchievements.totalExerciseGoals >= 365,
            otherAchievements.totalStepsGoals >= 365, otherAchievements.totalSteps >= 5000000, otherAchievements.maxWaterStreak >= 100,
            otherAchievements.maxCaloriesStreak >= 100, otherAchievements.maxExerciseStreak >= 100, otherAchievements.maxStepsStreak >= 100,

            // Legendary
            leaderboardsAchievements.appearWaterLeaderboards, leaderboardsAchievements.appearCaloriesLeaderboards,
            leaderboardsAchievements.appearExerciseLeaderboards, leaderboardsAchievements.appearStepsLeaderboards,
            leaderboardsAchievements.appearTotalStepsLeaderboards, leaderboardsAchievements.secret, userSettings.username == "TheWalkingDemi®",

            // Impossible
            otherAchievements.totalWaterGoals >= 1000, otherAchievements.totalCaloriesGoals >= 1000, otherAchievements.totalExerciseGoals >= 1000,
            otherAchievements.totalStepsGoals >= 1000, otherAchievements.totalSteps >= 10000000, otherAchievements.maxWaterStreak >= 1000,
            otherAchievements.maxCaloriesStreak >= 1000, otherAchievements.maxExerciseStreak >= 1000, otherAchievements.maxStepsStreak >= 1000
        )
    }

    // Early Playtester achievement only shows up if it's true
    val isEarlyPlaytesterVisible = achievementsStatus[36]
    val totalAchievements = if (isEarlyPlaytesterVisible) 43 else 42
    val totalLegendary = if (isEarlyPlaytesterVisible) 7 else 6

    // Because normal Arrangement.SpaceBetween doesn't offer minimum spacing between items, a custom one is created. A 12dp horizontal padding
    // between items is only applied if everything fits on the screen, otherwise SpaceBetween is applied
    val spaceBetweenWithMinSpacing = object : Arrangement.Horizontal {
        override val spacing = 12.dp

        override fun Density.arrange(
            totalSize: Int,
            sizes: IntArray,
            layoutDirection: LayoutDirection,
            outPositions: IntArray
        ) {
            with(Arrangement.SpaceBetween) {
                arrange(totalSize, sizes, layoutDirection, outPositions)
            }
        }
    }


    // Uses a box with constraints to vertically space around the achievements in the scrollable column
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val minScrollHeight = maxHeight

        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = minScrollHeight)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "Collected: ${achievementsStatus.count { it }} / $totalAchievements",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(32.dp)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface)
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Common: ${achievementsStatus.subList(0, 9).count { it }} / 9",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = spaceBetweenWithMinSpacing
                ) {
                    item { AchievementItem(Icons.Filled.Shower, "Sipper", "Complete 10 Water Goals", "Common", achievementsStatus[0], otherAchievements.totalWaterGoals, 10) }
                    item { AchievementItem(Icons.Filled.LocalDining, "Mindful Eater", "Complete 10 Calorie Goals", "Common", achievementsStatus[1], otherAchievements.totalCaloriesGoals, 10) }
                    item { AchievementItem(Icons.AutoMirrored.Filled.DirectionsBike, "The Active One", "Complete 10 Exercise Goals", "Common", achievementsStatus[2], otherAchievements.totalExerciseGoals, 10) }
                    item { AchievementItem(Icons.AutoMirrored.Filled.DirectionsRun, "Foot Soldier", "Complete 10 Step Goals", "Common", achievementsStatus[3], otherAchievements.totalStepsGoals, 10) }
                    item { AchievementItem(Icons.Filled.Explore, "Wanderer", "Walk 100000 Steps", "Common", achievementsStatus[4], otherAchievements.totalSteps, 100000) }
                    item { AchievementItem(Icons.Filled.LocalFlorist, "Well Watered", "Maintain a 5-Day Water Goal Streak", "Common", achievementsStatus[5], otherAchievements.maxWaterStreak, 5) }
                    item { AchievementItem(Icons.Filled.Balance, "Dietitian", "Maintain a 5-Day Calorie Goal Streak", "Common", achievementsStatus[6], otherAchievements.maxCaloriesStreak, 5) }
                    item { AchievementItem(Icons.Filled.WbSunny, "Early Riser", "Maintain a 5-Day Exercise Goal Streak", "Common", achievementsStatus[7], otherAchievements.maxExerciseStreak, 5) }
                    item { AchievementItem(Icons.Filled.NaturePeople, "Road Warrior", "Maintain a 5-Day Step Goal Streak", "Common", achievementsStatus[8], otherAchievements.maxStepsStreak, 5) }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Rare: ${achievementsStatus.subList(9, 18).count { it }} / 9",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = spaceBetweenWithMinSpacing
                ) {
                    item { AchievementItem(Icons.Filled.LocalCafe, "Water Regular", "Complete 100 Water Goals", "Rare", achievementsStatus[9], otherAchievements.totalWaterGoals, 100) }
                    item { AchievementItem(Icons.Filled.SoupKitchen, "Nutritionist", "Complete 100 Calorie Goals", "Rare", achievementsStatus[10], otherAchievements.totalCaloriesGoals, 100) }
                    item { AchievementItem(Icons.Filled.SportsTennis, "Athlete", "Complete 100 Exercise Goals", "Rare", achievementsStatus[11], otherAchievements.totalExerciseGoals, 100) }
                    item { AchievementItem(Icons.Filled.Blind, "Pathfinder", "Complete 100 Step Goals", "Rare", achievementsStatus[12], otherAchievements.totalStepsGoals, 100) }
                    item { AchievementItem(Icons.Filled.Navigation, "Voyager", "Walk 1000000 Steps", "Rare", achievementsStatus[13], otherAchievements.totalSteps, 1000000) }
                    item { AchievementItem(Icons.Filled.Spa, "Oasis", "Maintain a 50-Day Water Goal Streak", "Rare", achievementsStatus[14], otherAchievements.maxWaterStreak, 50) }
                    item { AchievementItem(Icons.Filled.Whatshot, "Metabolizer", "Maintain a 50-Day Calorie Goal Streak", "Rare", achievementsStatus[15], otherAchievements.maxCaloriesStreak, 50) }
                    item { AchievementItem(Icons.Filled.MonitorHeart, "Hard-Wired", "Maintain a 50-Day Exercise Goal Streak", "Rare", achievementsStatus[16], otherAchievements.maxExerciseStreak, 50) }
                    item { AchievementItem(Icons.AutoMirrored.Filled.FollowTheSigns, "Marathoner", "Maintain a 50-Day Step Goal Streak", "Rare", achievementsStatus[17], otherAchievements.maxStepsStreak, 50) }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Epic: ${achievementsStatus.subList(18, 27).count { it }} / 9",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = spaceBetweenWithMinSpacing
                ) {
                    item { AchievementItem(Icons.Filled.Waves, "Reservoir", "Complete 365 Water Goals", "Epic", achievementsStatus[18], otherAchievements.totalWaterGoals, 365) }
                    item { AchievementItem(Icons.Filled.OutdoorGrill, "Master Chef", "Complete 365 Calorie Goals", "Epic", achievementsStatus[19], otherAchievements.totalCaloriesGoals, 365) }
                    item { AchievementItem(Icons.Filled.Power, "Power House", "Complete 365 Exercise Goals", "Epic", achievementsStatus[20], otherAchievements.totalExerciseGoals, 365) }
                    item { AchievementItem(Icons.Filled.NordicWalking, "Trailblazer", "Complete 365 Step Goals", "Epic", achievementsStatus[21], otherAchievements.totalStepsGoals, 365) }
                    item { AchievementItem(Icons.Filled.Language, "Globetrotter", "Walk 5000000 Steps", "Epic", achievementsStatus[22], otherAchievements.totalSteps, 5000000) }
                    item { AchievementItem(Icons.Filled.Pool, "Tidal Force", "Maintain a 100-Day Water Goal Streak", "Epic", achievementsStatus[23], otherAchievements.maxWaterStreak, 100) }
                    item { AchievementItem(Icons.Filled.LocalFireDepartment, "Metabolic Monster", "Maintain a 100-Day Calorie Goal Streak", "Epic", achievementsStatus[24], otherAchievements.maxCaloriesStreak, 100) }
                    item { AchievementItem(Icons.Filled.Security, "Ironclad", "Maintain a 100-Day Exercise Goal Streak", "Epic", achievementsStatus[25], otherAchievements.maxExerciseStreak, 100) }
                    item { AchievementItem(Icons.Filled.Landscape, "Scout", "Maintain a 100-Day Step Goal Streak", "Epic", achievementsStatus[26], otherAchievements.maxStepsStreak, 100) }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Legendary: ${achievementsStatus.subList(27, 34).count { it }} / $totalLegendary",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = spaceBetweenWithMinSpacing
                ) {
                    item { AchievementItem(Icons.Filled.Anchor, "Hall of Famer", "Appear on the Water Leaderboards", "Legendary", achievementsStatus[27], if (achievementsStatus[27]) 1 else 0, 1) }
                    item { AchievementItem(Icons.Filled.LocationCity, "Household Name", "Appear on the Calories Leaderboards", "Legendary", achievementsStatus[28], if (achievementsStatus[28]) 1 else 0, 1) }
                    item { AchievementItem(Icons.Filled.HotelClass, "Superstar", "Appear on the Exercise Leaderboards", "Legendary", achievementsStatus[29], if (achievementsStatus[29]) 1 else 0, 1) }
                    item { AchievementItem(Icons.Filled.WorkspacePremium, "Record Holder", "Appear on the Steps Leaderboards", "Legendary", achievementsStatus[30], if (achievementsStatus[30]) 1 else 0, 1) }
                    item { AchievementItem(Icons.Filled.Public, "Healthterra's Finest", "Become the \"Healthiest User\"", "Legendary", achievementsStatus[31], if (achievementsStatus[31]) 1 else 0, 1) }
                    item { AchievementItem(Icons.Filled.QuestionMark, "Secret", if (achievementsStatus[32]) "Take the top spot on every single leaderboard. You might be the only person to ever achieve this!" else "???", "Legendary", achievementsStatus[32], if (achievementsStatus[32]) 1 else 0, 1) }

                    if (isEarlyPlaytesterVisible) {
                        item { AchievementItem(Icons.Filled.Science, "Early Playtester", "We couldn't have done it without you!", "Legendary", achievementsStatus[33], if (achievementsStatus[33]) 1 else 0, 1) }
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Impossible: ${achievementsStatus.subList(34, 43).count { it }} / 9",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = spaceBetweenWithMinSpacing
                ) {
                    item { AchievementItem(ImageVector.vectorResource(id = R.drawable.leviathan), "Leviathan", "Complete 1000 Water Goals", "Impossible", achievementsStatus[34], otherAchievements.totalWaterGoals, 1000) }
                    item { AchievementItem(Icons.AutoMirrored.Filled.MenuBook, "Alchemist", "Complete 1000 Calorie Goals", "Impossible", achievementsStatus[35], otherAchievements.totalCaloriesGoals, 1000) }
                    item { AchievementItem(ImageVector.vectorResource(id = R.drawable.juggernaut), "Juggernaut", "Complete 1000 Exercise Goals", "Impossible", achievementsStatus[36], otherAchievements.totalExerciseGoals, 1000) }
                    item { AchievementItem(ImageVector.vectorResource(id = R.drawable.griffin), "Griffin", "Complete 1000 Step Goals", "Impossible", achievementsStatus[37], otherAchievements.totalStepsGoals, 1000) }
                    item { AchievementItem(ImageVector.vectorResource(id = R.drawable.phoenix), "Phoenix", "Walk 10000000 Steps", "Impossible", achievementsStatus[38], otherAchievements.totalSteps, 10000000) }
                    item { AchievementItem(ImageVector.vectorResource(id = R.drawable.hydra), "Hydra", "Maintain a 365-Day Water Goal Streak", "Impossible", achievementsStatus[39], otherAchievements.maxWaterStreak, 365) }
                    item { AchievementItem(ImageVector.vectorResource(id = R.drawable.minotaur), "Minotaur", "Maintain a 365-Day Calorie Goal Streak", "Impossible", achievementsStatus[40], otherAchievements.maxCaloriesStreak, 365) }
                    item { AchievementItem(ImageVector.vectorResource(id = R.drawable.golem), "Golem", "Maintain a 365-Day Exercise Goal Streak", "Impossible", achievementsStatus[41], otherAchievements.maxExerciseStreak, 365) }
                    item { AchievementItem(ImageVector.vectorResource(id = R.drawable.unicorn), "Unicorn", "Maintain a 365-Day Step Goal Streak", "Impossible", achievementsStatus[42], otherAchievements.maxStepsStreak, 365) }
                }
            }
        }
    }
}
