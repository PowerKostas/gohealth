package com.kostas.gohealth.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.kostas.gohealth.R
import com.kostas.gohealth.helpers.checkActivityPermissions
import com.kostas.gohealth.ui.components.general.ProgressBar

@Composable
fun StepsScreen(categoryProgress: Int, categoryGoal: Int) {
    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(70.dp, Alignment.CenterVertically),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Checks the value of isActivityRecognitionEnabled every time the screen comes back into focus and if a change happened, it
        // redraws the screen
        val context = LocalContext.current

        var isActivityRecognitionEnabled by remember { mutableStateOf(checkActivityPermissions(context)) }

        LifecycleResumeEffect(Unit) {
            isActivityRecognitionEnabled = checkActivityPermissions(context)
            onPauseOrDispose { }
        }

        val text = buildAnnotatedString {
            if (isActivityRecognitionEnabled) {
                append("Currently tracking steps...")
            }

            else {
                append("Currently not tracking steps. Physical activity permission is ")

                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.error)) {
                    append("disabled")
                }

                append(".")
            }
        }

        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFFE0AC69),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Icon(
            painter = painterResource(id = R.drawable.steps),
            contentDescription = "Category",
            tint = Color.Unspecified,
            modifier = Modifier.size(200.dp)
        )

        val remainder = categoryGoal % 10
        val roundedCategoryGoal = when {
            remainder < 5 -> categoryGoal - remainder
            remainder == 5 -> categoryGoal
            else -> categoryGoal + (10 - remainder)
        }

        Text(
            text = "$categoryProgress / $roundedCategoryGoal steps",
            style = MaterialTheme.typography.titleLarge
        )

        ProgressBar(20.dp, Color(0xFFE0AC69), categoryProgress.toFloat() / roundedCategoryGoal)
    }
}
