package com.healthterra.ui.components.general

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PillButtonGroup(options: List<String>, rows: Int, selectedOption: String, onOptionSelected: (String) -> Unit) {
    // Divides the options into x rows, if odd amount of options, extra option goes to the first row
    val itemsPerRow = (options.size + rows - 1) / rows
    val rows = options.chunked(itemsPerRow)

    Column(
        verticalArrangement = Arrangement.spacedBy(1.dp), // Creates horizontal grid lines
        modifier = Modifier
            .width(IntrinsicSize.Max) // Limits the width of the component to only what is needed
            .clip(RoundedCornerShape(8.dp))
            .background(Color.LightGray) // Colors all the grid lines
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)), // Outer border
    ) {
        rows.forEach { rowOptions ->
            Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) { // Creates vertical grid lines
                rowOptions.forEach { option ->
                    val isSelected = option == selectedOption

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                            .clickable { onOptionSelected(option) }
                            .padding(10.dp)
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}
