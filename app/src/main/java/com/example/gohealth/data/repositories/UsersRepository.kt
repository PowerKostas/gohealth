package com.example.gohealth.data.repositories

import com.example.gohealth.data.daos.UsersDao
import com.example.gohealth.data.entities.Users
import kotlinx.coroutines.flow.Flow

class UsersRepository(private val usersDao: UsersDao) {
    val allUsers: Flow<List<Users>> = usersDao.getAll()

    suspend fun insert(user: Users) {
        usersDao.insert(user)
    }

    suspend fun update(user: Users) {
        usersDao.update(user)
    }
}
