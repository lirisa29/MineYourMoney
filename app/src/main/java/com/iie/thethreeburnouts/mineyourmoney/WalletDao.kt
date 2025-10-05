package com.iie.thethreeburnouts.mineyourmoney

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao //(Google Developers Training team, 2024)
interface WalletDao { //(Google Developers Training team, 2024)

    @Insert(onConflict = OnConflictStrategy.REPLACE) //(Google Developers Training team, 2024)
    suspend fun addWallet(wallet: Wallet) //(Google Developers Training team, 2024)

    @Query("SELECT * FROM wallets WHERE userId = :userId ORDER BY id ASC") //(Google Developers Training team, 2024)
    fun getAllWalletsLive(userId: Int): LiveData<List<Wallet>> //(Google Developers Training team, 2024)

    @Query("SELECT * FROM wallets WHERE userId = :userId") //(Google Developers Training team, 2024)
    suspend fun getAllWallets(userId: Int): List<Wallet> //(Google Developers Training team, 2024)

    @Query("SELECT EXISTS(SELECT 1 FROM wallets WHERE userId = :userId AND LOWER(name) = LOWER(:name))") //(Google Developers Training team, 2024)
    suspend fun walletExists(userId: Int, name: String): Boolean //(Google Developers Training team, 2024)

    @Query("UPDATE wallets SET balance = balance - :amount WHERE id = :walletId")
    suspend fun subtractFromWallet(walletId: Int, amount: Double)

    @Query("SELECT balance FROM wallets WHERE id = :walletId LIMIT 1")
    suspend fun getWalletBalance(walletId: Int): Double

    @Query("UPDATE wallets SET balance = balance + :amount WHERE id = :walletId")
    suspend fun addToWallet(walletId: Int, amount: Double)

    @Query("DELETE FROM wallets WHERE userId = :userId") //(Google Developers Training team, 2024)
    suspend fun clearAll(userId: Int) //(Google Developers Training team, 2024)
}
//Reference List:
/*(Google Developers Training team, 2024). Save data in a local database using Room. [Online].
Available at: https://developer.android.com/training/data-storage/room [Accessed 3 October 2025). */