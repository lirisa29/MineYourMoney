package com.iie.thethreeburnouts.mineyourmoney

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.collections.orEmpty

class ExpensesViewModel (application: Application, private val userId: Int) : AndroidViewModel(application) {

    private val expensesDao = AppDatabase.getInstance(application).expensesDao()

    private val allExpenses: LiveData<List<ExpenseWithWallet>> = expensesDao.getAllExpensesLive(userId)

    val expense: LiveData<List<ExpenseWithWallet>> = MediatorLiveData<List<ExpenseWithWallet>>().apply {
        addSource(allExpenses) { updateExpenses(this) }
    }

    private fun updateExpenses(mediator: MediatorLiveData<List<ExpenseWithWallet>>) {
        mediator.value = allExpenses.value.orEmpty()
    }

    fun addExpense(expense: Expense) {
        viewModelScope.launch(Dispatchers.IO) {
            expensesDao.addExpense(expense.copy(userId = userId))
        }
    }

    fun getExpensesInRange(startDate: Long, endDate: Long): LiveData<List<ExpenseWithWallet>> {
        return expensesDao.getExpensesInRange(userId, startDate, endDate)
    }
}