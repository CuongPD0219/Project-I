package com.example.expensemanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.util.recursiveFetchLongSparseArray
import com.example.expensemanager.R
import com.example.expensemanager.database.AppDatabase
import com.example.expensemanager.database.User
import com.example.expensemanager.repository.UserRepository
import com.example.expensemanager.utils.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: UserRepository
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user
    private val _imgProfile = MutableLiveData<Int?>()
    val imgProfile: LiveData<Int?> = _imgProfile
    private val _updateResult = SingleLiveEvent<Result<Boolean>>()
    val updateResult: SingleLiveEvent<Result<Boolean>> = _updateResult

    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        repository = UserRepository(userDao)
    }


    fun loadImgProfile(imgId: Int) {
        viewModelScope.launch {
            try {
                _imgProfile.value = imgId?: R.drawable.mu
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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
                if(result > 0){
                    _updateResult.value = Result.success(true)
                    _user.postValue(user)
                }else{
                    _updateResult.value = Result.success(false)
                }

            } catch (e: Exception) {
                _updateResult.value = Result.failure(e)
            }
        }
    }
}