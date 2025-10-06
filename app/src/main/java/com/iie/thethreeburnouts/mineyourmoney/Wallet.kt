package com.iie.thethreeburnouts.mineyourmoney

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "wallets") //(Google Developers Training team, 2024)
data class Wallet( //(Google Developers Training team, 2024)
    @PrimaryKey(autoGenerate = true) val id: Int = 0, //(Google Developers Training team, 2024)
    val userId: Int, //(Google Developers Training team, 2024)
    val name: String, //(Google Developers Training team, 2024)
    val balance: Double, //(Google Developers Training team, 2024)
    val iconResId: Int, //(Google Developers Training team, 2024)
    val color: Int //(Google Developers Training team, 2024)
)
//Reference List:
/* Google Developers Training team. 2024. Save data in a local database using Room. [Online].
Available at: https://developer.android.com/training/data-storage/room [Accessed 3 October 2025). */