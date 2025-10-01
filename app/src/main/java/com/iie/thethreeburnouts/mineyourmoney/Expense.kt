package com.iie.thethreeburnouts.mineyourmoney

import android.net.Uri
import java.util.Calendar


data class Expense(
    val amount: Double,
    val note: String?,
    val wallet: Wallet,
    val recurrence: String?,
    val date: Calendar,
    val photoUri: Uri? = null
)
