package com.example.expensemanager.repository

import com.example.expensemanager.database.User
import com.example.expensemanager.database.UserDao

class UserRepository(private val userDao: UserDao) {

    suspend fun registerUser(user: User): Long{
        return userDao.insertUser(user)
    }

    suspend fun login(userName: String, password: String): User?{
        return userDao.login(userName, password)
    }

    suspend fun getUserById(userId: Int): User?{
        return userDao.getUserById(userId)
    }

    suspend fun updateUser(user: User): Int{
        return userDao.updateUser(user)
    }

    suspend fun checkUserNameExists(userName: String):Boolean{
        return userDao.getUserByUsername(userName) != null
    }
}