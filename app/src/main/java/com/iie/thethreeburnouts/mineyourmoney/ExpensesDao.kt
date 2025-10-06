package com.iie.thethreeburnouts.mineyourmoney

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao //(Google Developers Training team, 2025)
interface ExpensesDao {  //(Google Developers Training team, 2025)
    @Insert(onConflict = OnConflictStrategy.REPLACE) //(Google Developers Training team, 2025)
    suspend fun addExpense(expense: Expense) : Long //(Google Developers Training team, 2025)

    @Transaction
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC") //(Google Developers Training team, 2025)
    fun getAllExpensesLive(userId: Int): LiveData<List<ExpenseWithWallet>> //(Google Developers Training team, 2025)

    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :start AND :end ORDER BY date DESC") //(Google Developers Training team, 2025)
    fun getExpensesInRange(userId: Int, start: Long, end: Long): LiveData<List<ExpenseWithWallet>> //(Google Developers Training team, 2025)

    @Transaction
    suspend fun checkIfSufficientFunds(expense: Expense, walletDao: WalletDao): Long {
        val currentBalance = walletDao.getWalletBalance(expense.walletId)
        return if (currentBalance >= expense.amount) {
            val newId = addExpense(expense)
            walletDao.subtractFromWallet(expense.walletId, expense.amount)
            newId // return the inserted expense ID
        } else {
            -1L // signal insufficient funds
        }
    }

    @Query("SELECT e.*, w.* FROM expenses e LEFT JOIN wallets w ON e.walletId = w.id WHERE e.id = :expenseId")
    fun getExpenseById(expenseId: Int): LiveData<ExpenseWithWallet>

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM expenses WHERE id = :expenseId LIMIT 1")
    suspend fun getExpenseByIdSync(expenseId: Int): Expense?
}
//Reference List:
/*(Google Developers Training team, 2025). Save data in a local database using Room. [Online].
Available at: https://developer.android.com/training/data-storage/room [Accessed 3 October 2025). */