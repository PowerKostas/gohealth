package com.healthterra.helpers

// For a better view, it rounds the value
fun roundValue(value: Int): Int {
    val remainder = value % 10
    val roundedValue = when {
        remainder < 5 -> value - remainder
        remainder == 5 -> value
        else -> value + (10 - remainder)
    }

    return roundedValue
}
