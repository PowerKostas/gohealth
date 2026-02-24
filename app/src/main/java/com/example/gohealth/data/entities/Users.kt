package com.example.gohealth.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class Users(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "profile_picture_path") val profilePicturePath: String,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "gender") val gender: String,
    @ColumnInfo(name = "age") val age: Int,
    @ColumnInfo(name = "height") val height: Int,
    @ColumnInfo(name = "weight") val weight: Int,
    @ColumnInfo(name = "activity_level") val activityLevel: String,
    @ColumnInfo(name = "weight_goal") val weightGoal: String,
    @ColumnInfo(name = "appearance") val appearance: String
)
