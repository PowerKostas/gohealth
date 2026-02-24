package com.example.gohealth.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gohealth.data.entities.Users
import kotlinx.coroutines.flow.Flow

@Dao
interface UsersDao {
    @Query("SELECT * FROM users")
    fun getAll(): Flow<List<Users>>

    @Insert
    suspend fun insert(user: Users)

    @Update
    suspend fun update(user: Users)
}
