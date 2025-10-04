package com.iie.thethreeburnouts.mineyourmoney

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ExpensesViewModelFactory (
    private val application: Application,
    private val userId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpensesViewModel::class.java)) {
            return ExpensesViewModel(application, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}