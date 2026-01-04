package com.example.expensemanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.database.AppDatabase
import com.example.expensemanager.database.Expense
import com.example.expensemanager.repository.ExpenseRepository
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository

    private val _expenses = MutableLiveData<List<Expense>>()
    val expenses: LiveData<List<Expense>> = _expenses

    private val _totalExpense = MutableLiveData<Double>()
    val totalExpense: LiveData<Double> = _totalExpense

    private val _totalIncome = MutableLiveData<Double>()
    val totalIncome: LiveData<Double> = _totalIncome

    private val _balance = MutableLiveData<Double>()
    val balance: LiveData<Double> = _balance

    private val _operationResult = MutableLiveData<Result<Boolean>>()
    val operationResult: LiveData<Result<Boolean>> = _operationResult

    init {
        val expenseDao = AppDatabase.getDatabase(application).expenseDao()
        repository = ExpenseRepository(expenseDao)
    }

    fun loadAllExpenses(userId: Int) {
        viewModelScope.launch {
            try {
                val expenseList = AppDatabase.getDatabase(getApplication()).expenseDao().getAllExpenses(userId)
                _expenses.value = expenseList
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadExpensesByMonth(userId: Int, year: Int, month: Int) {
        viewModelScope.launch {
            try {
                val startDate = String.format("%04d-%02d-01", year, month)
                val endDate = String.format("%04d-%02d-31", year, month)
                val expenseList = AppDatabase.getDatabase(getApplication())
                    .expenseDao()
                    .getExpensesByMonth(userId, startDate, endDate)
                _expenses.value = expenseList
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadSummary(userId: Int) {
        viewModelScope.launch {
            try {
                val expense = repository.getTotalByType(userId, "expense")
                val income = repository.getTotalByType(userId, "income")

                _totalExpense.value = expense
                _totalIncome.value = income
                _balance.value = income - expense
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun insertExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                val result = repository.insertExpense(expense)
                _operationResult.value = Result.success(result > 0)
            } catch (e: Exception) {
                _operationResult.value = Result.failure(e)
            }
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                val result = repository.updateExpense(expense)
                _operationResult.value = Result.success(result > 0)
            } catch (e: Exception) {
                _operationResult.value = Result.failure(e)
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                val result = repository.deleteExpense(expense)
                _operationResult.value = Result.success(result > 0)
            } catch (e: Exception) {
                _operationResult.value = Result.failure(e)
            }
        }
    }

    fun getExpenseById(expenseId: Int, callback: (Expense?) -> Unit) {
        viewModelScope.launch {
            try {
                val expense = repository.getExpenseById(expenseId)
                callback(expense)
            } catch (e: Exception) {
                callback(null)
            }
        }
    }
}