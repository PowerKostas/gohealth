package com.healthterra.helpers

import android.icu.text.CompactDecimalFormat
import java.util.Locale

// Turns "173938" to "173.9K"
fun formatNumber(number: Int): String {
    val formatter = CompactDecimalFormat.getInstance(
        Locale.getDefault(),
        CompactDecimalFormat.CompactStyle.SHORT
    ).apply {
        maximumFractionDigits = 1
    }

    return formatter.format(number)
}
