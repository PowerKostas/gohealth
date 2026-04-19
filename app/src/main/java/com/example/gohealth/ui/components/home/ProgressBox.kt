package com.example.gohealth.ui.components.home

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.gohealth.ui.components.general.ProgressBar

@Composable
fun ProgressBox(iconId: Int, category: String, progressBarColour: Color) {
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .border(
                2.dp,
                MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(12.dp)
            )

            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = category,
                tint = Color.Unspecified,
                modifier = Modifier.size(80.dp)
            )

            Text(
                text = category,
                style = MaterialTheme.typography.labelLarge
            )

            ProgressBar(8.dp, progressBarColour)

            Text(
                text = "25%",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
