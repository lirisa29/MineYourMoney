package com.iie.thethreeburnouts.mineyourmoney.expense

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses") //(Google Developers Training team, 2024)
data class Expense( //(Google Developers Training team, 2024)
    @PrimaryKey(autoGenerate = true) val id: Int = 0, //(Google Developers Training team, 2024)
    val userId: Int = 0,
    val amount: Double = 0.0,
    val note: String? = null,
    val walletId: Int = 0,
    val recurrence: String? = null,
    val date: Long = 0L,
    val photoPath: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)