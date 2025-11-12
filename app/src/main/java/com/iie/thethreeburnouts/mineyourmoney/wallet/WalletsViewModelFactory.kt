package com.iie.thethreeburnouts.mineyourmoney.wallet

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class WalletsViewModelFactory (private val application: Application, private val userId: Int) : ViewModelProvider.Factory{ //(Google Developers Training team, 2025)
    @Suppress("UNCHECKED_CAST") //(Google Developers Training team, 2025)
    override fun <T : ViewModel> create(modelClass: Class<T>): T { //(Google Developers Training team, 2025)
        if (modelClass.isAssignableFrom(WalletsViewModel::class.java)) { //(Google Developers Training team, 2025)
            return WalletsViewModel(application, userId) as T //(Google Developers Training team, 2025)
        }
        throw IllegalArgumentException("Unknown ViewModel class") //(Google Developers Training team, 2025)
    }
}