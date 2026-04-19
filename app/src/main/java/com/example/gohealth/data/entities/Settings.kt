package com.example.gohealth.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "profile_picture_string") val profilePictureString: String,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "appearance") val appearance: String
)
