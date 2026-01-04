package com.example.expensemanager.repository

import com.example.expensemanager.database.User
import com.example.expensemanager.database.UserDao

class UserRepository(private val userDao: UserDao) {

    suspend fun registerUser(user: User): Long {
        return userDao.insertUser(user)
    }

    suspend fun login(username: String, password: String): User? {
        return userDao.login(username, password)
    }

    suspend fun getUserById(userId: Int): User? {
        return userDao.getUserById(userId)
    }

    suspend fun updateUser(user: User): Int {
        return userDao.updateUser(user)
    }

    suspend fun checkUsernameExists(username: String): Boolean {
        return userDao.getUserByUsername(username) != null
    }
}