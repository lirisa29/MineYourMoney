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

    @Query("SELECT * FROM wallets WHERE userId = :userId ORDER BY id ASC")
    fun getAllWalletsLive(userId: Int): LiveData<List<Wallet>>

    @Query("SELECT * FROM wallets WHERE userId = :userId")
    suspend fun getAllWallets(userId: Int): List<Wallet>

    @Query("SELECT EXISTS(SELECT 1 FROM wallets WHERE userId = :userId AND LOWER(name) = LOWER(:name))")
    suspend fun walletExists(userId: Int, name: String): Boolean

    @Query("DELETE FROM wallets WHERE userId = :userId")
    suspend fun clearAll(userId: Int)
}
