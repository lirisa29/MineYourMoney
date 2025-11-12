package com.iie.thethreeburnouts.mineyourmoney.wallet

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

            // Save wallet for this user
            walletDao.addWallet(wallet.copy(userId = userId))
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
            if (totalExpenses > 0) {
                budgetDao.refundSpending(userId, totalExpenses)
            }

            // Delete all expenses associated with this wallet
            expenseDao.deleteExpensesByWallet(wallet.id)

            // Delete the wallet itself
            walletDao.deleteWallet(wallet)
        }
    }
}