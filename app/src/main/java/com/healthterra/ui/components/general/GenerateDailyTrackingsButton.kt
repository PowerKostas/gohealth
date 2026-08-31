package com.healthterra.ui.components.general

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthterra.data.entities.DailyTrackings
import com.healthterra.ui.viewModels.DailyTrackingsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

// Button that fills the daily trackings table with fake data, just for testing
@Composable
fun GenerateDailyTrackingsButton(days: Int) {
    val dailyTrackingsViewModel = viewModel<DailyTrackingsViewModel>(factory = DailyTrackingsViewModel.Factory)

    Button(
        onClick = {
            val today = LocalDate.now().minusDays(2)
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            for (i in 0 until days) {
                val dateStr = today.minusDays(i.toLong()).format(dateFormatter)

                val mockData = DailyTrackings(
                    userId = 1,
                    date = dateStr,
                    waterProgress = Random.nextInt(500, 3000),
                    caloriesProgress = Random.nextInt(1200, 3200),
                    exerciseProgress = Random.nextInt(0, 90),
                    stepsProgress = Random.nextInt(1000, 15000),
                    waterGoal = 0,
                    caloriesGoal = 0,
                    exerciseGoal = 0,
                    stepsGoal = 0,
                    caloriesBurned = Random.nextInt(300, 1000)
                )

                dailyTrackingsViewModel.upsertUserDailyTrackings(mockData)
            }
        }
    ) {
        Text("Generate $days Days of Random Data")
    }
}
