package com.iie.thethreeburnouts.mineyourmoney.wallet

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "wallets") //(Google Developers Training team, 2024)
data class Wallet( //(Google Developers Training team, 2024)
    @PrimaryKey(autoGenerate = true) val id: Int = 0, //(Google Developers Training team, 2024)
    val userId: Int, //(Google Developers Training team, 2024)
    val name: String, //(Google Developers Training team, 2024)
    val balance: Double, //(Google Developers Training team, 2024)
    val iconResId: Int, //(Google Developers Training team, 2024)
    val color: Int, //(Google Developers Training team, 2024)
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
) : Parcelable