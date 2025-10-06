package com.iie.thethreeburnouts.mineyourmoney

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE userId = :userId LIMIT 1")
    fun getBudgetLive(userId: Int): LiveData<Budget?>

    @Query("SELECT * FROM budgets WHERE userId = :userId LIMIT 1")
    suspend fun getBudget(userId: Int): Budget?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudget(budget: Budget)

    @Query("UPDATE budgets SET totalSpent = totalSpent + :amount WHERE userId = :userId")
    suspend fun addSpending(userId: Int, amount: Double)

    @Query("UPDATE budgets SET totalSpent = totalSpent - :amount WHERE userId = :userId")
    suspend fun refundSpending(userId: Int, amount: Double)

    @Query("SELECT (monthlyLimit - totalSpent) FROM budgets WHERE userId = :userId LIMIT 1")
    suspend fun getRemainingBudget(userId: Int): Double
}