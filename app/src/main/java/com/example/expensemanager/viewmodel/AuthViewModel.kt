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

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserRepository

    private val _loginResult = MutableLiveData<Result<User>>()
    val loginResult: LiveData<Result<User>> = _loginResult

    private val _registerResult = MutableLiveData<Result<Long>>()
    val registerResult: LiveData<Result<Long>> = _registerResult

    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        repository = UserRepository(userDao)
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val user = repository.login(username, password)
                if (user != null) {
                    _loginResult.value = Result.success(user)
                } else {
                    _loginResult.value = Result.failure(Exception("Tên đăng nhập hoặc mật khẩu không đúng"))
                }
            } catch (e: Exception) {
                _loginResult.value = Result.failure(e)
            }
        }
    }

    fun register(user: User) {
        viewModelScope.launch {
            try {
                // Kiểm tra username đã tồn tại
                if (repository.checkUsernameExists(user.username)) {
                    _registerResult.value = Result.failure(Exception("Tên đăng nhập đã tồn tại"))
                    return@launch
                }

                val result = repository.registerUser(user)
                if (result > 0) {
                    _registerResult.value = Result.success(result)
                } else {
                    _registerResult.value = Result.failure(Exception("Đăng ký thất bại"))
                }
            } catch (e: Exception) {
                _registerResult.value = Result.failure(e)
            }
        }
    }
}