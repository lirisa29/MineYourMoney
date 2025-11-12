package com.iie.thethreeburnouts.mineyourmoney.expense

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ExpensesViewModelFactory (
    private val application: Application,
    private val userId: Int
) : ViewModelProvider.Factory { //(Google Developers Training team, 2025)
    @Suppress("UNCHECKED_CAST") //(Google Developers Training team, 2025)
    override fun <T : ViewModel> create(modelClass: Class<T>): T { //(Google Developers Training team, 2025)
        if (modelClass.isAssignableFrom(ExpensesViewModel::class.java)) { //(Google Developers Training team, 2025)
            return ExpensesViewModel(application, userId) as T //(Google Developers Training team, 2025)
        }
        throw IllegalArgumentException("Unknown ViewModel class") //(Google Developers Training team, 2025)
    }
}