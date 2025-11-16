package com.iie.thethreeburnouts.mineyourmoney.wallet

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao //(Google Developers Training team, 2025)
interface WalletDao { //(Google Developers Training team, 2025)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE) //(Google Developers Training team, 2025)
    suspend fun addWallet(wallet: Wallet) //(Google Developers Training team, 2025)

    @Query("SELECT * FROM wallets WHERE userId = :userId AND deletedAt IS NULL ORDER BY id ASC") //(Google Developers Training team, 2025)
    fun getAllWalletsLive(userId: Int): LiveData<List<Wallet>> //(Google Developers Training team, 2025)

    @Query("SELECT * FROM wallets WHERE userId = :userId AND deletedAt IS NULL") //(Google Developers Training team, 2025)
    suspend fun getAllWallets(userId: Int): List<Wallet> //(Google Developers Training team, 2025)

    // Return all wallets including deleted ones
    @Query("SELECT * FROM wallets WHERE userId = :userId")
    suspend fun getAllWalletsIncludingDeleted(userId: Int): List<Wallet>

    @Query("SELECT EXISTS(SELECT 1 FROM wallets WHERE userId = :userId AND LOWER(name) = LOWER(:name))") //(Google Developers Training team, 2025)
    suspend fun walletExists(userId: Int, name: String): Boolean //(Google Developers Training team, 2025)

    @Query("UPDATE wallets SET balance = balance - :amount WHERE id = :walletId")
    suspend fun subtractFromWallet(walletId: Int, amount: Double)

    @Query("SELECT balance FROM wallets WHERE id = :walletId LIMIT 1")
    suspend fun getWalletBalance(walletId: Int): Double

    @Query("UPDATE wallets SET balance = balance + :amount WHERE id = :walletId")
    suspend fun addToWallet(walletId: Int, amount: Double)

    @Query("DELETE FROM wallets WHERE userId = :userId") //(Google Developers Training team, 2025)
    suspend fun clearAll(userId: Int) //(Google Developers Training team, 2025)

    @Delete
    suspend fun deleteWallet(wallet: Wallet)

    @Query("SELECT * FROM wallets WHERE id = :walletId")
    suspend fun getWalletById(walletId: Int): Wallet?

    @Query("SELECT * FROM wallets WHERE userId = :userId AND deletedAt IS NOT NULL")
    suspend fun getDeletedWallets(userId: Int): List<Wallet>
}