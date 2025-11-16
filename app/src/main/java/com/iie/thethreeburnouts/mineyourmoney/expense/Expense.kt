package com.iie.thethreeburnouts.mineyourmoney.expense

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses") //(Google Developers Training team, 2024)
data class Expense( //(Google Developers Training team, 2024)
    @PrimaryKey(autoGenerate = true) val id: Int = 0, //(Google Developers Training team, 2024)
    val userId: Int,
    val amount: Double,
    val note: String?,
    val walletId: Int,
    val recurrence: String?,
    val date: Long,
    val photoPath: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)