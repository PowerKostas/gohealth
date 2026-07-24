package com.healthterra.helpers

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.FollowTheSigns
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Blind
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.HotelClass
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalDining
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
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.healthterra.R
import com.healthterra.data.entities.Achievements

data class AchievementData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isUnlocked: Boolean,
    val currentProgress: Int,
    val goal: Int,
    val tier: String
)

data class TieredAchievements(
    val common: List<AchievementData> = emptyList(),
    val rare: List<AchievementData> = emptyList(),
    val epic: List<AchievementData> = emptyList(),
    val legendary: List<AchievementData> = emptyList(),
    val impossible: List<AchievementData> = emptyList(),
    val optional: List<AchievementData> = emptyList()
) {
    val flatList: List<AchievementData>
        get() = common + rare + epic + legendary + impossible + optional
}


@Composable
fun getAchievementsData(otherAchievements: OtherAchievements, leaderboardsAchievements: Achievements, username: String): TieredAchievements {
    return TieredAchievements(
        common = listOf(
            AchievementData("Sipper", "Complete 10 Water Goals", Icons.Filled.Shower, otherAchievements.totalWaterGoals >= 10, otherAchievements.totalWaterGoals, 10, "Common"),
            AchievementData("Mindful Eater", "Complete 10 Calorie Goals", Icons.Filled.LocalDining, otherAchievements.totalCaloriesGoals >= 10, otherAchievements.totalCaloriesGoals, 10, "Common"),
            AchievementData("The Active One", "Complete 10 Exercise Goals", Icons.AutoMirrored.Filled.DirectionsBike, otherAchievements.totalExerciseGoals >= 10, otherAchievements.totalExerciseGoals, 10, "Common"),
            AchievementData("Foot Soldier", "Complete 10 Step Goals", Icons.AutoMirrored.Filled.DirectionsRun, otherAchievements.totalStepsGoals >= 10, otherAchievements.totalStepsGoals, 10, "Common"),
            AchievementData("Wanderer", "Walk 100000 Steps", Icons.Filled.Explore, otherAchievements.totalSteps >= 100000, otherAchievements.totalSteps, 100000, "Common"),
            AchievementData("Well-Watered", "Maintain a 5-Day Water Goal Streak", Icons.Filled.LocalFlorist, otherAchievements.maxWaterStreak >= 5, otherAchievements.maxWaterStreak, 5, "Common"),
            AchievementData("Dietitian", "Maintain a 5-Day Calorie Goal Streak", Icons.Filled.Balance, otherAchievements.maxCaloriesStreak >= 5, otherAchievements.maxCaloriesStreak, 5, "Common"),
            AchievementData("Catalyst", "Maintain a 5-Day Exercise Goal Streak", Icons.Filled.ElectricBolt, otherAchievements.maxExerciseStreak >= 5, otherAchievements.maxExerciseStreak, 5, "Common"),
            AchievementData("Road Warrior", "Maintain a 5-Day Step Goal Streak", Icons.Filled.NaturePeople, otherAchievements.maxStepsStreak >= 5, otherAchievements.maxStepsStreak, 5, "Common")
        ),

        rare = listOf(
            AchievementData("Water Regular", "Complete 100 Water Goals", Icons.Filled.LocalCafe, otherAchievements.totalWaterGoals >= 100, otherAchievements.totalWaterGoals, 100, "Rare"),
            AchievementData("Nutritionist", "Complete 100 Calorie Goals", Icons.Filled.SoupKitchen, otherAchievements.totalCaloriesGoals >= 100, otherAchievements.totalCaloriesGoals, 100, "Rare"),
            AchievementData("Athlete", "Complete 100 Exercise Goals", Icons.Filled.SportsTennis, otherAchievements.totalExerciseGoals >= 100, otherAchievements.totalExerciseGoals, 100, "Rare"),
            AchievementData("Pathfinder", "Complete 100 Step Goals", Icons.Filled.Blind, otherAchievements.totalStepsGoals >= 100, otherAchievements.totalStepsGoals, 100, "Rare"),
            AchievementData("Voyager", "Walk 1000000 Steps", Icons.Filled.Navigation, otherAchievements.totalSteps >= 1000000, otherAchievements.totalSteps, 1000000, "Rare"),
            AchievementData("Oasis", "Maintain a 50-Day Water Goal Streak", Icons.Filled.Spa, otherAchievements.maxWaterStreak >= 50, otherAchievements.maxWaterStreak, 50, "Rare"),
            AchievementData("Fully Fueled", "Maintain a 50-Day Calorie Goal Streak", Icons.Filled.BatteryChargingFull, otherAchievements.maxCaloriesStreak >= 50, otherAchievements.maxCaloriesStreak, 50, "Rare"),
            AchievementData("Hard-Wired", "Maintain a 50-Day Exercise Goal Streak", Icons.Filled.MonitorHeart, otherAchievements.maxExerciseStreak >= 50, otherAchievements.maxExerciseStreak, 50, "Rare"),
            AchievementData("Marathoner", "Maintain a 50-Day Step Goal Streak", Icons.AutoMirrored.Filled.FollowTheSigns, otherAchievements.maxStepsStreak >= 50, otherAchievements.maxStepsStreak, 50, "Rare")
        ),

        epic = listOf(
            AchievementData("Reservoir", "Complete 365 Water Goals", Icons.Filled.Waves, otherAchievements.totalWaterGoals >= 365, otherAchievements.totalWaterGoals, 365, "Epic"),
            AchievementData("Master Chef", "Complete 365 Calorie Goals", Icons.Filled.OutdoorGrill, otherAchievements.totalCaloriesGoals >= 365, otherAchievements.totalCaloriesGoals, 365, "Epic"),
            AchievementData("Power House", "Complete 365 Exercise Goals", Icons.Filled.Power, otherAchievements.totalExerciseGoals >= 365, otherAchievements.totalExerciseGoals, 365, "Epic"),
            AchievementData("Trailblazer", "Complete 365 Step Goals", Icons.Filled.NordicWalking, otherAchievements.totalStepsGoals >= 365, otherAchievements.totalStepsGoals, 365, "Epic"),
            AchievementData("Globetrotter", "Walk 5000000 Steps", Icons.Filled.Language, otherAchievements.totalSteps >= 5000000, otherAchievements.totalSteps, 5000000, "Epic"),
            AchievementData("Tidal Force", "Maintain a 100-Day Water Goal Streak", Icons.Filled.Pool, otherAchievements.maxWaterStreak >= 100, otherAchievements.maxWaterStreak, 100, "Epic"),
            AchievementData("Metabolic Monster", "Maintain a 100-Day Calorie Goal Streak", Icons.Filled.Whatshot, otherAchievements.maxCaloriesStreak >= 100, otherAchievements.maxCaloriesStreak, 100, "Epic"),
            AchievementData("Ironclad", "Maintain a 100-Day Exercise Goal Streak", Icons.Filled.Security, otherAchievements.maxExerciseStreak >= 100, otherAchievements.maxExerciseStreak, 100, "Epic"),
            AchievementData("Scout", "Maintain a 100-Day Step Goal Streak", Icons.Filled.Landscape, otherAchievements.maxStepsStreak >= 100, otherAchievements.maxStepsStreak, 100, "Epic")
        ),

        legendary = listOf(
            AchievementData("Hall of Famer", "Appear on the Water Leaderboard", Icons.Filled.Anchor, leaderboardsAchievements.appearWaterLeaderboards, if (leaderboardsAchievements.appearWaterLeaderboards) 1 else 0, 1, "Legendary"),
            AchievementData("Household Name", "Appear on the Calories Leaderboard", Icons.Filled.LocationCity, leaderboardsAchievements.appearCaloriesLeaderboards, if (leaderboardsAchievements.appearCaloriesLeaderboards) 1 else 0, 1, "Legendary"),
            AchievementData("Superstar", "Appear on the Exercise Leaderboard", Icons.Filled.HotelClass, leaderboardsAchievements.appearExerciseLeaderboards, if (leaderboardsAchievements.appearExerciseLeaderboards) 1 else 0, 1, "Legendary"),
            AchievementData("Record Holder", "Appear on the Steps Leaderboard", Icons.Filled.WorkspacePremium, leaderboardsAchievements.appearStepsLeaderboards, if (leaderboardsAchievements.appearStepsLeaderboards) 1 else 0, 1, "Legendary"),
            AchievementData("Global Icon", "Appear on the Total Steps Leaderboard", Icons.Filled.EmojiEvents, leaderboardsAchievements.appearTotalStepsLeaderboards, if (leaderboardsAchievements.appearTotalStepsLeaderboards) 1 else 0, 1, "Legendary"),
            AchievementData("Healthterra's Finest", "Become the \"Healthiest User\"", Icons.Filled.Public, leaderboardsAchievements.appearHealthiestUser, if (leaderboardsAchievements.appearHealthiestUser) 1 else 0, 1, "Legendary"),
            AchievementData("Secret", if (leaderboardsAchievements.secret) "Take the top spot on every single leaderboard. You might be the only person to ever achieve this!" else "???", Icons.Filled.QuestionMark, leaderboardsAchievements.secret, if (leaderboardsAchievements.secret) 1 else 0, 1, "Legendary")
        ),

        impossible = listOf(
            AchievementData("Leviathan", "Complete 1000 Water Goals", ImageVector.vectorResource(id = R.drawable.leviathan), otherAchievements.totalWaterGoals >= 1000, otherAchievements.totalWaterGoals, 1000, "Impossible"),
            AchievementData("Alchemist", "Complete 1000 Calorie Goals", Icons.AutoMirrored.Filled.MenuBook, otherAchievements.totalCaloriesGoals >= 1000, otherAchievements.totalCaloriesGoals, 1000, "Impossible"),
            AchievementData("Juggernaut", "Complete 1000 Exercise Goals", ImageVector.vectorResource(id = R.drawable.juggernaut), otherAchievements.totalExerciseGoals >= 1000, otherAchievements.totalExerciseGoals, 1000, "Impossible"),
            AchievementData("Griffin", "Complete 1000 Step Goals", ImageVector.vectorResource(id = R.drawable.griffin), otherAchievements.totalStepsGoals >= 1000, otherAchievements.totalStepsGoals, 1000, "Impossible"),
            AchievementData("Phoenix", "Walk 10000000 Steps", ImageVector.vectorResource(id = R.drawable.phoenix), otherAchievements.totalSteps >= 10000000, otherAchievements.totalSteps, 10000000, "Impossible"),
            AchievementData("Hydra", "Maintain a 365-Day Water Goal Streak", ImageVector.vectorResource(id = R.drawable.hydra), otherAchievements.maxWaterStreak >= 365, otherAchievements.maxWaterStreak, 365, "Impossible"),
            AchievementData("Minotaur", "Maintain a 365-Day Calorie Goal Streak", ImageVector.vectorResource(id = R.drawable.minotaur), otherAchievements.maxCaloriesStreak >= 365, otherAchievements.maxCaloriesStreak, 365, "Impossible"),
            AchievementData("Golem", "Maintain a 365-Day Exercise Goal Streak", ImageVector.vectorResource(id = R.drawable.golem), otherAchievements.maxExerciseStreak >= 365, otherAchievements.maxExerciseStreak, 365, "Impossible"),
            AchievementData("Unicorn", "Maintain a 365-Day Step Goal Streak", ImageVector.vectorResource(id = R.drawable.unicorn), otherAchievements.maxStepsStreak >= 365, otherAchievements.maxStepsStreak, 365, "Impossible")
        ),

        // Early Playtester achievement only shows up if it's true
        optional = listOf(
            AchievementData("Early Playtester", "We couldn't have done it without you!", Icons.Filled.Science, username == "TheWalkingDemi®", if (username == "TheWalkingDemi®") 1 else 0, 1, "Legendary")
        )
    )
}
