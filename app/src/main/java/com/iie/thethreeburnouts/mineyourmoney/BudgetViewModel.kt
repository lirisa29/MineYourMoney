package com.iie.thethreeburnouts.mineyourmoney

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class BudgetViewModel(application: Application, private val userId: Int) :
    AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).budgetDao()
    private val repository = BudgetRepository(dao)

    val budget: LiveData<Budget?> = repository.getBudgetLive(userId)

    fun loadOrInitBudget() {
        viewModelScope.launch {
            val existing = repository.getBudget(userId)
            if (existing == null) {
                repository.saveBudget(
                    Budget(
                        userId = userId,
                        monthlyLimit = 0.0,
                        totalSpent = 0.0
                    )
                )
            }
        }
    }

    fun updateMonthlyLimit(newLimit: Double) {
        viewModelScope.launch {
            val current = repository.getBudget(userId)
            if (current != null) {
                repository.saveBudget(current.copy(monthlyLimit = newLimit))
            } else {
                repository.saveBudget(Budget(userId = userId, monthlyLimit = newLimit, totalSpent = 0.0))
            }
        }
    }

    fun addSpending(amount: Double) {
        viewModelScope.launch {
            repository.addSpending(userId, amount)
        }
    }

    fun refundSpending(amount: Double) {
        viewModelScope.launch {
            repository.refundSpending(userId, amount)
        }
    }
}