package com.iie.thethreeburnouts.mineyourmoney

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WalletsViewModel (application: Application) : AndroidViewModel(application) {
    private val walletDao = AppDatabase.getInstance(application).walletDao()

    private val _currentSort = MutableLiveData<SortType>(SortType.DEFAULT)

    private val allWallets: LiveData<List<Wallet>> = walletDao.getAllWalletsLive()

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
        viewModelScope.launch(Dispatchers.IO) {
            walletDao.addWallet(wallet)
        }
    }

    fun getCurrentSort(): SortType = _currentSort.value ?: SortType.DEFAULT
}