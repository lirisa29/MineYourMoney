package com.iie.thethreeburnouts.mineyourmoney

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WalletDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addWallet(wallet: Wallet)

    @Query("SELECT * FROM wallets ORDER BY id ASC")
    fun getAllWalletsLive(): LiveData<List<Wallet>>

    @Query("SELECT * FROM wallets")
    suspend fun getAllWallets(): List<Wallet>

    @Query("SELECT EXISTS(SELECT 1 FROM wallets WHERE LOWER(name) = LOWER(:name))")
    suspend fun walletExists(name: String): Boolean

    // This will fetch all wallets, then sort in Kotlin
    suspend fun getSortedWallets(sortType: SortType): List<Wallet> {
        val wallets = getAllWallets()
        return when (sortType) {
            SortType.DEFAULT -> wallets.sortedBy { it.name.lowercase() }
            SortType.BALANCE_HIGH -> wallets.sortedByDescending { it.balance }
            SortType.BALANCE_LOW -> wallets.sortedBy { it.balance }
        }
    }

    @Query("DELETE FROM wallets")
    suspend fun clearAll()
}
