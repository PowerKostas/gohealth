package com.example.gohealth.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.gohealth.R
import com.example.gohealth.ui.components.home.ProgressBox

@Composable
fun HomeScreen() {
    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface)

    Column(
        verticalArrangement = Arrangement.spacedBy(64.dp),
        modifier = Modifier
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        ProgressBox(R.drawable.water, "Water", Color(0xFF2196F3))
        ProgressBox(R.drawable.calories, "Calories", Color(0xFF8B4513))
        ProgressBox(R.drawable.push_ups, "Push-ups", Color.Black)
        ProgressBox(R.drawable.steps, "Steps", Color(0xFFE0AC69))
    }
}
