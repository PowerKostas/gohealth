package com.healthterra.helpers

// For a better view, it rounds 0-4 down to 0, leaves 5 as 5, and rounds 6-9 up to 10
fun roundValue(value: Int): Int {
    val remainder = value % 10
    val roundedValue = when {
        remainder < 5 -> value - remainder
        remainder == 5 -> value
        else -> value + (10 - remainder)
    }

    return roundedValue
}
