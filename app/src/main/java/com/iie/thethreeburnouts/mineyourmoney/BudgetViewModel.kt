package com.iie.thethreeburnouts.mineyourmoney

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
                repository.saveBudget(Budget(userId = userId, monthlyLimit = 0.0, totalSpent = 0.0, lastUpdatedMonth = currentMonth))
            } else if (existing.lastUpdatedMonth != currentMonth) {
                // Reset for new month
                repository.saveBudget(existing.copy(totalSpent = 0.0, monthlyLimit = 0.0, lastUpdatedMonth = currentMonth))
            }
        }
    }

    fun updateMonthlyLimit(newLimit: Double) {
        viewModelScope.launch { //(Google Developers Training team, 2025)
            val current = repository.getBudget(userId)
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH)

            if (current != null) {
                val updatedBudget = if (current.lastUpdatedMonth != currentMonth) {
                    // It's a new month, reset totalSpent and update month
                    current.copy(
                        monthlyLimit = 0.0,
                        totalSpent = 0.0,
                        lastUpdatedMonth = currentMonth
                    )
                } else {
                    // Same month, just update limit
                    current.copy(monthlyLimit = newLimit)
                }
                repository.saveBudget(updatedBudget)
            } else {
                // No budget exists yet, create one with current month
                repository.saveBudget(
                    Budget(
                        userId = userId,
                        monthlyLimit = 0.0,
                        totalSpent = 0.0,
                        lastUpdatedMonth = currentMonth
                    )
                )
            }
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
//Reference List:
/* Google Developers Training team. 2025. ViewModel overview. [Online].
Available at: https://developer.android.com/topic/libraries/architecture/viewmodel [Accessed 6 October 2025). */