package com.iie.thethreeburnouts.mineyourmoney.expense

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import AppDatabase
import com.iie.thethreeburnouts.mineyourmoney.crystals.MiningManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExpensesViewModel(application: Application, private val userId: Int)
    : AndroidViewModel(application) {

    private val expensesDao = AppDatabase.getInstance(application).expensesDao()
    private val allExpenses = expensesDao.getAllExpensesLive(userId)

    val expense: LiveData<List<ExpenseWithWallet>> =
        MediatorLiveData<List<ExpenseWithWallet>>().apply {
            addSource(allExpenses) { value = it }
        }

    // ⭐ RESTORED — this is required by ExpenseDetailsFragment
    fun getExpenseById(expenseId: Int): LiveData<ExpenseWithWallet> {
        return expensesDao.getExpenseById(expenseId)
    }

    fun addExpense(expense: Expense, onResult: (Boolean, Long) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {

            val walletDao = AppDatabase.getInstance(getApplication()).walletDao()

            val newId = expensesDao.checkIfSufficientFunds(
                expense.copy(userId = userId),
                walletDao
            )

            withContext(Dispatchers.Main) {
                onResult(newId != -1L, newId)

                if (newId != -1L) {
                    MiningManager.addSwing(getApplication(), 1)
                    Log.i("ExpensesViewModel", "Awarded +1 swing for logging an expense")
                }
            }
        }
    }

    fun getExpensesInRange(startDate: Long, endDate: Long): LiveData<List<ExpenseWithWallet>> {
        return expensesDao.getExpensesInRange(userId, startDate, endDate)
    }

    fun deleteExpense(expenseId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(getApplication())
            val expensesDao = db.expensesDao()
            val walletDao = db.walletDao()
            val budgetDao = db.budgetDao()

            val exp = expensesDao.getExpenseByIdSync(expenseId)

            if (exp != null) {
                walletDao.addToWallet(exp.walletId, exp.amount)
                budgetDao.refundSpending(exp.userId, exp.amount)
                expensesDao.deleteExpense(exp)

                // Cancel automatic recurring deduction
                WorkManager.getInstance(getApplication())
                    .cancelUniqueWork("recurring_expense_${exp.id}_${exp.recurrence}")
            }
        }
    }
}
