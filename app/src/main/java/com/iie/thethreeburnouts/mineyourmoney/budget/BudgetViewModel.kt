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
        viewModelScope.launch { //(Google Developers Training team, 2025)
            val existing = repository.getBudget(userId)
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
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
            } else if (existing.lastUpdatedMonth != currentMonth) {
                // Reset for new month
                repository.saveBudget(existing.copy(totalSpent = 0.0, minLimit = 0.0, maxLimit = 0.0, lastUpdatedMonth = currentMonth))
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