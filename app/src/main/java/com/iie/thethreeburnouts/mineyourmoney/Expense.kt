package com.iie.thethreeburnouts.mineyourmoney

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val note: String?,
    val walletId: Int,
    val recurrence: String?,
    val date: Long,
    val photoPath: String? = null
)
