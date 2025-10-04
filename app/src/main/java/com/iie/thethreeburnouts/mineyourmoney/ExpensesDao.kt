package com.iie.thethreeburnouts.mineyourmoney

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao //(Google Developers Training team, 2024)
interface ExpensesDao {  //(Google Developers Training team, 2024)
    @Insert(onConflict = OnConflictStrategy.REPLACE) //(Google Developers Training team, 2024)
    suspend fun addExpense(expense: Expense) //(Google Developers Training team, 2024)

    @Transaction
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC") //(Google Developers Training team, 2024)
    fun getAllExpensesLive(userId: Int): LiveData<List<ExpenseWithWallet>> //(Google Developers Training team, 2024)

    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :start AND :end ORDER BY date DESC") //(Google Developers Training team, 2024)
    fun getExpensesInRange(userId: Int, start: Long, end: Long): LiveData<List<ExpenseWithWallet>> //(Google Developers Training team, 2024)
}
//Reference List:
/*(Google Developers Training team, 2024). Save data in a local database using Room. [Online].
Available at: https://developer.android.com/training/data-storage/room [Accessed 3 October 2025). */