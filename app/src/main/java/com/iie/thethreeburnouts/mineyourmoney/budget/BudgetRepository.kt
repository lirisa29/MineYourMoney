package com.iie.thethreeburnouts.mineyourmoney.budget

class BudgetRepository (private val dao: BudgetDao) {

    fun getBudgetLive(userId: Int) = dao.getBudgetLive(userId)

    suspend fun getBudget(userId: Int) = dao.getBudget(userId)

    suspend fun saveBudget(budget: Budget) = dao.insertOrUpdateBudget(budget)

    suspend fun addSpending(userId: Int, amount: Double) = dao.addSpending(userId, amount)

    suspend fun refundSpending(userId: Int, amount: Double) = dao.refundSpending(userId, amount)
}