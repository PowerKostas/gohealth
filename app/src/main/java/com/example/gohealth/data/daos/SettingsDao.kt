package com.example.gohealth.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.example.gohealth.data.entities.Settings

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings")
    fun getAll(): Flow<List<Settings>>

    @Insert
    suspend fun insert(user: Settings)

    @Update
    suspend fun update(user: Settings)
}
