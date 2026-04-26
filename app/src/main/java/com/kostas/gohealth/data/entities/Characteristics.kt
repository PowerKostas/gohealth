package com.kostas.gohealth.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// An entity represents a table within the local Room API database, uses userId from Settings as a foreign key
@Entity(
    tableName = "characteristics",
    foreignKeys = [
        ForeignKey(
            entity = Settings::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Characteristics(
    @PrimaryKey val userId: Int,
    @ColumnInfo(name = "gender") val gender: String?,
    @ColumnInfo(name = "age") val age: Float?,
    @ColumnInfo(name = "height") val height: Float?,
    @ColumnInfo(name = "weight") val weight: Float?,
    @ColumnInfo(name = "activity_level") val activityLevel: String?,
    @ColumnInfo(name = "weight_goal") val weightGoal: String?
)
