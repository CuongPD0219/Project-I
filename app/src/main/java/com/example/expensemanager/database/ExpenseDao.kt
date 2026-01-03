package com.example.expensemanager.database

import androidx.room.*

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Update
    suspend fun updateExpense(expense: Expense): Int

    @Delete
    suspend fun deleteExpense(expense: Expense): Int

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpenseById(expenseId: Int): Int

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC")
    suspend fun getAllExpenses(userId: Int): List<Expense>

    @Query("Select * FROM expenses WHERE userId =:userId AND date >= :startDate AND date <= :endDate")
    suspend fun getExpensesByMonth(userId: Int, startDate: String, endDate: String): List<Expense>

    @Query("SELECT SUM(amount) FROM expenses WHERE userID = :userId AND type = :type")
    suspend fun getTotalByType(userId: Int, type: String): Double?

    @Query("SELECT * FROM expenses WHERE id = :expenseId LIMIT 1")
    suspend fun getExpenseById(expenseId: Int): Expense?
}
