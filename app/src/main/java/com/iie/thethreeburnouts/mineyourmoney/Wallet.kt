package com.iie.thethreeburnouts.mineyourmoney

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "wallets")
data class Wallet(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val balance: Double,
    val iconResId: Int
)
