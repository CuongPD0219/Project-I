package com.example.expensemanager.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.expensemanager.database.Expense
import com.example.expensemanager.database.ExpenseDao
import kotlinx.coroutines.Dispatchers

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    fun getAllExpenses(userId: Int): LiveData<List<Expense>> = liveData(Dispatchers.IO) {
        val expenses = expenseDao.getAllExpenses(userId)
        emit(expenses)
    }

    fun getExpensesByMonth(userId: Int, startDate: String, endDate: String): LiveData<List<Expense>> =
        liveData(Dispatchers.IO) {
            val expenses = expenseDao.getExpensesByMonth(userId, startDate, endDate)
            emit(expenses)
        }

    suspend fun insertExpense(expense: Expense): Long {
        return expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: Expense): Int {
        return expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense): Int {
        return expenseDao.deleteExpense(expense)
    }

    suspend fun getExpenseById(expenseId: Int): Expense? {
        return expenseDao.getExpenseById(expenseId)
    }

    suspend fun getTotalByType(userId: Int, type: String): Double {
        return expenseDao.getTotalByType(userId, type) ?: 0.0
    }
}