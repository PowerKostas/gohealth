package com.example.gohealth.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.gohealth.data.daos.UsersDao
import com.example.gohealth.data.entities.Users

@Database(entities = [Users::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UsersDao
}
