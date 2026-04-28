package com.kostas.gohealth.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kostas.gohealth.ui.components.general.ActionButton
import com.kostas.gohealth.ui.components.general.ProgressBar
import com.kostas.gohealth.ui.components.screen.CustomAlertDialog
import com.kostas.gohealth.ui.viewModels.TrackingsViewModel

// Water, calories and push-ups screen
@Composable
fun CategoriesScreen(categoryName: String, iconId: Int, progressBarColour: Color, categoryProgress: Int, categoryGoal: Int, metric: String) {
    val trackingsViewModel: TrackingsViewModel = viewModel(factory = TrackingsViewModel.Factory)
    val userTrackingsList by trackingsViewModel.trackings.collectAsState()
    val userTrackings = userTrackingsList.firstOrNull()

    var showCustomAlertDialog by remember { mutableStateOf(false) }

    // Handles which category to update
    val handleAddAmount: (Int) -> Unit = { amount ->
        userTrackings?.let { trackings ->
            val updatedTrackings = when (categoryName) {
                "Water" -> trackings.copy(waterProgress = trackings.waterProgress + amount)
                "Calories" -> trackings.copy(caloriesProgress = trackings.caloriesProgress + amount)
                "Push-ups" -> trackings.copy(pushUpsProgress = trackings.pushUpsProgress + amount)
                else -> throw IllegalStateException("Invalid Input")
            }

            trackingsViewModel.updateUserTrackings(updatedTrackings)
        }
    }

    val handleDeletePrevious: () -> Unit = {
        userTrackings?.let { trackings ->
            val updatedUser = when (categoryName) {
                "Water" -> trackings.copy(waterProgress = trackings.waterProgress.dropLast(1))
                "Calories" -> trackings.copy(caloriesProgress = trackings.caloriesProgress.dropLast(1))
                "Push-ups" -> trackings.copy(pushUpsProgress = trackings.pushUpsProgress.dropLast(1))
                else -> throw IllegalStateException("Invalid Input")
            }
            trackingsViewModel.updateUserTrackings(updatedUser)
        }
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(40.dp, Alignment.CenterVertically),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = "Category",
            tint = Color.Unspecified,
            modifier = Modifier.size(200.dp)
        )

        Text(
            text = "$categoryProgress / $categoryGoal $metric",
            style = MaterialTheme.typography.titleLarge
        )

        ProgressBar(20.dp, progressBarColour, categoryProgress.toFloat() / categoryGoal)

        Text(text = "Add $metric to reach your goal!")

        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                val modifier = Modifier.weight(1f)
                ActionButton(modifier, progressBarColour, "+1") { handleAddAmount(1) }
                ActionButton(modifier, progressBarColour, "+10") { handleAddAmount(10) }
                ActionButton(modifier, progressBarColour, "+100") { handleAddAmount(100) }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                val modifier = Modifier.weight(1f)
                ActionButton(modifier, progressBarColour, "Custom") { showCustomAlertDialog = true }
                ActionButton(modifier, Color(0xFFE53935), "Undo") { handleDeletePrevious() }
            }
        }
    }

    if (showCustomAlertDialog) {
        CustomAlertDialog(
            metric = metric,
            onDismiss = { showCustomAlertDialog = false },
            onConfirm = { amount ->
                handleAddAmount(amount)
            }
        )
    }
}
