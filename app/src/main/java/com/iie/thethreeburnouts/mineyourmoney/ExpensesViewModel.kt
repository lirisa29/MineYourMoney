package com.iie.thethreeburnouts.mineyourmoney

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.orEmpty

class ExpensesViewModel (application: Application, private val userId: Int) : AndroidViewModel(application) {

    private val expensesDao = AppDatabase.getInstance(application).expensesDao()

    private val allExpenses: LiveData<List<ExpenseWithWallet>> = expensesDao.getAllExpensesLive(userId)

    val expense: LiveData<List<ExpenseWithWallet>> = MediatorLiveData<List<ExpenseWithWallet>>().apply {
        addSource(allExpenses) { updateExpenses(this) }
    }

    // Get expense by ID
    fun getExpenseById(expenseId: Int): LiveData<ExpenseWithWallet> {
        Log.i("ExpensesViewModel", "Fetching expense")
        return expensesDao.getExpenseById(expenseId)
    }

    private fun updateExpenses(mediator: MediatorLiveData<List<ExpenseWithWallet>>) {
        Log.i("ExpensesViewModel", "Updating MediatorLiveData with latest expenses")
        mediator.value = allExpenses.value.orEmpty()
    }

    fun addExpense(expense: Expense, onResult: (Boolean, Long) -> Unit) {
        Log.i("ExpensesViewModel", "Request to add expense")
        viewModelScope.launch(Dispatchers.IO) {
            val walletDao = AppDatabase.getInstance(getApplication()).walletDao()
            val newId = expensesDao.checkIfSufficientFunds(expense.copy(userId = userId), walletDao)
            withContext(Dispatchers.Main) {
                onResult(newId != -1L, newId)
            }
        }
    }

    fun getExpensesInRange(startDate: Long, endDate: Long): LiveData<List<ExpenseWithWallet>> {
        return expensesDao.getExpensesInRange(userId, startDate, endDate)
    }

    // Delete expense
    fun deleteExpense(expenseId: Int) {
        Log.w("ExpensesViewModel", "Attempting to delete expense ID")
        viewModelScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(getApplication())
            val expensesDao = db.expensesDao()
            val walletDao = db.walletDao()

            // Get the expense (synchronously)
            val expense = expensesDao.getExpenseByIdSync(expenseId)

            if (expense != null) {
                // Refund the wallet balance
                Log.i("ExpensesViewModel", "Expense found and refunding the wallet")
                walletDao.addToWallet(expense.walletId, expense.amount)

                // Delete expense
                Log.i("ExpensesViewModel", "Deleting expense ID")
                expensesDao.deleteExpense(expense)

                // Cancel recurring work
                Log.i("ExpensesViewModel", "Cancelling recurring work")
                WorkManager.getInstance(getApplication())
                    .cancelUniqueWork("recurring_expense_${expense.id}_${expense.recurrence}")
            }
        }
    }
}