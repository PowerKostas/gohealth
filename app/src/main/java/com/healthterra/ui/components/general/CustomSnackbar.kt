package com.healthterra.ui.components.general

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.healthterra.helpers.CustomSnackbarData

// Have to create a CustomSnackbarData class like the one in PrepareAchievementSnackbar for this component to work
@Composable
fun CustomSnackbar(modifier: Modifier = Modifier, hostState: SnackbarHostState) {
    val context = LocalContext.current

    SnackbarHost(
        hostState = hostState,
        modifier = modifier
    ) { snackbarData ->
        val visuals = snackbarData.visuals as CustomSnackbarData

        // Optional sound effect
        LaunchedEffect(snackbarData) {
            visuals.sound?.let { safeSound ->
                MediaPlayer.create(context, safeSound)?.apply {
                    setOnCompletionListener { release() }
                    start()
                }
            }
        }

        Card(
            modifier = Modifier.padding(horizontal = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(visuals.backgroundBrush)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = visuals.icon,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color.White
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = visuals.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )

                    Text(
                        text = visuals.title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = visuals.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}
