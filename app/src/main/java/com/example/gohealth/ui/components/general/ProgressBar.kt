package com.example.gohealth.ui.components.general

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ProgressBar(height: Dp, colour: Color) {
    LinearProgressIndicator(
        progress = { 0.25f },

        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(4.dp)),

        color = colour,
        trackColor = Color.White,
        strokeCap = StrokeCap.Butt,
        gapSize = 0.dp,
        drawStopIndicator = {}
    )
}
