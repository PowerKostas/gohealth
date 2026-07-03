package com.healthterra.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.filled.Moving
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
import androidx.compose.material.icons.filled.Snowshoeing
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.StackedLineChart
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthterra.R
import com.healthterra.ui.components.screen.AchievementItem
import com.healthterra.ui.viewModels.AchievementsViewModel
import com.healthterra.ui.viewModels.SettingsViewModel

@Composable
fun AchievementsScreen() {
    val achievementsViewModel = viewModel<AchievementsViewModel>(factory = AchievementsViewModel.Factory)
    val achievementsDataState by achievementsViewModel.achievementsData.collectAsState()
    val achievementsData = achievementsDataState ?: return

    val settingsViewModel = viewModel<SettingsViewModel>(factory = SettingsViewModel.Factory)
    val userSettingsList by settingsViewModel.settings.collectAsState()
    val userSettings = userSettingsList.firstOrNull() ?: return

    val achievementsStatus = remember(achievementsData) {
        listOf(
            // Common
            achievementsData.totalWaterGoals >= 10, achievementsData.totalCaloriesGoals >= 10, achievementsData.totalExerciseGoals >= 10,
            achievementsData.totalStepsGoals >= 10, achievementsData.totalSteps >= 100000, achievementsData.maxWaterStreak >= 5,
            achievementsData.maxCaloriesStreak >= 5, achievementsData.maxExerciseStreak >= 5, achievementsData.maxStepsStreak >= 5,
            achievementsData.maxSteps >= 10000,

            // Rare
            achievementsData.totalWaterGoals >= 100, achievementsData.totalCaloriesGoals >= 100, achievementsData.totalExerciseGoals >= 100,
            achievementsData.totalStepsGoals >= 100, achievementsData.totalSteps >= 1000000, achievementsData.maxWaterStreak >= 50,
            achievementsData.maxCaloriesStreak >= 50, achievementsData.maxExerciseStreak >= 50, achievementsData.maxStepsStreak >= 50,
            achievementsData.maxSteps >= 20000,

            // Epic
            achievementsData.totalWaterGoals >= 365, achievementsData.totalCaloriesGoals >= 365, achievementsData.totalExerciseGoals >= 365,
            achievementsData.totalStepsGoals >= 365, achievementsData.totalSteps >= 5000000, achievementsData.maxWaterStreak >= 100,
            achievementsData.maxCaloriesStreak >= 100, achievementsData.maxExerciseStreak >= 100, achievementsData.maxStepsStreak >= 100,
            achievementsData.maxSteps >= 50000,

            // Legendary
            false, false, false, false, false, false, userSettings.username == "TheWalkingDemi®",

            // Impossible
            achievementsData.totalWaterGoals >= 1000, achievementsData.totalCaloriesGoals >= 1000, achievementsData.totalExerciseGoals >= 1000,
            achievementsData.totalStepsGoals >= 1000, achievementsData.totalSteps >= 10000000, achievementsData.maxWaterStreak >= 1000,
            achievementsData.maxCaloriesStreak >= 1000, achievementsData.maxExerciseStreak >= 1000, achievementsData.maxStepsStreak >= 1000,
            achievementsData.maxSteps >= 100000
        )
    }

    // Early Playtester achievement only shows up if it's true
    val isEarlyPlaytesterVisible = achievementsStatus[36]
    val totalAchievements = if (isEarlyPlaytesterVisible) 47 else 46
    val totalLegendary = if (isEarlyPlaytesterVisible) 7 else 6

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
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
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Common: ${achievementsStatus.subList(0, 10).count { it }} / 10",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { AchievementItem(Icons.Filled.Shower, "Sipper", "10 Water Goals Completed", "Common", achievementsStatus[0], achievementsData.totalWaterGoals, 10) }
                    item { AchievementItem(Icons.Filled.LocalDining, "Mindful Eater", "10 Calorie Goals Completed", "Common", achievementsStatus[1], achievementsData.totalCaloriesGoals, 10) }
                    item { AchievementItem(Icons.AutoMirrored.Filled.DirectionsBike, "The Active One", "10 Exercise Goals Completed", "Common", achievementsStatus[2], achievementsData.totalExerciseGoals, 10) }
                    item { AchievementItem(Icons.AutoMirrored.Filled.DirectionsRun, "Foot Soldier", "10 Step Goals Completed", "Common", achievementsStatus[3], achievementsData.totalStepsGoals, 10) }
                    item { AchievementItem(Icons.Filled.Explore, "Wanderer", "100000 Steps Made", "Common", achievementsStatus[4], achievementsData.totalSteps, 100000) }
                    item { AchievementItem(Icons.Filled.LocalFlorist, "Well Watered", "5-Day Water Goal Streak", "Common", achievementsStatus[5], achievementsData.maxWaterStreak, 5) }
                    item { AchievementItem(Icons.Filled.Balance, "Dietitian", "5-Day Calorie Goal Streak", "Common", achievementsStatus[6], achievementsData.maxCaloriesStreak, 5) }
                    item { AchievementItem(Icons.Filled.WbSunny, "Early Riser", "5-Day Exercise Goal Streak", "Common", achievementsStatus[7], achievementsData.maxExerciseStreak, 5) }
                    item { AchievementItem(Icons.Filled.Moving, "Pacer", "5-Day Step Goal Streak", "Common", achievementsStatus[8], achievementsData.maxStepsStreak, 5) }
                    item { AchievementItem(Icons.AutoMirrored.Filled.FollowTheSigns, "Road Warrior", "10000 Steps in a Day", "Common", achievementsStatus[9], achievementsData.maxSteps, 10000) }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Rare: ${achievementsStatus.subList(10, 20).count { it }} / 10",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { AchievementItem(Icons.Filled.LocalCafe, "Water Regular", "100 Water Goals Completed", "Rare", achievementsStatus[10], achievementsData.totalWaterGoals, 100) }
                    item { AchievementItem(Icons.Filled.SoupKitchen, "Nutritionist", "100 Calorie Goals Completed", "Rare", achievementsStatus[11], achievementsData.totalCaloriesGoals, 100) }
                    item { AchievementItem(Icons.Filled.SportsTennis, "Athlete", "100 Exercise Goals Completed", "Rare", achievementsStatus[12], achievementsData.totalExerciseGoals, 100) }
                    item { AchievementItem(Icons.Filled.Blind, "Pathfinder", "100 Step Goals Completed", "Rare", achievementsStatus[13], achievementsData.totalStepsGoals, 100) }
                    item { AchievementItem(Icons.Filled.Navigation, "Voyager", "1000000 Steps Made", "Rare", achievementsStatus[14], achievementsData.totalSteps, 1000000) }
                    item { AchievementItem(Icons.Filled.Spa, "Oasis", "50-Day Water Goal Streak", "Rare", achievementsStatus[15], achievementsData.maxWaterStreak, 50) }
                    item { AchievementItem(Icons.Filled.Whatshot, "Metabolizer", "50-Day Calorie Goal Streak", "Rare", achievementsStatus[16], achievementsData.maxCaloriesStreak, 50) }
                    item { AchievementItem(Icons.Filled.MonitorHeart, "Hard-Wired", "50-Day Exercise Goal Streak", "Rare", achievementsStatus[17], achievementsData.maxExerciseStreak, 50) }
                    item { AchievementItem(Icons.Filled.StackedLineChart, "Marathoner", "50-Day Step Goal Streak", "Rare", achievementsStatus[18], achievementsData.maxStepsStreak, 50) }
                    item { AchievementItem(Icons.Filled.NaturePeople, "Phew...", "20000 Steps in a Day", "Rare", achievementsStatus[19], achievementsData.maxSteps, 20000) }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Epic: ${achievementsStatus.subList(20, 30).count { it }} / 10",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { AchievementItem(Icons.Filled.Waves, "Reservoir", "365 Water Goals Completed", "Epic", achievementsStatus[20], achievementsData.totalWaterGoals, 365) }
                    item { AchievementItem(Icons.Filled.OutdoorGrill, "Master Chef", "365 Calorie Goals Completed", "Epic", achievementsStatus[21], achievementsData.totalCaloriesGoals, 365) }
                    item { AchievementItem(Icons.Filled.Power, "Power House", "365 Exercise Goals Completed", "Epic", achievementsStatus[22], achievementsData.totalExerciseGoals, 365) }
                    item { AchievementItem(Icons.Filled.NordicWalking, "Trailblazer", "365 Step Goals Completed", "Epic", achievementsStatus[23], achievementsData.totalStepsGoals, 365) }
                    item { AchievementItem(Icons.Filled.Language, "Globetrotter", "5000000 Steps Made", "Epic", achievementsStatus[24], achievementsData.totalSteps, 5000000) }
                    item { AchievementItem(Icons.Filled.Pool, "Tidal Force", "100-Day Water Goal Streak", "Epic", achievementsStatus[25], achievementsData.maxWaterStreak, 100) }
                    item { AchievementItem(Icons.Filled.LocalFireDepartment, "Metabolic Monster", "100-Day Calorie Goal Streak", "Epic", achievementsStatus[26], achievementsData.maxCaloriesStreak, 100) }
                    item { AchievementItem(Icons.Filled.Security, "Ironclad", "100-Day Exercise Goal Streak", "Epic", achievementsStatus[27], achievementsData.maxExerciseStreak, 100) }
                    item { AchievementItem(Icons.Filled.Landscape, "Scout", "100-Day Step Goal Streak", "Epic", achievementsStatus[28], achievementsData.maxStepsStreak, 100) }
                    item { AchievementItem(Icons.Filled.Snowshoeing, "Golden Legs", "50000 Steps in a Day", "Epic", achievementsStatus[29], achievementsData.maxSteps, 50000) }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Legendary: ${achievementsStatus.subList(30, 37).count { it }} / $totalLegendary",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { AchievementItem(Icons.Filled.Anchor, "Hall of Famer", "Appeared on Water Leaderboards", "Legendary", achievementsStatus[30], 0, 1) }
                    item { AchievementItem(Icons.Filled.LocationCity, "Household Name", "Appeared on Calories Leaderboards", "Legendary", achievementsStatus[31], 0, 1) }
                    item { AchievementItem(Icons.Filled.HotelClass, "Superstar", "Appeared on Exercise Leaderboards", "Legendary", achievementsStatus[32], 0, 1) }
                    item { AchievementItem(Icons.Filled.WorkspacePremium, "Record Holder", "Appeared on Steps Leaderboards", "Legendary", achievementsStatus[33], 0, 1) }
                    item { AchievementItem(Icons.Filled.Public, "Healthterra's Finest", "Crowned \"Healthiest User\"", "Legendary", achievementsStatus[34], 0, 1) }
                    item { AchievementItem(Icons.Filled.QuestionMark, "Secret", "???", "Legendary", achievementsStatus[35], 0, 1) }

                    if (isEarlyPlaytesterVisible) {
                        item { AchievementItem(Icons.Filled.Science, "Early Playtester", "We couldn't have done it without you!", "Legendary", achievementsStatus[36], if (achievementsStatus[36]) 1 else 0, 1) }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Impossible: ${achievementsStatus.subList(37, 47).count { it }} / 10",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { AchievementItem(ImageVector.vectorResource(id = R.drawable.leviathan), "Leviathan", "1000 Water Goals Completed", "Impossible", achievementsStatus[37], achievementsData.totalWaterGoals, 1000) }
                    item { AchievementItem(Icons.AutoMirrored.Filled.MenuBook, "Alchemist", "1000 Calorie Goals Completed", "Impossible", achievementsStatus[38], achievementsData.totalCaloriesGoals, 1000) }
                    item { AchievementItem(ImageVector.vectorResource(id = R.drawable.juggernaut), "Juggernaut", "1000 Exercise Goals Completed", "Impossible", achievementsStatus[39], achievementsData.totalExerciseGoals, 1000) }
                    item { AchievementItem(ImageVector.vectorResource(id = R.drawable.griffin), "Griffin", "1000 Step Goals Completed", "Impossible", achievementsStatus[40], achievementsData.totalStepsGoals, 1000) }
                    item { AchievementItem(ImageVector.vectorResource(id = R.drawable.phoenix), "Phoenix", "10000000 Steps Made", "Impossible", achievementsStatus[41], achievementsData.totalSteps, 10000000) }
                    item { AchievementItem(ImageVector.vectorResource(id = R.drawable.hydra), "Hydra", "365-Day Water Goal Streak", "Impossible", achievementsStatus[42], achievementsData.maxWaterStreak, 365) }
                    item { AchievementItem(ImageVector.vectorResource(id = R.drawable.minotaur), "Minotaur", "365-Day Calorie Goal Streak", "Impossible", achievementsStatus[43], achievementsData.maxCaloriesStreak, 365) }
                    item { AchievementItem(ImageVector.vectorResource(id = R.drawable.golem), "Golem", "365-Day Exercise Goal Streak", "Impossible", achievementsStatus[44], achievementsData.maxExerciseStreak, 365) }
                    item { AchievementItem(ImageVector.vectorResource(id = R.drawable.unicorn), "Unicorn", "365-Day Step Goal Streak", "Impossible", achievementsStatus[45], achievementsData.maxStepsStreak, 365) }
                    item { AchievementItem(ImageVector.vectorResource(id = R.drawable.skull), "How...", "100000 Steps in a Day", "Impossible", achievementsStatus[46], achievementsData.maxSteps, 100000) }
                }
            }
        }
    }
}
