package com.kostas.gohealth.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0,
    @ColumnInfo(name = "firestore_id") val firestoreId: String = java.util.UUID.randomUUID().toString(),
    @ColumnInfo(name = "profile_picture_string") val profilePictureString: String,
    @ColumnInfo(name = "username") val username: String?,
    @ColumnInfo(name = "appearance") val appearance: String,
    @ColumnInfo(name = "last_reset_date") val lastResetDate: LocalDate,
    @ColumnInfo(name = "last_saved_steps") val lastSavedSteps: Int,
    @ColumnInfo(name = "step_tracking") val stepTracking: String
)
