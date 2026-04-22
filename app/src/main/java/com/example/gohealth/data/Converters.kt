package com.example.gohealth.data

import androidx.room.TypeConverter
import java.time.LocalDate

// I have to use this trick because SQLite cannot store lists or dates in a column
class Converters {
    // Converts the list to a comma-separated string: "1,2,3"
    @TypeConverter
    fun fromIntList(list: List<Int?>?): String? {
        return list?.joinToString(", ")
    }

    // Converts the comma-separated string back to a list of Ints
    @TypeConverter
    fun toIntList(data: String?): List<Int?> {
        if (data.isNullOrBlank()) return emptyList()
        return data.split(", ").mapNotNull { it.toIntOrNull() }
    }

    // Converts the LocalDate to a Long
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    // Converts the Long to a LocalDate
    @TypeConverter
    fun toLocalDate(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }
}
