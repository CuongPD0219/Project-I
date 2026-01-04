package com.example.expensemanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.database.AppDatabase
import com.example.expensemanager.database.User
import com.example.expensemanager.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserRepository

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _updateResult = MutableLiveData<Result<Boolean>>()
    val updateResult: LiveData<Result<Boolean>> = _updateResult

    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        repository = UserRepository(userDao)
    }

    fun loadUser(userId: Int) {
        viewModelScope.launch {
            try {
                val userData = repository.getUserById(userId)
                _user.value = userData
            } catch (e: Exception) {
                _user.value = null
            }
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            try {
                val result = repository.updateUser(user)
                _updateResult.value = Result.success(result > 0)
            } catch (e: Exception) {
                _updateResult.value = Result.failure(e)
            }
        }
    }
}