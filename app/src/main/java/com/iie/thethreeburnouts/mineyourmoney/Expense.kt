package com.iie.thethreeburnouts.mineyourmoney

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
    val photoPath: String? = null
)
//Reference List:
/* Google Developers Training team. 2024. Save data in a local database using Room. [Online].
Available at: https://developer.android.com/training/data-storage/room [Accessed 3 October 2025). */