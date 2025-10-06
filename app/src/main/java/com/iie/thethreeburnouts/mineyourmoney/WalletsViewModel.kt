package com.iie.thethreeburnouts.mineyourmoney

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WalletsViewModel (application: Application, private val userId: Int) : AndroidViewModel(application) { //(Google Developers Training team, 2025)
    private val walletDao = AppDatabase.getInstance(application).walletDao()

    private val _currentSort = MutableLiveData<SortType>(SortType.DEFAULT)

    private val allWallets: LiveData<List<Wallet>> = walletDao.getAllWalletsLive(userId)

    val wallets: LiveData<List<Wallet>> = MediatorLiveData<List<Wallet>>().apply {
        addSource(allWallets) { updateWallets(this) }
        addSource(_currentSort) { updateWallets(this) }
    }

    private fun updateWallets(mediator: MediatorLiveData<List<Wallet>>) {
        val walletsList = allWallets.value.orEmpty()
        val sortType = _currentSort.value ?: SortType.DEFAULT
        mediator.value = when (sortType) {
            SortType.DEFAULT -> walletsList.sortedBy { it.name.lowercase() }
            SortType.BALANCE_HIGH -> walletsList.sortedByDescending { it.balance }
            SortType.BALANCE_LOW -> walletsList.sortedBy { it.balance }
        }
    }

    fun setSort(sortType: SortType) {
        _currentSort.value = sortType
    }

    fun addWallet(wallet: Wallet) {
        viewModelScope.launch(Dispatchers.IO) { //(Google Developers Training team, 2025)
            val db = AppDatabase.getInstance(getApplication())
            val walletDao = db.walletDao()
            val budgetDao = db.budgetDao()

            // Save wallet for this user
            walletDao.addWallet(wallet.copy(userId = userId))

            // Subtract wallet's initial balance from user's budget totalSpent
            if (wallet.balance > 0) {
                budgetDao.addSpending(userId, wallet.balance)
            }
        }
    }

    fun getCurrentSort(): SortType = _currentSort.value ?: SortType.DEFAULT

    fun deleteWalletAndExpenses(wallet: Wallet) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(getApplication())
            val walletDao = db.walletDao()
            val expenseDao = db.expensesDao()
            val budgetDao = db.budgetDao()

            // Get total spent in this wallet
            val totalExpenses = expenseDao.getTotalSpentInWallet(wallet.id) ?: 0.0

            // Refund the wallet's balance + its total spent to the budget
            val refundAmount = wallet.balance + totalExpenses
            if (refundAmount > 0) {
                budgetDao.refundSpending(userId, refundAmount)
            }

            // Delete all expenses associated with this wallet
            expenseDao.deleteExpensesByWallet(wallet.id)

            // Delete the wallet itself
            walletDao.deleteWallet(wallet)
        }
    }
}
//Reference List:
/* Google Developers Training team. 2025. ViewModel overview. [Online].
Available at: https://developer.android.com/topic/libraries/architecture/viewmodel [Accessed 6 October 2025). */