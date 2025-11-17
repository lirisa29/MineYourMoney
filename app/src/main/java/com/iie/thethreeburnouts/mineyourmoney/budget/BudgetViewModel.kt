package com.iie.thethreeburnouts.mineyourmoney.budget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.Calendar

class BudgetViewModel(application: Application, private val userId: Int) : //(Google Developers Training team, 2025)
    AndroidViewModel(application) { //(Google Developers Training team, 2025)

    private val dao = AppDatabase.getInstance(application).budgetDao()
    private val repository = BudgetRepository(dao)

    val budget: LiveData<Budget?> = repository.getBudgetLive(userId)

    fun loadOrInitBudget() {
        viewModelScope.launch {
            val existing = repository.getBudget(userId)
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH)

            // If no local budget AND no remote budget → initialize only then
            if (existing == null) {
                repository.saveBudget(
                    Budget(
                        userId = userId,
                        minLimit = 0.0,
                        maxLimit = 0.0,
                        totalSpent = 0.0,
                        lastUpdatedMonth = currentMonth
                    )
                )
                return@launch
            }

            // Monthly reset should NOT touch min/max (those are user settings!)
            if (existing.lastUpdatedMonth != currentMonth) {
                val updated = existing.copy(
                    totalSpent = 0.0,
                    lastUpdatedMonth = currentMonth
                )
                repository.saveBudget(updated)
            }
        }
    }

    fun updateBudgetLimits(minLimit: Double, maxLimit: Double) {
        viewModelScope.launch {
            val current = repository.getBudget(userId)
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH)

            val updatedBudget = if (current != null) {
                current.copy(
                    minLimit = minLimit,
                    maxLimit = maxLimit,
                    lastUpdatedMonth = currentMonth
                )
            } else {
                Budget(
                    userId = userId,
                    minLimit = minLimit,
                    maxLimit = maxLimit,
                    totalSpent = 0.0,
                    lastUpdatedMonth = currentMonth
                )
            }
            repository.saveBudget(updatedBudget)
        }
    }

    fun addSpending(amount: Double) {
        viewModelScope.launch { //(Google Developers Training team, 2025)
            repository.addSpending(userId, amount)
        }
    }

    fun refundSpending(amount: Double) {
        viewModelScope.launch { //(Google Developers Training team, 2025)
            repository.refundSpending(userId, amount)
        }
    }
}