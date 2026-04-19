package com.example.gohealth.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// An entity represents a table within the database
@Entity(tableName = "characteristics")
data class Characteristics(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "gender") val gender: String,
    @ColumnInfo(name = "age") val age: Float?,
    @ColumnInfo(name = "height") val height: Float?,
    @ColumnInfo(name = "weight") val weight: Float?,
    @ColumnInfo(name = "activity_level") val activityLevel: String,
    @ColumnInfo(name = "weight_goal") val weightGoal: String
)
