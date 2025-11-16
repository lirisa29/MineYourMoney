package com.iie.thethreeburnouts.mineyourmoney.expense

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.iie.thethreeburnouts.mineyourmoney.budget.BudgetRepository
import com.iie.thethreeburnouts.mineyourmoney.expense.ExpenseWithWallet
import com.iie.thethreeburnouts.mineyourmoney.wallet.WalletRepository
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

    private fun updateExpenses(mediator: MediatorLiveData<List<ExpenseWithWallet>>) {
        Log.i("ExpensesViewModel", "Updating MediatorLiveData with latest expenses")
        // Filter out deleted expenses
        mediator.value = allExpenses.value.orEmpty()
            .filter { it.expense.deletedAt == null }
    }

    fun addExpense(expense: Expense, onResult: (Boolean, Long) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {

            val walletDao = AppDatabase.getInstance(getApplication()).walletDao()
            val newId = expensesDao.checkIfSufficientFunds(expense.copy(userId = userId, updatedAt = System.currentTimeMillis()), walletDao)

            // Upload to Firestore
            if (newId != -1L) {
                val repo = ExpenseRepository(expensesDao)
                val savedExpense = expensesDao.getExpenseByIdSync(newId.toInt())
                savedExpense?.let { repo.uploadExpense(it) }
            }

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
            val walletRepo = WalletRepository(walletDao)
            val budgetDao = db.budgetDao()
            val budgetRepo = BudgetRepository(budgetDao)

            // Get the expense (synchronously)
            val expense = expensesDao.getExpenseByIdSync(expenseId)

            if (expense != null) {
                // Refund the wallet balance
                Log.i("ExpensesViewModel", "Expense found and refunding the wallet")
                walletRepo.addToWallet(expense.walletId, expense.amount)

                // Refund spending in budget
                Log.i("ExpensesViewModel", "Refunding spending in budget")
                budgetRepo.refundSpending(expense.userId, expense.amount)

                // Soft delete locally + remove from Firestore
                val repo = ExpenseRepository(expensesDao)
                repo.deleteExpense(expense)

                // Cancel automatic recurring deduction
                WorkManager.getInstance(getApplication())
                    .cancelUniqueWork("recurring_expense_${exp.id}_${exp.recurrence}")
            }
        }
    }
}
