package com.iie.thethreeburnouts.mineyourmoney

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface ExpensesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addExpense(expense: Expense)

    @Transaction
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC")
    fun getAllExpensesLive(userId: Int): LiveData<List<ExpenseWithWallet>>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :start AND :end ORDER BY date DESC")
    fun getExpensesInRange(userId: Int, start: Long, end: Long): LiveData<List<ExpenseWithWallet>>
}