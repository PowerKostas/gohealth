package com.healthterra.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "achievements",
    foreignKeys = [
        ForeignKey(
            entity = Settings::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class Achievements(
    @PrimaryKey val userId: Int,
    @ColumnInfo(name = "apper_water_leaderboards") val appearWaterLeaderboards: Boolean = false,
    @ColumnInfo(name = "appear_calories_leaderboards") val appearCaloriesLeaderboards: Boolean = false,
    @ColumnInfo(name = "appear_exercise_leaderboards") val appearExerciseLeaderboards: Boolean = false,
    @ColumnInfo(name = "appear_steps_leaderboards") val appearStepsLeaderboards: Boolean = false,
    @ColumnInfo(name = "appear_total_steps_leaderboards") val appearTotalStepsLeaderboards: Boolean = false,
    @ColumnInfo(name = "appear_healthiest_user") val appearHealthiestUser: Boolean = false,
    @ColumnInfo(name = "secret") val secret: Boolean = false,
)
