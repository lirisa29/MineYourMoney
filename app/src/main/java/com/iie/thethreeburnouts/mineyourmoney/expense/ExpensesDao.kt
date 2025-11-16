package com.iie.thethreeburnouts.mineyourmoney.expense

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.iie.thethreeburnouts.mineyourmoney.wallet.WalletDao
import com.iie.thethreeburnouts.mineyourmoney.wallet.WalletRepository

@Dao //(Google Developers Training team, 2025)
interface ExpensesDao {  //(Google Developers Training team, 2025)
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE) //(Google Developers Training team, 2025)
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
            val repository = WalletRepository(walletDao)
            repository.subtractFromWallet(expense.walletId, expense.amount)
            newId // return the inserted expense ID
        } else {
            -1L // signal insufficient funds
        }
    }

    @Query("SELECT e.*, w.* FROM expenses e LEFT JOIN wallets w ON e.walletId = w.id WHERE e.id = :expenseId")
    fun getExpenseById(expenseId: Int): LiveData<ExpenseWithWallet>

    @Query("UPDATE expenses SET deletedAt = :timestamp, updatedAt = :updatedAt WHERE id = :expenseId")
    suspend fun markDeleted(expenseId: Int, timestamp: Long, updatedAt: Long)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM expenses WHERE id = :expenseId LIMIT 1")
    suspend fun getExpenseByIdSync(expenseId: Int): Expense?

    @Query("SELECT SUM(amount) FROM expenses WHERE walletId = :walletId")
    suspend fun getTotalSpentInWallet(walletId: Int): Double?

    @Query("DELETE FROM expenses WHERE walletId = :walletId")
    suspend fun deleteExpensesByWallet(walletId: Int)

    @Query("SELECT * FROM expenses WHERE userId = :userId AND deletedAt IS NULL ORDER BY date DESC")
    suspend fun getAllExpensesSync(userId: Int): List<Expense>

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC")
    suspend fun getAllExpensesIncludingDeleted(userId: Int): List<Expense>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND deletedAt IS NOT NULL")
    suspend fun getDeletedExpenses(userId: Int): List<Expense>

    @Query("SELECT * FROM expenses WHERE walletId = :walletId AND deletedAt IS NULL")
    suspend fun getAllExpensesSyncByWallet(walletId: Int): List<Expense>
}